package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfertaMapper {
    OfertaDTO toDTO(Oferta oferta);
    @Mapping(target = "id", ignore = true)
    Oferta toEntity(OfertaDTO dto);
}
