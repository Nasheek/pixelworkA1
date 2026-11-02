package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImagenServiceTest {
    @TempDir Path uploads;

    @Test
    void guardaYRecuperaImagenEnDisco() throws Exception {
        ImagenService servicio = new ImagenService(uploads.toString());
        var imagen = new MockMultipartFile("imagen", "foto.png", "image/png", new byte[]{1, 2, 3});
        String nombre = servicio.guardar(imagen);
        assertTrue(nombre.endsWith(".png"));
        assertTrue(Files.isRegularFile(uploads.resolve(nombre)));
        assertArrayEquals(new byte[]{1, 2, 3}, servicio.obtener(nombre).getInputStream().readAllBytes());
        servicio.eliminarSiEsNuestra(nombre);
        assertFalse(Files.exists(uploads.resolve(nombre)));
    }

    @Test
    void rechazaImagenesNoPermitidasYRutasFueraDeUploads() {
        ImagenService servicio = new ImagenService(uploads.toString());
        var svg = new MockMultipartFile("imagen", "peligrosa.svg", "image/svg+xml", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(svg));
        assertThrows(ResourceNotFoundException.class, () -> servicio.obtener("../.env"));
    }
}
