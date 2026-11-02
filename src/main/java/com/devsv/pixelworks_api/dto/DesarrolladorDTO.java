package com.devsv.pixelworks_api.dto;

import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesarrolladorDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private TipoDesarrollador tipo;
    private String pais;
}