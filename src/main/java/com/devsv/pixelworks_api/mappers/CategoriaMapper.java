package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.CategoriaDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDTO(Categoria categoria);
    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaDTO dto);
}
