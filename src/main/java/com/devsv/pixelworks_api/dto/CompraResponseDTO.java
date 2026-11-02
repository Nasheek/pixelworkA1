package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CompraResponseDTO {
    private Integer id;
    private LocalDateTime fechaVenta;
    private String metodoPagoNombre;
    private BigDecimal total;
    private List<DetalleCompraResponseDTO> detalles;
}