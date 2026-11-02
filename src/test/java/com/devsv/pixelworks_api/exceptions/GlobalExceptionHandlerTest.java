package com.devsv.pixelworks_api.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void prepararControlador() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitudController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void jsonMalFormadoDevuelveBadRequest() throws Exception {
        mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isString());
    }

    @Test
    void identificadorNoNumericoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/solicitudes/no-es-numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isString());
    }

    @Test
    void metodoIncorrectoConservaElEstadoYLaCabeceraAllow() throws Exception {
        mockMvc.perform(post("/solicitudes/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"));
    }

    @Test
    void tipoDeContenidoNoSoportadoDevuelveUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/solicitudes")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("nombre=Usuario"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void parametroObligatorioAusenteDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/buscar"))
                .andExpect(status().isBadRequest());
    }

    @RestController
    static class SolicitudController {

        @PostMapping("/solicitudes")
        public Map<String, Object> crear(@RequestBody Map<String, Object> solicitud) {
            return solicitud;
        }

        @GetMapping("/solicitudes/{id}")
        public Map<String, Integer> obtener(@PathVariable("id") Integer id) {
            return Map.of("id", id);
        }

        @GetMapping("/buscar")
        public Map<String, String> buscar(@RequestParam("nombre") String nombre) {
            return Map.of("nombre", nombre);
        }
    }
}
