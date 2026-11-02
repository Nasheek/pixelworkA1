package com.devsv.pixelworks_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey obtenerLlave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(UsuarioPrincipal principal) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(principal.getCorreo())
                .claim("id", principal.getId())
                .claim("nombre", principal.getNombre())
                .claim("rol", principal.getRol())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(obtenerLlave())
                .compact();
    }

    public Claims validarYExtraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerLlave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
