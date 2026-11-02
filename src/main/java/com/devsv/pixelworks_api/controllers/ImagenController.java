package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.services.ImagenService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ImagenController {
    private final ImagenService imagenService;

    public ImagenController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    @GetMapping("/images/{nombre:.+}")
    public ResponseEntity<Resource> mostrar(@PathVariable String nombre) {
        return ResponseEntity.ok()
                .contentType(imagenService.tipoDe(nombre))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(imagenService.obtener(nombre));
    }
}
