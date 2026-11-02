package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RealizarCompraDTO {
    private Integer metodoPagoId;
    private List<DetalleItemDTO> items;
}