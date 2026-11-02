package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import com.devsv.pixelworks_api.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OfertaPersistenceTest {
    @Autowired OfertaService servicio;
    @Autowired OfertaRepository ofertas;
    @Autowired OfertaProductoRepository asignaciones;
    @Autowired CategoriaRepository categorias;
    @Autowired DesarrolladorRepository desarrolladores;
    @Autowired ProductoRepository productos;
    @Autowired EntityManager entityManager;

    @Test
    void eliminarOfertaAsignadaNoViolaClaveForaneaNiEliminaProducto() {
        Categoria categoria = categorias.save(new Categoria(null, "Categoría oferta", "Prueba"));
        Desarrollador desarrollador = desarrolladores.save(new Desarrollador(
                null, "Estudio oferta", "Prueba", TipoDesarrollador.I, "SV"));
        Producto producto = productos.save(new Producto(null, "Producto oferta", "Prueba", LocalDate.now(),
                BigDecimal.TEN, null, true, categoria, desarrollador));
        Oferta oferta = ofertas.save(new Oferta(null, "Oferta asignada", BigDecimal.TEN,
                LocalDate.now(), LocalDate.now()));
        OfertaProducto asignacion = asignaciones.saveAndFlush(new OfertaProducto(null, oferta, producto));
        Integer ofertaId = oferta.getId();
        Integer asignacionId = asignacion.getId();
        Integer productoId = producto.getId();

        servicio.eliminar(ofertaId);
        entityManager.flush();
        entityManager.clear();

        assertFalse(ofertas.existsById(ofertaId));
        assertFalse(asignaciones.existsById(asignacionId));
        assertTrue(productos.existsById(productoId));
    }

    @Test
    void crearConIdAjenoGeneraOfertaNuevaSinSobrescribirLaExistente() {
        Oferta original = ofertas.saveAndFlush(new Oferta(null, "Original", BigDecimal.TEN,
                LocalDate.now(), LocalDate.now()));
        Integer originalId = original.getId();
        OfertaDTO solicitud = new OfertaDTO(originalId, "Nueva", BigDecimal.ZERO,
                LocalDate.now(), LocalDate.now());

        OfertaDTO resultado = servicio.guardar(solicitud);
        entityManager.flush();
        entityManager.clear();

        assertNotEquals(originalId, resultado.getId());
        assertEquals("Original", ofertas.findById(originalId).orElseThrow().getNombre());
        assertEquals("Nueva", ofertas.findById(resultado.getId()).orElseThrow().getNombre());
    }
}
