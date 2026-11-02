package com.devsv.pixelworks_api.config;

import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import com.devsv.pixelworks_api.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Los datos de demostracion se crean solamente si APP_DEMO_ENABLED=true. */
@Configuration
public class InicializadorDatos {

    @Bean
    ApplicationRunner prepararDatos(
            RolRepository roles, UsuarioRepository usuarios,
            CategoriaRepository categorias, DesarrolladorRepository desarrolladores,
            ProductoRepository productos, ClaveActivacionRepository claves,
            MetodoPagoRepository metodos, PasswordEncoder encoder,
            @Value("${app.demo.enabled:false}") boolean demo,
            @Value("${app.admin.email:}") String correoAdmin,
            @Value("${app.admin.password:}") String claveAdmin) {
        return args -> {
            roles.findByNombre("JUGADOR").orElseGet(() -> roles.save(new Rol(null, "JUGADOR")));
            if (!demo) return;
            Rol admin = roles.findByNombre("ADMIN").orElseGet(() -> roles.save(new Rol(null, "ADMIN")));
            roles.findByNombre("DESARROLLADOR")
                    .orElseGet(() -> roles.save(new Rol(null, "DESARROLLADOR")));

            // Admin solo de laboratorio, con credenciales generadas localmente por INICIAR.cmd.
            if (correoAdmin.isBlank() || claveAdmin.length() < 12) {
                throw new IllegalStateException("Para el modo demo configure APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD (12+ caracteres).");
            }
            Usuario usuarioDemo = usuarios.findByCorreo(correoAdmin).orElse(null);
            if (usuarioDemo == null) {
                Usuario usuario = new Usuario();
                usuario.setNombre("Administrador Pixelworks");
                usuario.setCorreo(correoAdmin);
                usuario.setPassword(encoder.encode(claveAdmin));
                usuario.setRol(admin);
                usuario.setEstado(EstadoUsuario.ACTIVO);
                usuarios.save(usuario);
            } else if ("admin@pixelworks.local".equalsIgnoreCase(correoAdmin)
                    && usuarioDemo.getRol() != null
                    && admin.getId().equals(usuarioDemo.getRol().getId())
                    && !encoder.matches(claveAdmin, usuarioDemo.getPassword())) {
                // Solo para el usuario DEMO reservado: si se regenera .env en otra
                // carpeta, permite iniciar sesion con la clave de prueba actual.
                // No cambia contrasenas de usuarios normales o administradores reales.
                usuarioDemo.setPassword(encoder.encode(claveAdmin));
                usuarios.save(usuarioDemo);
            }

            if (productos.count() > 0) return; // No alterar un catalogo existente.

            Categoria accion = categorias.save(new Categoria(null, "Accion Demo", "Juegos de accion de prueba"));
            Categoria aventuras = categorias.save(new Categoria(null, "Aventuras Demo", "Juegos de aventuras de prueba"));
            Desarrollador estudio = desarrolladores.save(new Desarrollador(
                    null, "Pixelworks Demo", "Estudio de demostracion", TipoDesarrollador.I, "SV"));

            if (!metodos.existsByNombre("Tarjeta Demo")) {
                metodos.save(new MetodoPago(null, "Tarjeta Demo"));
            }

            List<Producto> ejemplos = List.of(
                    productos.save(new Producto(null, "Aventura del Bosque Demo", "Videojuego de prueba", LocalDate.of(2025, 1, 1),
                            new BigDecimal("12.99"), null, true, aventuras, estudio)),
                    productos.save(new Producto(null, "Carrera Estelar Demo", "Videojuego de prueba", LocalDate.of(2025, 2, 1),
                            new BigDecimal("19.99"), null, true, accion, estudio)),
                    productos.save(new Producto(null, "Explorador Pixel Demo", "Videojuego de prueba", LocalDate.of(2025, 3, 1),
                            new BigDecimal("8.50"), null, true, aventuras, estudio))
            );
            for (Producto producto : ejemplos) {
                for (int i = 0; i < 3; i++) {
                    claves.save(new ClaveActivacion(null, producto,
                            "DEMO-" + UUID.randomUUID(), EstadoClave.DISPONIBLE, null));
                }
            }
        };
    }
}
