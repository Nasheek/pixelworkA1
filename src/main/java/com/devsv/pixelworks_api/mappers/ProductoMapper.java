package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import com.devsv.pixelworks_api.entities.Desarrollador;
import com.devsv.pixelworks_api.entities.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "desarrollador.id", target = "desarrolladorId")
    ProductoDTO toDTO(Producto producto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.nombre", target = "nombre")
    @Mapping(source = "dto.descripcion", target = "descripcion")
    @Mapping(source = "categoria", target = "categoria")
    @Mapping(source = "desarrollador", target = "desarrollador")
    Producto toEntity(ProductoDTO dto, Categoria categoria, Desarrollador desarrollador);
}