package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.interfaces.ICompraService;
import com.devsv.pixelworks_api.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final ICompraService compraService;

    @PostMapping
    @PreAuthorize("hasRole('JUGADOR')")
    public ResponseEntity<CompraResponseDTO> realizarCompra(
            @RequestBody RealizarCompraDTO dto,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogueado) {

        CompraResponseDTO factura = compraService.procesarCompra(dto, usuarioLogueado.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(factura);
    }

    @GetMapping("/mis-juegos")
    @PreAuthorize("hasRole('JUGADOR')")
    public ResponseEntity<List<CompraResponseDTO>> obtenerMisJuegos(
            @AuthenticationPrincipal AuthenticatedUser usuarioLogueado) {

        List<CompraResponseDTO> historial = compraService.obtenerMisCompras(usuarioLogueado.id());
        return ResponseEntity.ok(historial);
    }
}