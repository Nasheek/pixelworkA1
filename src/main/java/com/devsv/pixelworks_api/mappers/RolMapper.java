package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.RolDTO;
import com.devsv.pixelworks_api.entities.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RolMapper {
    RolDTO toDTO(Rol rol);
    @Mapping(target = "id", ignore = true)
    Rol toEntity(RolDTO dto);
}
