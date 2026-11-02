package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.MetodoPagoDTO;
import com.devsv.pixelworks_api.interfaces.IMetodoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final IMetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<MetodoPagoDTO>> listarTodos() {
        return ResponseEntity.ok(metodoPagoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(metodoPagoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodoPagoDTO> guardar(@RequestBody MetodoPagoDTO dto) {
        MetodoPagoDTO nuevoMetodo = metodoPagoService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMetodo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodoPagoDTO> actualizar(@PathVariable Integer id, @RequestBody MetodoPagoDTO dto) {
        return ResponseEntity.ok(metodoPagoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        metodoPagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}