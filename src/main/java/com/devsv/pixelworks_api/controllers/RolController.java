package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.RolDTO;
import com.devsv.pixelworks_api.interfaces.IRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final IRolService rolService;

    @GetMapping
    public ResponseEntity<List<RolDTO>> listarTodos() {
        return ResponseEntity.ok(rolService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RolDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RolDTO> guardar(@RequestBody RolDTO dto) {
        RolDTO nuevoRol = rolService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoRol);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RolDTO> actualizar(@PathVariable Integer id, @RequestBody RolDTO dto) {
        return ResponseEntity.ok(rolService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}