package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.ClaveActivacionDTO;
import com.devsv.pixelworks_api.interfaces.IClaveActivacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/claves")
@RequiredArgsConstructor
public class ClaveActivacionController {

    private final IClaveActivacionService claveService;

    // Candado: Solo dueños del juego o administradores pueden agregar inventario
    @PostMapping
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<ClaveActivacionDTO> guardar(@RequestBody ClaveActivacionDTO dto) {
        ClaveActivacionDTO nuevaClave = claveService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaClave);
    }

    // Abierto: Cualquiera puede preguntar cuánto stock queda
    @GetMapping("/producto/{productoId}/stock")
    public ResponseEntity<?> contarStock(@PathVariable Integer productoId) {
        long stock = claveService.contarStockDisponible(productoId);

        // Devolvemos un JSON limpio y amigable para React
        return ResponseEntity.ok(Map.of(
                "productoId", productoId,
                "stockDisponible", stock
        ));
    }
}