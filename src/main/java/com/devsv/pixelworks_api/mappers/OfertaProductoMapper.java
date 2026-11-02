package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.OfertaProductoDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.entities.OfertaProducto;
import com.devsv.pixelworks_api.entities.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfertaProductoMapper {

    @Mapping(source = "oferta.id", target = "ofertaId")
    @Mapping(source = "producto.id", target = "productoId")
    OfertaProductoDTO toDTO(OfertaProducto ofertaProducto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "oferta", target = "oferta")
    @Mapping(source = "producto", target = "producto")
    OfertaProducto toEntity(OfertaProductoDTO dto, Oferta oferta, Producto producto);
}