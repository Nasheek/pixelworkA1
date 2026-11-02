package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ResumenFinancieroDTO {
    private BigDecimal ingresosTotales;
    private long cantidadVentas;
    private List<DetalleReporteDTO> detalles;
}