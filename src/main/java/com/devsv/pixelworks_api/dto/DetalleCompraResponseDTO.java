package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DetalleCompraResponseDTO {
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subTotal;
    private List<String> claves;
}