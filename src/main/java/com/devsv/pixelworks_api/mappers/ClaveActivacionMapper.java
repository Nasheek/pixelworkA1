package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.ClaveActivacionDTO;
import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.entities.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaveActivacionMapper {

    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "detalleCompra.id", target = "detalleCompraId")
    ClaveActivacionDTO toDTO(ClaveActivacion clave);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.codigo", target = "codigo")
    @Mapping(source = "producto", target = "producto")
    @Mapping(target = "estado", ignore = true) // Se asignará manualmente en el Service
    @Mapping(target = "detalleCompra", ignore = true) // Al crearse, no tiene comprador
    ClaveActivacion toEntity(ClaveActivacionDTO dto, Producto producto);
}