package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.entities.OfertaProducto;
import com.devsv.pixelworks_api.mappers.OfertaMapper;
import com.devsv.pixelworks_api.repository.OfertaProductoRepository;
import com.devsv.pixelworks_api.repository.OfertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OfertaServiceTest {
    private final OfertaRepository ofertas = mock(OfertaRepository.class);
    private final OfertaProductoRepository relaciones = mock(OfertaProductoRepository.class);
    private final OfertaService servicio = new OfertaService(
            ofertas, relaciones, Mappers.getMapper(OfertaMapper.class));

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-0.01", "100.01", "1000", "12.345"})
    void rechazaDescuentoInvalidoAlCrearYActualizar(String descuento) {
        OfertaDTO dto = ofertaValida();
        dto.setPorcentajeDescuento(descuento == null ? null : new BigDecimal(descuento));

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, dto));
        verifyNoInteractions(ofertas, relaciones);
    }

    @Test
    void rechazaFechasFaltantesOInvertidas() {
        OfertaDTO dto = ofertaValida();
        dto.setFechaInicio(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        dto.setFechaInicio(LocalDate.of(2026, 1, 3));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(dto));
        dto.setFechaFin(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(1, dto));
        verifyNoInteractions(ofertas);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "100", "12.50"})
    void aceptaExtremosYPorcentajesValidos(String descuento) {
        OfertaDTO dto = ofertaValida();
        dto.setPorcentajeDescuento(new BigDecimal(descuento));
        when(ofertas.save(any(Oferta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(dto.getPorcentajeDescuento(), servicio.guardar(dto).getPorcentajeDescuento());
    }

    @Test
    void eliminaAsignacionesAntesDeEliminarOferta() {
        List<OfertaProducto> asignaciones = List.of(new OfertaProducto());
        when(ofertas.existsById(7)).thenReturn(true);
        when(relaciones.findByOfertaId(7)).thenReturn(asignaciones);

        servicio.eliminar(7);

        var orden = inOrder(relaciones, ofertas);
        orden.verify(relaciones).deleteAll(asignaciones);
        orden.verify(relaciones).flush();
        orden.verify(ofertas).deleteById(7);
    }

    @Test
    void actualizacionConservaIdentidadDeLaRuta() {
        Oferta existente = new Oferta();
        existente.setId(7);
        OfertaDTO dto = ofertaValida();
        dto.setId(99);
        when(ofertas.findById(7)).thenReturn(Optional.of(existente));
        when(ofertas.save(any(Oferta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(7, servicio.actualizar(7, dto).getId());
    }

    private OfertaDTO ofertaValida() {
        return new OfertaDTO(null, "Promoción", new BigDecimal("10"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
    }
}
