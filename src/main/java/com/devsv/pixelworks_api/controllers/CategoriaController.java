package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.CategoriaDTO;
import com.devsv.pixelworks_api.interfaces.ICategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ICategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<CategoriaDTO> guardar(@RequestBody CategoriaDTO dto) {
        CategoriaDTO nuevaCategoria = categoriaService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable Integer id, @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
