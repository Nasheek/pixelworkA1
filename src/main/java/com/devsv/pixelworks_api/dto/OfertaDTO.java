package com.devsv.pixelworks_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfertaDTO {
    private Integer id;
    private String nombre;
    private BigDecimal porcentajeDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}