package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/** Guarda las imagenes en el volumen persistente /app/uploads de Docker. */
@Service
public class ImagenService {
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp"
    );
    private final Path directorio;

    public ImagenService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.directorio = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new IllegalStateException("No se puede preparar el directorio de imagenes", e);
        }
    }

    public String guardar(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) return null;
        String extension = EXTENSIONES.get(imagen.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WEBP.");
        }
        String nombre = UUID.randomUUID() + extension;
        Path destino = directorio.resolve(nombre);
        try (var entrada = imagen.getInputStream()) {
            Files.copy(entrada, destino);
        } catch (IOException e) {
            // Nunca conservar archivos a medio subir.
            try { Files.deleteIfExists(destino); } catch (IOException ignored) { }
            throw new IllegalStateException("No fue posible guardar la imagen", e);
        }
        return nombre;
    }

    public void eliminarSiEsNuestra(String nombre) {
        if (nombre == null || !nombre.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) return;
        try { Files.deleteIfExists(directorio.resolve(nombre)); }
        catch (IOException ignored) { /* No ocultar la excepcion original de guardado */ }
    }

    public Resource obtener(String nombre) {
        if (nombre == null || !nombre.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ResourceNotFoundException("Imagen no encontrada.");
        }
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.startsWith(directorio) || !Files.isRegularFile(archivo)) {
            throw new ResourceNotFoundException("Imagen no encontrada.");
        }
        try { return new UrlResource(archivo.toUri()); }
        catch (Exception e) { throw new ResourceNotFoundException("Imagen no encontrada."); }
    }

    public MediaType tipoDe(String nombre) {
        if (nombre.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (nombre.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}
