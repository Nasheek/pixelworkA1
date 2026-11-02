package com.devsv.pixelworks_api;

import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import com.devsv.pixelworks_api.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired CategoriaRepository categorias;
    @Autowired DesarrolladorRepository desarrolladores;
    @Autowired ProductoRepository productos;
    @Autowired MetodoPagoRepository metodosPago;
    @Autowired OfertaRepository ofertas;
    @Autowired OfertaProductoRepository ofertasProductos;
    @Autowired ClaveActivacionRepository claves;

    @Test
    void registrationLoginAndPurchaseApplyDiscountAndDeliverDistinctKeys() throws Exception {
        String token = register("buyer@example.com");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"buyer@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());

        Categoria categoria = categorias.save(new Categoria(null, "Acción", "Pruebas"));
        Desarrollador desarrollador = desarrolladores.save(
                new Desarrollador(null, "Estudio", "Pruebas", TipoDesarrollador.I, "SV"));
        Producto producto = productos.save(new Producto(null, "Juego", "Pruebas", LocalDate.now(),
                new BigDecimal("10.00"), null, true, categoria, desarrollador));
        MetodoPago pago = metodosPago.save(new MetodoPago(null, "Tarjeta"));
        Oferta oferta = ofertas.save(new Oferta(null, "Oferta", new BigDecimal("10.00"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
        ofertasProductos.save(new OfertaProducto(null, oferta, producto));
        claves.save(new ClaveActivacion(null, producto, "TEST-KEY-1", EstadoClave.DISPONIBLE, null));
        claves.save(new ClaveActivacion(null, producto, "TEST-KEY-2", EstadoClave.DISPONIBLE, null));
        claves.flush();

        String cart = "{\"metodoPagoId\":%d,\"items\":[{\"productoId\":%d,\"cantidad\":1},{\"productoId\":%d,\"cantidad\":1}]}"
                .formatted(pago.getId(), producto.getId(), producto.getId());
        mvc.perform(post("/api/compras").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(18.0))
                .andExpect(jsonPath("$.detalles.length()").value(1))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.detalles[0].claves.length()").value(2));

        assertEquals(0, claves.countByProductoIdAndEstado(producto.getId(), EstadoClave.DISPONIBLE));
        mvc.perform(get("/api/compras/mis-juegos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(18.0))
                .andExpect(jsonPath("$[0].detalles[0].claves.length()").value(2));
    }

    @Test
    void playerCannotModifyCategoriesOrReadFinancialReports() throws Exception {
        String token = register("player@example.com");
        mvc.perform(post("/api/categorias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"No autorizada\",\"descripcion\":\"Pruebas\"}"))
                .andExpect(status().isForbidden());
        assertFalse(categorias.existsByNombre("No autorizada"));
        mvc.perform(get("/api/reportes/ventas-totales").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void disablingAUserInvalidatesTheirExistingToken() throws Exception {
        String token = register("disabled@example.com");
        Usuario usuario = usuarios.findByCorreo("disabled@example.com").orElseThrow();
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarios.saveAndFlush(usuario);
        mvc.perform(get("/api/compras/mis-juegos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokingAdminRoleInvalidatesPrivilegesOfExistingToken() throws Exception {
        String token = register("admin@example.com");
        Usuario usuario = usuarios.findByCorreo("admin@example.com").orElseThrow();
        usuario.setRol(roles.save(new Rol(null, "ADMIN")));
        usuarios.saveAndFlush(usuario);
        // The existing token must use the current role from the database.
        mvc.perform(get("/api/reportes/ventas-totales").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        usuario.setRol(roles.findByNombre("JUGADOR").orElseThrow());
        usuarios.saveAndFlush(usuario);
        mvc.perform(get("/api/reportes/ventas-totales").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedJsonReturnsBadRequestAndCatalogRemainsPublic() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
    }

    @Test
    void corsAcceptsSupportedLocalFrontendsAndRejectsOtherOrigins() throws Exception {
        for (String origin : new String[]{"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"}) {
            mvc.perform(options("/api/auth/login").header("Origin", origin)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
        mvc.perform(options("/api/auth/login").header("Origin", "https://unconfigured.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Usuario de pruebas\",\"correo\":\"%s\",\"password\":\"secret123\"}".formatted(email)))
                .andExpect(status().isCreated()).andReturn();
        return JsonMapper.builder().build().readTree(result.getResponse().getContentAsString())
                .path("token").asText();
    }
}
