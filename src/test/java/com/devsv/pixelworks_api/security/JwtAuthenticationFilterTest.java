package com.devsv.pixelworks_api.security;

import com.devsv.pixelworks_api.entities.Rol;
import com.devsv.pixelworks_api.entities.Usuario;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void tokenVacioContinuaSinAutenticacion(String token) {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "ClaveDePruebaJwtDeAlMenos32Caracteres0123456789");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, mock(CustomUserDetailsService.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean cadenaEjecutada = new AtomicBoolean();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("usuario", null, "ROLE_JUGADOR"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, (req, res) -> {
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            cadenaEjecutada.set(true);
        }));

        assertTrue(cadenaEjecutada.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenConRolAnteriorUsaPermisosActualesDeLaBaseDeDatos() throws Exception {
        JwtService jwtService = jwtService();
        CustomUserDetailsService usuarios = mock(CustomUserDetailsService.class);
        UsuarioPrincipal principalActual = principal(1, "JUGADOR", EstadoUsuario.ACTIVO);
        when(usuarios.loadUserByUsername("usuario@example.com")).thenReturn(principalActual);
        String token = jwtService.generarToken(principal(1, "ADMIN", EstadoUsuario.ACTIVO));

        filtrar(jwtService, usuarios, token);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("JUGADOR", ((AuthenticatedUser) authentication.getPrincipal()).rol());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_JUGADOR")));
        assertTrue(authentication.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void tokenValidoDeUsuarioDesactivadoNoAutentica() throws Exception {
        JwtService jwtService = jwtService();
        CustomUserDetailsService usuarios = mock(CustomUserDetailsService.class);
        when(usuarios.loadUserByUsername("usuario@example.com"))
                .thenReturn(principal(1, "JUGADOR", EstadoUsuario.INACTIVO));

        filtrar(jwtService, usuarios,
                jwtService.generarToken(principal(1, "JUGADOR", EstadoUsuario.ACTIVO)));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenDeUsuarioEliminadoNoAutentica() throws Exception {
        JwtService jwtService = jwtService();
        CustomUserDetailsService usuarios = mock(CustomUserDetailsService.class);
        when(usuarios.loadUserByUsername("usuario@example.com"))
                .thenThrow(new UsernameNotFoundException("Usuario eliminado"));

        filtrar(jwtService, usuarios,
                jwtService.generarToken(principal(1, "JUGADOR", EstadoUsuario.ACTIVO)));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenNoSePuedeReutilizarConOtroUsuarioQueTieneElMismoCorreo() throws Exception {
        JwtService jwtService = jwtService();
        CustomUserDetailsService usuarios = mock(CustomUserDetailsService.class);
        when(usuarios.loadUserByUsername("usuario@example.com"))
                .thenReturn(principal(2, "JUGADOR", EstadoUsuario.ACTIVO));

        filtrar(jwtService, usuarios,
                jwtService.generarToken(principal(1, "JUGADOR", EstadoUsuario.ACTIVO)));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void filtrar(JwtService jwtService, CustomUserDetailsService usuarios, String token) throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, usuarios);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        AtomicBoolean cadenaEjecutada = new AtomicBoolean();
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> cadenaEjecutada.set(true));
        assertTrue(cadenaEjecutada.get());
    }

    private JwtService jwtService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "ClaveDePruebaJwtDeAlMenos32Caracteres0123456789");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60000L);
        return jwtService;
    }

    private UsuarioPrincipal principal(int id, String rol, EstadoUsuario estado) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setCorreo("usuario@example.com");
        usuario.setNombre("Usuario");
        usuario.setEstado(estado);
        usuario.setRol(new Rol(1, rol));
        return new UsuarioPrincipal(usuario);
    }
}
