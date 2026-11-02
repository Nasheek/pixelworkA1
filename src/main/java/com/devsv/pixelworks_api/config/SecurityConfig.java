package com.devsv.pixelworks_api.config;

import com.devsv.pixelworks_api.security.JwtAccessDeniedHandler;
import com.devsv.pixelworks_api.security.JwtAuthenticationEntryPoint;
import com.devsv.pixelworks_api.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                jwtAccessDeniedHandler
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // ==================================================
                        // AUTENTICACIÓN
                        // ==================================================

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // ==================================================
                        // CONSULTAS PÚBLICAS DEL CATÁLOGO (listas y detalles)
                        // ==================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/productos", "/api/productos/*",
                                "/api/categorias", "/api/categorias/*",
                                "/api/desarrolladores", "/api/desarrolladores/*",
                                "/api/ofertas", "/api/ofertas/*",
                                "/images/*"
                        ).permitAll()

                        // ==================================================
                        // ==================================================
                        // STOCK PÚBLICO DE CLAVES
                        // ==================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/claves/producto/*/stock"
                        ).permitAll()

                        // ==================================================
                        // TODO LO DEMÁS REQUIERE AUTENTICACIÓN
                        // ==================================================

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
