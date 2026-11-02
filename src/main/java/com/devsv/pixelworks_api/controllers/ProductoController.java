package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.interfaces.IProductoService;
import com.devsv.pixelworks_api.services.ImagenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final IProductoService productoService;
    private final ImagenService imagenService;

    public ProductoController(IProductoService productoService, ImagenService imagenService) {
        this.productoService = productoService;
        this.imagenService = imagenService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // Sigue aceptando JSON: no rompe las aplicaciones que ya consumen la API.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<ProductoDTO> guardar(@RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(dto));
    }

    // Igual que AutoParts 9_2: multipart con partes 'dto' (JSON) e 'imagen' (archivo).
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<ProductoDTO> guardarConImagen(
            @RequestPart("dto") ProductoDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        String nombreNuevo = imagenService.guardar(imagen);
        if (nombreNuevo != null) dto.setImagen(nombreNuevo);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(dto));
        } catch (RuntimeException e) {
            imagenService.eliminarSiEsNuestra(nombreNuevo);
            throw e;
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Integer id, @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<ProductoDTO> actualizarConImagen(
            @PathVariable Integer id,
            @RequestPart("dto") ProductoDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        String nombreNuevo = imagenService.guardar(imagen);
        if (nombreNuevo != null) dto.setImagen(nombreNuevo);
        try {
            return ResponseEntity.ok(productoService.actualizar(id, dto));
        } catch (RuntimeException e) {
            imagenService.eliminarSiEsNuestra(nombreNuevo);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DESARROLLADOR', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
