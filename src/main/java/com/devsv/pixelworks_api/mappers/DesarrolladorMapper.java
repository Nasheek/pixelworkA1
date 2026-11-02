package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.DesarrolladorDTO;
import com.devsv.pixelworks_api.entities.Desarrollador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DesarrolladorMapper {
    DesarrolladorDTO toDTO(Desarrollador desarrollador);
    @Mapping(target = "id", ignore = true)
    Desarrollador toEntity(DesarrolladorDTO dto);
}
