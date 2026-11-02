package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.auth.LoginRequestDTO;
import com.devsv.pixelworks_api.dto.auth.RegistroUsuarioDTO;
import com.devsv.pixelworks_api.repository.RolRepository;
import com.devsv.pixelworks_api.repository.UsuarioRepository;
import com.devsv.pixelworks_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Test
    void loginNormalizaCorreoYConservaLosEspaciosDeLaContrasena() {
        LoginRequestDTO dto = solicitudLogin();
        dto.setCorreo("  usuario@example.com  ");
        dto.setPassword(" password123 ");
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
        verify(authenticationManager).authenticate(argThat(auth ->
                "usuario@example.com".equals(auth.getPrincipal())
                        && " password123 ".equals(auth.getCredentials())));
    }

    @Test
    void registroCompruebaDuplicadosDespuesDeRecortarElCorreo() {
        RegistroUsuarioDTO dto = solicitudRegistro();
        dto.setCorreo("  usuario@example.com  ");
        when(usuarioRepository.existsByCorreo("usuario@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.registrarUsuario(dto));
        verify(usuarioRepository).existsByCorreo("usuario@example.com");
        verifyNoInteractions(passwordEncoder, rolRepository, jwtService);
    }


    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginDeUsuarioInactivoDevuelveErrorDeSolicitudSinEmitirToken() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new DisabledException("User is disabled"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> authService.login(solicitudLogin()));

        assertEquals("El usuario está inactivo.", error.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginConCredencialesIncorrectasConservaSuMensaje() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> authService.login(solicitudLogin()));

        assertEquals("Correo o contraseña incorrectos", error.getMessage());
        verifyNoInteractions(jwtService);
    }

    private LoginRequestDTO solicitudLogin() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCorreo("usuario@example.com");
        dto.setPassword("password123");
        return dto;
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void registroSinNombreSeRechazaAntesDeGuardar(String nombre) {
        RegistroUsuarioDTO dto = solicitudRegistro();
        dto.setNombre(nombre);

        assertThrows(IllegalArgumentException.class, () -> authService.registrarUsuario(dto));

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder, jwtService);
    }

    @Test
    void registroConNombreDemasiadoLargoSeRechazaAntesDeGuardar() {
        RegistroUsuarioDTO dto = solicitudRegistro();
        dto.setNombre("a".repeat(51));

        assertThrows(IllegalArgumentException.class, () -> authService.registrarUsuario(dto));

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder, jwtService);
    }

    @Test
    void registroConCorreoDemasiadoLargoSeRechazaAntesDeGuardar() {
        RegistroUsuarioDTO dto = solicitudRegistro();
        dto.setCorreo("a".repeat(40) + "@example.com");

        assertThrows(IllegalArgumentException.class, () -> authService.registrarUsuario(dto));

        verifyNoInteractions(usuarioRepository, rolRepository, passwordEncoder, jwtService);
    }

    private RegistroUsuarioDTO solicitudRegistro() {
        RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
        dto.setNombre("Usuario");
        dto.setCorreo("usuario@example.com");
        dto.setPassword("password123");
        return dto;
    }
}
