package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.CambioRolDTO;
import com.devsv.pixelworks_api.dto.UsuarioDTO;
import com.devsv.pixelworks_api.security.AuthenticatedUser;
import com.devsv.pixelworks_api.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {


    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {

        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarRol(
            @PathVariable Integer id,
            @RequestBody CambioRolDTO dto,
            @AuthenticationPrincipal AuthenticatedUser adminLogueado) {

        usuarioService.cambiarRol(
                id,
                dto.getIdRol(),
                adminLogueado.id()
        );

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "El rol del usuario ha sido actualizado correctamente."
                )
        );
    }


}
