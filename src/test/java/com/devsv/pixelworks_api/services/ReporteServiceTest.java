package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.repository.CompraRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReporteServiceTest {
    private final CompraRepository compras = mock(CompraRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final ReporteService servicio = new ReporteService(compras, productos);

    @Test
    void rechazaUnIntervaloInvertidoEnLugarDeMostrarUnReporteVacio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 0, 0);
        LocalDateTime fin = inicio.minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerFinanzas(inicio, fin, null));

        verifyNoInteractions(compras);
    }

    @Test
    void sumaImportesYCantidadesDeLosProductosDelReporte() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime fin = inicio.plusDays(1);
        when(compras.obtenerDesgloseVentas(inicio, fin, List.of(1, 2), true)).thenReturn(List.of(
                new Object[]{"Juego uno", 2L, new BigDecimal("19.98")},
                new Object[]{"Juego dos", 1L, new BigDecimal("5.00")}
        ));

        var resultado = servicio.obtenerFinanzas(inicio, fin, List.of(1, 2));

        assertEquals(new BigDecimal("24.98"), resultado.getIngresosTotales());
        assertEquals(3L, resultado.getCantidadVentas());
        assertEquals(2, resultado.getDetalles().size());
    }
}
