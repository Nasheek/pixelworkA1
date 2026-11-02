package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.dto.DetalleItemDTO;
import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.entities.Compra;
import com.devsv.pixelworks_api.entities.DetalleCompra;
import com.devsv.pixelworks_api.entities.MetodoPago;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.entities.Usuario;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.mappers.CompraMapper;
import com.devsv.pixelworks_api.repository.ClaveActivacionRepository;
import com.devsv.pixelworks_api.repository.CompraRepository;
import com.devsv.pixelworks_api.repository.DetalleCompraRepository;
import com.devsv.pixelworks_api.repository.MetodoPagoRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import com.devsv.pixelworks_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CompraServiceTests {

    private final CompraRepository compraRepository = mock(CompraRepository.class);
    private final DetalleCompraRepository detalleCompraRepository = mock(DetalleCompraRepository.class);
    private final ProductoRepository productoRepository = mock(ProductoRepository.class);
    private final MetodoPagoRepository metodoPagoRepository = mock(MetodoPagoRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final ClaveActivacionRepository claveActivacionRepository = mock(ClaveActivacionRepository.class);
    private final IOfertaService ofertaService = mock(IOfertaService.class);

    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(
                compraRepository,
                detalleCompraRepository,
                productoRepository,
                metodoPagoRepository,
                usuarioRepository,
                claveActivacionRepository,
                ofertaService,
                Mappers.getMapper(CompraMapper.class)
        );
    }

    @Test
    void compraConDescuentoCobraElPrecioRedondeadoQueMuestraElCatalogo() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(1);
        metodoPago.setNombre("Tarjeta");
        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("Juego");
        producto.setPrecio(new BigDecimal("19.99"));
        ClaveActivacion primeraClave = claveDisponible("AAAA-1111-BBBB", producto);
        ClaveActivacion segundaClave = claveDisponible("CCCC-2222-DDDD", producto);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoPago));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(claveActivacionRepository.findByProductoIdAndEstado(
                1, EstadoClave.DISPONIBLE, PageRequest.of(0, 2)
        )).thenReturn(List.of(primeraClave, segundaClave));
        when(ofertaService.obtenerDescuentoActivo(1)).thenReturn(new BigDecimal("50"));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1);
            return compra;
        });
        when(detalleCompraRepository.save(any(DetalleCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompraResponseDTO resultado = compraService.procesarCompra(compraConItems(List.of(item(1, 2))), 1);

        assertAll(
                () -> assertEquals(new BigDecimal("10.00"), resultado.getDetalles().get(0).getPrecioUnitario()),
                () -> assertEquals(new BigDecimal("20.00"), resultado.getDetalles().get(0).getSubTotal()),
                () -> assertEquals(new BigDecimal("20.00"), resultado.getTotal()),
                () -> assertEquals(List.of("AAAA-1111-BBBB", "CCCC-2222-DDDD"),
                        resultado.getDetalles().get(0).getClaves()),
                () -> assertEquals(EstadoClave.VENDIDA, primeraClave.getEstado()),
                () -> assertEquals(EstadoClave.VENDIDA, segundaClave.getEstado())
        );
    }

    @Test
    void rechazaCompraSinItemsAntesDePersistir() {
        assertCompraInvalida(compraConItems(null));
    }

    @Test
    void rechazaCompraVaciaAntesDePersistir() {
        assertCompraInvalida(compraConItems(List.of()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rechazaCantidadInvalidaAntesDePersistirNingunItem(Integer cantidad) {
        assertCompraInvalida(compraConItems(List.of(item(1, 1), item(2, cantidad))));
    }

    @Test
    void rechazaItemNuloAntesDePersistir() {
        assertCompraInvalida(compraConItems(Arrays.asList(item(1, 1), null)));
    }

    @Test
    void rechazaItemSinProductoAntesDePersistir() {
        assertCompraInvalida(compraConItems(List.of(item(null, 1))));
    }

    @Test
    void rechazaCompraSinMetodoPagoAntesDePersistir() {
        RealizarCompraDTO compra = compraConItems(List.of(item(1, 1)));
        compra.setMetodoPagoId(null);

        assertCompraInvalida(compra);
    }

    @Test
    void rechazaCompraNulaAntesDePersistir() {
        assertCompraInvalida(null);
    }

    @Test
    void agrupaProductosRepetidosYEntregaUnaClaveDistintaPorUnidad() {
        prepararCompraValida(3);
        when(ofertaService.obtenerDescuentoActivo(1)).thenReturn(BigDecimal.ZERO);

        CompraResponseDTO resultado = compraService.procesarCompra(
                compraConItems(List.of(item(1, 1), item(1, 2))), 1);

        assertAll(
                () -> assertEquals(1, resultado.getDetalles().size()),
                () -> assertEquals(3, resultado.getDetalles().get(0).getCantidad()),
                () -> assertEquals(3, resultado.getDetalles().get(0).getClaves().stream().distinct().count()),
                () -> assertEquals(new BigDecimal("59.97"), resultado.getTotal())
        );
        verify(claveActivacionRepository).findByProductoIdAndEstado(
                1, EstadoClave.DISPONIBLE, PageRequest.of(0, 3));
    }

    @Test
    void noCobraElPrecioCompletoSiFallaLaConsultaDeDescuentos() {
        prepararCompraValida(1);
        IllegalStateException fallo = new IllegalStateException("Servicio de ofertas no disponible");
        when(ofertaService.obtenerDescuentoActivo(1)).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class,
                () -> compraService.procesarCompra(compraConItems(List.of(item(1, 1))), 1)));

        verifyNoInteractions(detalleCompraRepository);
        verify(claveActivacionRepository, never()).save(any(ClaveActivacion.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "100.01"})
    void rechazaDescuentosQueProducirianCobrosInvalidos(String descuento) {
        prepararCompraValida(1);
        when(ofertaService.obtenerDescuentoActivo(1)).thenReturn(new BigDecimal(descuento));

        assertThrows(IllegalArgumentException.class,
                () -> compraService.procesarCompra(compraConItems(List.of(item(1, 1))), 1));

        verifyNoInteractions(detalleCompraRepository);
        verify(claveActivacionRepository, never()).save(any(ClaveActivacion.class));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-0.01"})
    void rechazaUnProductoSinPrecioValido(String precio) {
        Producto producto = prepararCompraValida(1);
        producto.setPrecio(precio == null ? null : new BigDecimal(precio));

        assertThrows(IllegalArgumentException.class,
                () -> compraService.procesarCompra(compraConItems(List.of(item(1, 1))), 1));

        verifyNoInteractions(detalleCompraRepository);
        verify(claveActivacionRepository, never()).save(any(ClaveActivacion.class));
    }

    @Test
    void permiteDescuentoDelCienPorCientoSinImportesNegativos() {
        prepararCompraValida(1);
        when(ofertaService.obtenerDescuentoActivo(1)).thenReturn(new BigDecimal("100"));

        CompraResponseDTO resultado = compraService.procesarCompra(compraConItems(List.of(item(1, 1))), 1);

        assertEquals(new BigDecimal("0.00"), resultado.getTotal());
        assertEquals(1, resultado.getDetalles().get(0).getClaves().size());
    }

    @Test
    void rechazaDesbordamientoDeCantidadesRepetidasAntesDePersistir() {
        assertCompraInvalida(compraConItems(List.of(item(1, Integer.MAX_VALUE), item(1, 1))));
    }

    @Test
    void reservaStockEnOrdenDeProductoAunqueElCarritoLlegueInvertido() {
        prepararCompraValida(1);
        Producto segundo = new Producto();
        segundo.setId(2);
        segundo.setNombre("Segundo juego");
        segundo.setPrecio(new BigDecimal("5.00"));
        when(productoRepository.findById(2)).thenReturn(Optional.of(segundo));
        when(claveActivacionRepository.findByProductoIdAndEstado(2, EstadoClave.DISPONIBLE, PageRequest.of(0, 1)))
                .thenReturn(List.of(claveDisponible("CCCC-2222-DDDD", segundo)));
        when(ofertaService.obtenerDescuentoActivo(1)).thenReturn(BigDecimal.ZERO);
        when(ofertaService.obtenerDescuentoActivo(2)).thenReturn(BigDecimal.ZERO);

        compraService.procesarCompra(compraConItems(List.of(item(2, 1), item(1, 1))), 1);

        var orden = org.mockito.Mockito.inOrder(claveActivacionRepository);
        orden.verify(claveActivacionRepository).findByProductoIdAndEstado(
                1, EstadoClave.DISPONIBLE, PageRequest.of(0, 1));
        orden.verify(claveActivacionRepository).findByProductoIdAndEstado(
                2, EstadoClave.DISPONIBLE, PageRequest.of(0, 1));
    }

    private Producto prepararCompraValida(int cantidad) {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(1);
        metodoPago.setNombre("Tarjeta");
        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("Juego");
        producto.setPrecio(new BigDecimal("19.99"));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(metodoPago));
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        List<ClaveActivacion> claves = java.util.stream.IntStream.range(0, cantidad)
                .mapToObj(i -> claveDisponible("AAAA-1111-BBB" + i, producto)).toList();
        when(claveActivacionRepository.findByProductoIdAndEstado(
                1, EstadoClave.DISPONIBLE, PageRequest.of(0, cantidad))).thenReturn(claves);
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            compra.setId(1);
            return compra;
        });
        when(detalleCompraRepository.save(any(DetalleCompra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return producto;
    }

    private void assertCompraInvalida(RealizarCompraDTO compra) {
        assertThrows(IllegalArgumentException.class, () -> compraService.procesarCompra(compra, 1));
        verifyNoInteractions(compraRepository, detalleCompraRepository, claveActivacionRepository);
    }

    private static RealizarCompraDTO compraConItems(List<DetalleItemDTO> items) {
        RealizarCompraDTO compra = new RealizarCompraDTO();
        compra.setMetodoPagoId(1);
        compra.setItems(items);
        return compra;
    }

    private static DetalleItemDTO item(Integer productoId, Integer cantidad) {
        DetalleItemDTO item = new DetalleItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        return item;
    }

    private static ClaveActivacion claveDisponible(String codigo, Producto producto) {
        ClaveActivacion clave = new ClaveActivacion();
        clave.setCodigo(codigo);
        clave.setProducto(producto);
        clave.setEstado(EstadoClave.DISPONIBLE);
        return clave;
    }
}
