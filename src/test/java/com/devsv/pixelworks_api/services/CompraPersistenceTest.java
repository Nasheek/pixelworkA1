package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.dto.DetalleItemDTO;
import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import com.devsv.pixelworks_api.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CompraPersistenceTest {

    @Autowired CompraService servicio;
    @Autowired CompraRepository compras;
    @Autowired DetalleCompraRepository detalles;
    @Autowired ClaveActivacionRepository claves;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired CategoriaRepository categorias;
    @Autowired DesarrolladorRepository desarrolladores;
    @Autowired ProductoRepository productos;
    @Autowired MetodoPagoRepository metodosPago;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired EntityManager entityManager;
    @Autowired DataSource dataSource;

    private TransactionTemplate transacciones;
    private Fixture fixture;

    @BeforeEach
    void prepararDatosConfirmadosEnUnaTransaccionIndependiente() throws Exception {
        // These tests commit real transactions; refuse an accidental production datasource.
        try (Connection conexion = dataSource.getConnection()) {
            String url = conexion.getMetaData().getURL();
            assertTrue(url.startsWith("jdbc:h2:mem:")
                            || (url.startsWith("jdbc:postgresql:") && "pixelworks_review".equals(conexion.getCatalog())),
                    "Las pruebas de persistencia requieren H2 en memoria o la base aislada pixelworks_review.");
        }
        transacciones = new TransactionTemplate(transactionManager);
        fixture = transacciones.execute(status -> {
            String identificador = UUID.randomUUID().toString().substring(0, 8);
            Rol rol = roles.findByNombre("JUGADOR").orElseThrow();
            Usuario usuario = usuarios.save(new Usuario(null, "Comprador de prueba",
                    "compra-" + identificador + "@example.test", "password-no-usada", EstadoUsuario.ACTIVO, rol));
            Categoria categoria = categorias.save(new Categoria(null, "Compra " + identificador, "Pruebas"));
            Desarrollador desarrollador = desarrolladores.save(new Desarrollador(null,
                    "Estudio " + identificador, "Pruebas", TipoDesarrollador.I, "SV"));
            Producto primero = productos.save(new Producto(null, "Con stock " + identificador, "Pruebas",
                    LocalDate.now(), new BigDecimal("9.99"), null, true, categoria, desarrollador));
            Producto segundo = productos.save(new Producto(null, "Sin stock " + identificador, "Pruebas",
                    LocalDate.now(), new BigDecimal("5.00"), null, true, categoria, desarrollador));
            MetodoPago pago = metodosPago.save(new MetodoPago(null, "Pago " + identificador));
            ClaveActivacion clave = claves.save(new ClaveActivacion(null, primero,
                    "TEST-" + identificador + "-LAST", EstadoClave.DISPONIBLE, null));
            return new Fixture(usuario.getId(), categoria.getId(), desarrollador.getId(),
                    primero.getId(), segundo.getId(), pago.getId(), clave.getId(), clave.getCodigo());
        });
    }

    @AfterEach
    void eliminarSolamenteLosDatosDeEstaPrueba() {
        if (fixture == null) {
            return;
        }
        transacciones.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM ClaveActivacion c WHERE c.producto.id IN :ids")
                    .setParameter("ids", List.of(fixture.productoConStock(), fixture.productoSinStock())).executeUpdate();
            entityManager.createQuery("DELETE FROM DetalleCompra d WHERE d.compra.usuario.id = :usuario")
                    .setParameter("usuario", fixture.usuario()).executeUpdate();
            entityManager.createQuery("DELETE FROM Compra c WHERE c.usuario.id = :usuario")
                    .setParameter("usuario", fixture.usuario()).executeUpdate();
            entityManager.createQuery("DELETE FROM Producto p WHERE p.id IN :ids")
                    .setParameter("ids", List.of(fixture.productoConStock(), fixture.productoSinStock())).executeUpdate();
            usuarios.deleteById(fixture.usuario());
            metodosPago.deleteById(fixture.metodoPago());
            categorias.deleteById(fixture.categoria());
            desarrolladores.deleteById(fixture.desarrollador());
        });
    }

    @Test
    void unaCompraQueFallaEnElSegundoProductoRevierteLaClaveYaAsignada() {
        RealizarCompraDTO carrito = carrito(List.of(item(fixture.productoConStock()), item(fixture.productoSinStock())));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarCompra(carrito, fixture.usuario()));
        assertTrue(error.getMessage().startsWith("Stock insuficiente"));

        transacciones.executeWithoutResult(status -> {
            assertTrue(compras.findByUsuarioId(fixture.usuario()).isEmpty());
            ClaveActivacion clave = claves.findById(fixture.clave()).orElseThrow();
            assertEquals(EstadoClave.DISPONIBLE, clave.getEstado());
            assertNull(clave.getDetalleCompra());
            assertEquals(0L, entityManager.createQuery(
                            "SELECT COUNT(d) FROM DetalleCompra d WHERE d.producto.id = :producto", Long.class)
                    .setParameter("producto", fixture.productoConStock()).getSingleResult());
        });
    }

    @Test
    void dosComprasSimultaneasDeLaUltimaClaveSoloConfirmanUnaVenta() throws Exception {
        CountDownLatch preparadas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        try {
            Future<CompraResponseDTO> primera = ejecutor.submit(() -> comprarUltimaClave(preparadas, iniciar));
            Future<CompraResponseDTO> segunda = ejecutor.submit(() -> comprarUltimaClave(preparadas, iniciar));
            assertTrue(preparadas.await(5, TimeUnit.SECONDS), "Las dos compras deben estar preparadas.");
            iniciar.countDown();
            CompraResponseDTO resultadoPrimero = primera.get(Math.max(1, limite - System.nanoTime()), TimeUnit.NANOSECONDS);
            CompraResponseDTO resultadoSegundo = segunda.get(Math.max(1, limite - System.nanoTime()), TimeUnit.NANOSECONDS);
            assertNotEquals(resultadoPrimero == null, resultadoSegundo == null,
                    "Debe confirmarse exactamente una compra y rechazarse la otra por falta de stock.");
            CompraResponseDTO confirmada = resultadoPrimero != null ? resultadoPrimero : resultadoSegundo;
            assertEquals(List.of(fixture.codigo()), confirmada.getDetalles().get(0).getClaves());

            transacciones.executeWithoutResult(status -> {
                List<Compra> persistidas = compras.findByUsuarioId(fixture.usuario());
                assertEquals(1, persistidas.size());
                assertEquals(new BigDecimal("9.99"), persistidas.get(0).getTotal());
                List<DetalleCompra> detallesPersistidos = detalles.findByCompraId(persistidas.get(0).getId());
                assertEquals(1, detallesPersistidos.size());
                ClaveActivacion clave = claves.findById(fixture.clave()).orElseThrow();
                assertEquals(EstadoClave.VENDIDA, clave.getEstado());
                assertEquals(detallesPersistidos.get(0).getId(), clave.getDetalleCompra().getId());
                assertEquals(persistidas.get(0).getId(), clave.getDetalleCompra().getCompra().getId());
                assertEquals(0, claves.countByProductoIdAndEstado(fixture.productoConStock(), EstadoClave.DISPONIBLE));
            });
        } finally {
            iniciar.countDown();
            ejecutor.shutdownNow();
            assertTrue(ejecutor.awaitTermination(5, TimeUnit.SECONDS), "Las compras deben terminar antes de limpiar los datos.");
        }
    }

    private CompraResponseDTO comprarUltimaClave(CountDownLatch preparadas, CountDownLatch iniciar) throws Exception {
        preparadas.countDown();
        if (!iniciar.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("No se inició la compra concurrente dentro del tiempo previsto.");
        }
        try {
            return servicio.procesarCompra(carrito(List.of(item(fixture.productoConStock()))), fixture.usuario());
        } catch (IllegalArgumentException error) {
            if (error.getMessage() != null && error.getMessage().startsWith("Stock insuficiente")) {
                return null;
            }
            throw error;
        }
    }

    private RealizarCompraDTO carrito(List<DetalleItemDTO> items) {
        RealizarCompraDTO dto = new RealizarCompraDTO();
        dto.setMetodoPagoId(fixture.metodoPago());
        dto.setItems(items);
        return dto;
    }

    private static DetalleItemDTO item(Integer productoId) {
        DetalleItemDTO item = new DetalleItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(1);
        return item;
    }

    private record Fixture(Integer usuario, Integer categoria, Integer desarrollador, Integer productoConStock,
                           Integer productoSinStock, Integer metodoPago, Integer clave, String codigo) {
    }
}
