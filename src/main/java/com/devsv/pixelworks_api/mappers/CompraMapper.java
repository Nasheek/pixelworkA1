package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.dto.DetalleCompraResponseDTO;
import com.devsv.pixelworks_api.entities.Compra;
import com.devsv.pixelworks_api.entities.DetalleCompra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompraMapper {

    @Mapping(source = "metodoPago.nombre", target = "metodoPagoNombre")
    @Mapping(target = "detalles", ignore = true)
    CompraResponseDTO toResponseDTO(Compra compra);

    @Mapping(source = "producto.nombre", target = "productoNombre")
    DetalleCompraResponseDTO toDetalleResponseDTO(DetalleCompra detalle);
}