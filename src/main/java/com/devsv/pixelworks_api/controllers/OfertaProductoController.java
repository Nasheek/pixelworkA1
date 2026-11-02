package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.OfertaProductoDTO;
import com.devsv.pixelworks_api.interfaces.IOfertaProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ofertas-productos")
@RequiredArgsConstructor
public class OfertaProductoController {

    private final IOfertaProductoService ofertaProductoService;

    @GetMapping
    public ResponseEntity<List<OfertaProductoDTO>> listarTodos() {
        return ResponseEntity.ok(ofertaProductoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaProductoDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ofertaProductoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<OfertaProductoDTO> guardar(@RequestBody OfertaProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaProductoService.guardar(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ofertaProductoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}