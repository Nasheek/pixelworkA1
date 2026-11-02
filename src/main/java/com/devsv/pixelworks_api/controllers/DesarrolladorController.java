package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.DesarrolladorDTO;
import com.devsv.pixelworks_api.interfaces.IDesarrolladorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/desarrolladores")
@RequiredArgsConstructor
public class DesarrolladorController {

    private final IDesarrolladorService desarrolladorService;

    @GetMapping
    public ResponseEntity<List<DesarrolladorDTO>> listarTodos() {
        return ResponseEntity.ok(desarrolladorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesarrolladorDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(desarrolladorService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DesarrolladorDTO> guardar(@RequestBody DesarrolladorDTO dto) {
        DesarrolladorDTO nuevoDesarrollador = desarrolladorService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDesarrollador);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DesarrolladorDTO> actualizar(@PathVariable Integer id, @RequestBody DesarrolladorDTO dto) {
        return ResponseEntity.ok(desarrolladorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        desarrolladorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}