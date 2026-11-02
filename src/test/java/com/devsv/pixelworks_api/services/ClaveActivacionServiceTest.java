package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ClaveActivacionDTO;
import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.mappers.ClaveActivacionMapper;
import com.devsv.pixelworks_api.repository.ClaveActivacionRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClaveActivacionServiceTest {
    private final ClaveActivacionRepository claves = mock(ClaveActivacionRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final ClaveActivacionService servicio = new ClaveActivacionService(
            claves, productos, Mappers.getMapper(ClaveActivacionMapper.class));

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rechazaInventarioSinProductoValido(Integer productoId) {
        ClaveActivacionDTO dto = new ClaveActivacionDTO();
        dto.setCodigo("AAAA-1111-BBBB");
        dto.setProductoId(productoId);

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));

        verifyNoInteractions(claves, productos);
    }

    @Test
    void rechazaDatosNulosSinAccederALaBase() {
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(null));

        verifyNoInteractions(claves, productos);
    }

    @Test
    void elClienteNoPuedeSobrescribirUnaClaveVendidaNiAsignarSuComprador() {
        Producto producto = new Producto();
        producto.setId(1);
        ClaveActivacionDTO dto = new ClaveActivacionDTO();
        dto.setId(99);
        dto.setProductoId(1);
        dto.setCodigo(" aaaa-1111-bbbb ");
        dto.setEstado(EstadoClave.VENDIDA);
        dto.setDetalleCompraId(40);
        when(productos.findById(1)).thenReturn(Optional.of(producto));
        when(claves.save(any(ClaveActivacion.class))).thenAnswer(invocation -> {
            ClaveActivacion creada = invocation.getArgument(0);
            assertNull(creada.getId());
            assertNull(creada.getDetalleCompra());
            return creada;
        });

        ClaveActivacionDTO resultado = servicio.guardar(dto);

        assertEquals("AAAA-1111-BBBB", resultado.getCodigo());
        assertEquals(EstadoClave.DISPONIBLE, resultado.getEstado());
        assertNull(resultado.getDetalleCompraId());
    }
}
