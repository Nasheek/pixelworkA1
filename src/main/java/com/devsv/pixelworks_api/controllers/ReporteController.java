package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.dto.ProductoAlertaDTO;
import com.devsv.pixelworks_api.interfaces.IReporteService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReporteController {

    private final IReporteService reporteService;

    @GetMapping("/dashboard/finanzas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumenFinancieroDTO> verFinanzas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String productoIds) { // Recibimos como String para separar las comas

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        // Convertimos la cadena "1,2,3" en una lista de Integers de forma segura
        List<Integer> idsList = null;
        if (productoIds != null && !productoIds.trim().isEmpty()) {
            idsList = Arrays.stream(productoIds.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(reporteService.obtenerFinanzas(inicio, fin, idsList));
    }

    @GetMapping("/dashboard/alertas/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoAlertaDTO>> verAlertasStock() {
        return ResponseEntity.ok(reporteService.obtenerJuegosSinStock());
    }

    @GetMapping("/reportes/ventas-totales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getVentasTotales() {

        ResumenFinancieroDTO finanzas = reporteService.obtenerFinanzas(null, null, null);

        BigDecimal ingresos = finanzas.getIngresosTotales() != null ? finanzas.getIngresosTotales() : BigDecimal.ZERO;
        long cantidad = finanzas.getCantidadVentas();

        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (cantidad > 0) {
            ticketPromedio = ingresos.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("cantidadVentas", cantidad);
        respuesta.put("ingresosTotales", ingresos);
        respuesta.put("ticketPromedio", ticketPromedio);
        respuesta.put("ultimaVenta", null);

        return ResponseEntity.ok(respuesta);
    }
}
