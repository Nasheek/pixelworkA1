package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.interfaces.IReporteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReporteControllerSecurityTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void prepararContexto() {
        context = new AnnotationConfigApplicationContext(SeguridadTestConfig.class);
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void jugadorNoPuedeConsultarLasVentasTotales() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("jugador", null, "ROLE_JUGADOR"));

        assertThrows(AccessDeniedException.class,
                () -> context.getBean(ReporteController.class).getVentasTotales());

        verifyNoInteractions(context.getBean(IReporteService.class));
    }

    @Test
    void administradorPuedeConsultarLasVentasTotales() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", null, "ROLE_ADMIN"));
        when(context.getBean(IReporteService.class).obtenerFinanzas(null, null, null))
                .thenReturn(new ResumenFinancieroDTO());

        assertDoesNotThrow(() -> context.getBean(ReporteController.class).getVentasTotales());
    }

    @Configuration
    @EnableMethodSecurity
    static class SeguridadTestConfig {
        @Bean
        IReporteService reporteService() {
            return mock(IReporteService.class);
        }

        @Bean
        ReporteController reporteController(IReporteService reporteService) {
            return new ReporteController(reporteService);
        }
    }
}
