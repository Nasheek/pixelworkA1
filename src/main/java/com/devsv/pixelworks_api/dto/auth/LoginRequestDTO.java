package com.devsv.pixelworks_api.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    private String correo;
    private String password;
}