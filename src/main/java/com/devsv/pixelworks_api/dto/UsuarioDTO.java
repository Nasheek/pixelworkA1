package com.devsv.pixelworks_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {


    private Integer id;
    private String nombre;
    private String correo;
    private String estado;
    private Integer idRol;
    private String rol;

}
