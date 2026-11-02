package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.mappers.ProductoMapper;
import com.devsv.pixelworks_api.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {
    @Test
    void descripcionDemasiadoLargaSeRechazaAntesDeAccederALaBase() {
        ProductoDTO dto = productoValido();
        dto.setDescripcion("a".repeat(101));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, dto));
        verifyNoInteractions(productos, categorias, desarrolladores, claves, ofertas);
    }

    @Test
    void imagenDemasiadoLargaSeRechazaAntesDeAccederALaBase() {
        ProductoDTO dto = productoValido();
        dto.setImagen("a".repeat(256));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        verifyNoInteractions(productos, categorias, desarrolladores, claves, ofertas);
    }

    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final DesarrolladorRepository desarrolladores = mock(DesarrolladorRepository.class);
    private final ClaveActivacionRepository claves = mock(ClaveActivacionRepository.class);
    private final OfertaProductoRepository ofertas = mock(OfertaProductoRepository.class);
    private final ProductoService servicio = new ProductoService(productos, categorias, desarrolladores,
            claves, ofertas, Mappers.getMapper(ProductoMapper.class));

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-0.01", "100000000", "1.001"})
    void rechazaPrecioInvalidoAntesDePersistir(String precio) {
        ProductoDTO dto = productoValido();
        dto.setPrecio(precio == null ? null : new BigDecimal(precio));

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, dto));
        verifyNoInteractions(productos, categorias, desarrolladores, claves, ofertas);
    }

    @Test
    void crearRequiereCategoriaYDesarrollador() {
        ProductoDTO dto = productoValido();
        dto.setCategoriaId(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        dto.setCategoriaId(1);
        dto.setDesarrolladorId(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        verifyNoInteractions(productos, categorias, desarrolladores);
    }

    @Test
    void actualizarRechazaNombreDeOtroProductoSinModificarElExistente() {
        Producto existente = new Producto();
        existente.setNombre("Original");
        when(productos.findById(1)).thenReturn(Optional.of(existente));
        when(productos.existsByNombreAndIdNot("Juego", 1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, productoValido()));

        org.junit.jupiter.api.Assertions.assertEquals("Original", existente.getNombre());
        verify(productos, never()).save(any());
    }

    private ProductoDTO productoValido() {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre("Juego");
        dto.setPrecio(new BigDecimal("19.99"));
        dto.setCategoriaId(1);
        dto.setDesarrolladorId(1);
        return dto;
    }
}
