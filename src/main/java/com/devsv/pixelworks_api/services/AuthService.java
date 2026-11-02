package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.auth.LoginRequestDTO;
import com.devsv.pixelworks_api.dto.auth.LoginResponseDTO;
import com.devsv.pixelworks_api.dto.auth.RegistroUsuarioDTO;
import com.devsv.pixelworks_api.entities.Rol;
import com.devsv.pixelworks_api.entities.Usuario;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IAuthService;
import com.devsv.pixelworks_api.repository.RolRepository;
import com.devsv.pixelworks_api.repository.UsuarioRepository;
import com.devsv.pixelworks_api.security.JwtService;
import com.devsv.pixelworks_api.security.UsuarioPrincipal;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private static final String ROL_DEFAULT = "JUGADOR";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public LoginResponseDTO registrarUsuario(RegistroUsuarioDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de registro son obligatorios.");
        }
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (dto.getNombre().length() > 50) {
            throw new IllegalArgumentException("El nombre no puede superar los 50 caracteres.");
        }
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        dto.setCorreo(dto.getCorreo().trim());
        if (dto.getCorreo().length() > 50) {
            throw new IllegalArgumentException("El correo no puede superar los 50 caracteres.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        Rol rolJugador = rolRepository.findByNombre(ROL_DEFAULT)
                .orElseThrow(() -> new ResourceNotFoundException("Error: No existe el rol '" + ROL_DEFAULT + "' en la base de datos."));

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(rolJugador);

        usuario = usuarioRepository.save(usuario);

        // Devuelve el token inmediatamente después de registrarse
        return generarRespuestaConToken(usuario);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        if (dto == null || dto.getCorreo() == null || dto.getCorreo().isBlank()
                || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("El correo y la contraseña son obligatorios.");
        }
        Authentication authentication;
        try {
            // El manager internamente llama a tu CustomUserDetailsService
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getCorreo().trim(), dto.getPassword()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos");
        } catch (DisabledException e) {
            throw new IllegalArgumentException("El usuario está inactivo.");
        }

        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        return generarRespuestaConToken(principal);
    }

    // Método auxiliar para no repetir código al generar el token
    private LoginResponseDTO generarRespuestaConToken(Usuario usuario) {
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtService.generarToken(principal);
        return new LoginResponseDTO(token, principal.getCorreo(), principal.getNombre(), principal.getRol());
    }

    // Sobrecarga para el login
    private LoginResponseDTO generarRespuestaConToken(UsuarioPrincipal principal) {
        String token = jwtService.generarToken(principal);
        return new LoginResponseDTO(token, principal.getCorreo(), principal.getNombre(), principal.getRol());
    }
}
