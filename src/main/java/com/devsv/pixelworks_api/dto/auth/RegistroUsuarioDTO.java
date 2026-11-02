package com.devsv.pixelworks_api.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroUsuarioDTO {
    private String nombre;
    private String correo;
    private String password;
}