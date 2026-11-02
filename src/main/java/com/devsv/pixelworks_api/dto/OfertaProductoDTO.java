package com.devsv.pixelworks_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfertaProductoDTO {
    private Integer id;
    private Integer ofertaId;
    private Integer productoId;
}