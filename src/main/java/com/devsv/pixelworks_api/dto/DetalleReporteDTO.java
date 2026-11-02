package com.devsv.pixelworks_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DetalleReporteDTO {
    private String nombre;
    private long cantidad;
    private BigDecimal ingresos;
}