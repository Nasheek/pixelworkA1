package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductoDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate anioLanzamiento;
    private BigDecimal precio;
    private String imagen;
    private Integer categoriaId;
    private Integer desarrolladorId;

    // Información comercial
    private Long stock;
    private BigDecimal porcentajeDescuento;
    private BigDecimal precioConDescuento;
}