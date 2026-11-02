package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.auth.LoginRequestDTO;
import com.devsv.pixelworks_api.dto.auth.LoginResponseDTO;
import com.devsv.pixelworks_api.dto.auth.RegistroUsuarioDTO;
import com.devsv.pixelworks_api.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> registrar(@RequestBody RegistroUsuarioDTO dto) {
        return new ResponseEntity<>(authService.registrarUsuario(dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return new ResponseEntity<>(authService.login(dto), HttpStatus.OK);
    }
}