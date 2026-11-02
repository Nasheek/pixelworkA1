package com.devsv.pixelworks_api.dto;

import com.devsv.pixelworks_api.enums.EstadoClave;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaveActivacionDTO {
    private Integer id;
    private Integer productoId;
    private String codigo;
    private EstadoClave estado;
    private Integer detalleCompraId;
}