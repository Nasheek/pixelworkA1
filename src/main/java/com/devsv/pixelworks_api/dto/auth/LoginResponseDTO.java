package com.devsv.pixelworks_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String correo;
    private String nombre;
    private String rol;
}