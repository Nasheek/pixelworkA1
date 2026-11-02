package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas")
@RequiredArgsConstructor
public class OfertaController {

    private final IOfertaService oferta;

    @GetMapping
    public ResponseEntity<List<OfertaDTO>> listarTodas() {
        return ResponseEntity.ok(oferta.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(oferta.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<OfertaDTO> guardar(@RequestBody OfertaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oferta.guardar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<OfertaDTO> actualizar(@PathVariable Integer id, @RequestBody OfertaDTO dto) {
        return ResponseEntity.ok(oferta.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        oferta.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}