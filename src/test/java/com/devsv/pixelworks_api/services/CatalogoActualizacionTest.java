package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.*;
import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.mappers.*;
import com.devsv.pixelworks_api.repository.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CatalogoActualizacionTest {
    @Test
    void categoriaNoSePuedeRenombrarComoOtraCategoria() {
        CategoriaRepository repository = mock(CategoriaRepository.class);
        Categoria existente = new Categoria(1, "Original", "Descripción");
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreAndIdNot("Ocupado", 1)).thenReturn(true);
        CategoriaService servicio = new CategoriaService(repository, Mappers.getMapper(CategoriaMapper.class));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizar(1, new CategoriaDTO(null, "Ocupado", "Otra")));

        assertEquals("Original", existente.getNombre());
        verify(repository, never()).save(any());
    }

    @Test
    void desarrolladorNoSePuedeRenombrarComoOtroDesarrollador() {
        DesarrolladorRepository repository = mock(DesarrolladorRepository.class);
        Desarrollador existente = new Desarrollador();
        existente.setNombre("Original");
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreAndIdNot("Ocupado", 1)).thenReturn(true);
        DesarrolladorService servicio = new DesarrolladorService(
                repository, Mappers.getMapper(DesarrolladorMapper.class));
        DesarrolladorDTO dto = new DesarrolladorDTO();
        dto.setNombre("Ocupado");

        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, dto));

        assertEquals("Original", existente.getNombre());
        verify(repository, never()).save(any());
    }

    @Test
    void metodoNoSePuedeRenombrarComoOtroMetodo() {
        MetodoPagoRepository repository = mock(MetodoPagoRepository.class);
        MetodoPago existente = new MetodoPago(1, "Original");
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreAndIdNot("Ocupado", 1)).thenReturn(true);
        MetodoPagoService servicio = new MetodoPagoService(repository, Mappers.getMapper(MetodoPagoMapper.class));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizar(1, new MetodoPagoDTO(null, "Ocupado")));

        assertEquals("Original", existente.getNombre());
        verify(repository, never()).save(any());
    }

    @Test
    void rolNoSePuedeRenombrarComoOtroRol() {
        RolRepository repository = mock(RolRepository.class);
        Rol existente = new Rol(1, "Original");
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.existsByNombreAndIdNot("Ocupado", 1)).thenReturn(true);
        RolService servicio = new RolService(repository, Mappers.getMapper(RolMapper.class));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizar(1, new RolDTO(null, "Ocupado")));

        assertEquals("Original", existente.getNombre());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizarSinCambiarNombreSiguePermitido() {
        CategoriaRepository repository = mock(CategoriaRepository.class);
        Categoria existente = new Categoria(1, "Original", "Descripción");
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);
        CategoriaService servicio = new CategoriaService(repository, Mappers.getMapper(CategoriaMapper.class));

        CategoriaDTO resultado = servicio.actualizar(1, new CategoriaDTO(99, "Original", "Actualizada"));

        assertEquals(1, resultado.getId());
        assertEquals("Actualizada", resultado.getDescripcion());
    }
}
