package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.auth.LoginRequestDTO;
import com.devsv.pixelworks_api.dto.auth.LoginResponseDTO;
import com.devsv.pixelworks_api.dto.auth.RegistroUsuarioDTO;

public interface IAuthService {
    LoginResponseDTO registrarUsuario(RegistroUsuarioDTO dto);
    // El método login lo implementaremos exacto cuando pasemos el JWT
    LoginResponseDTO login(LoginRequestDTO dto);
}