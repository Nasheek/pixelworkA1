package com.devsv.pixelworks_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtService.validarYExtraerClaims(token);
            String correo = claims.getSubject();
            Integer usuarioId = claims.get("id", Integer.class);
            if (correo == null || correo.isBlank() || usuarioId == null) {
                throw new BadCredentialsException("El token no identifica a un usuario.");
            }

            // El estado y los permisos pueden cambiar antes de que expire el token.
            UsuarioPrincipal principal = (UsuarioPrincipal) userDetailsService.loadUserByUsername(correo);
            if (!principal.isEnabled() || !Objects.equals(usuarioId, principal.getId())) {
                throw new BadCredentialsException("El usuario del token ya no está disponible.");
            }

            AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(
                    principal.getCorreo(),
                    principal.getId(),
                    principal.getNombre(),
                    principal.getRol()
            );

            var authentication = new UsernamePasswordAuthenticationToken(
                    usuarioAutenticado, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
