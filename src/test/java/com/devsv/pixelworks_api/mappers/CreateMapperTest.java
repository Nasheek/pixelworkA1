package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreateMapperTest {
    @Test
    void crearNoAceptaIdentificadoresQuePodrianSobrescribirRegistros() {
        OfertaDTO oferta = new OfertaDTO();
        oferta.setId(10);
        CategoriaDTO categoria = new CategoriaDTO();
        categoria.setId(10);
        DesarrolladorDTO desarrollador = new DesarrolladorDTO();
        desarrollador.setId(10);
        MetodoPagoDTO metodo = new MetodoPagoDTO();
        metodo.setId(10);
        RolDTO rol = new RolDTO();
        rol.setId(10);

        assertAll(
                () -> assertNull(Mappers.getMapper(OfertaMapper.class).toEntity(oferta).getId()),
                () -> assertNull(Mappers.getMapper(CategoriaMapper.class).toEntity(categoria).getId()),
                () -> assertNull(Mappers.getMapper(DesarrolladorMapper.class).toEntity(desarrollador).getId()),
                () -> assertNull(Mappers.getMapper(MetodoPagoMapper.class).toEntity(metodo).getId()),
                () -> assertNull(Mappers.getMapper(RolMapper.class).toEntity(rol).getId())
        );
    }
}
