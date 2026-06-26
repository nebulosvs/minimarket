package com.minimarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.minimarket.support.SecurityTestSupport.bearer;
import static com.minimarket.support.SecurityTestSupport.loginAndGetToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String clienteToken;
    private String empleadoToken;
    private String gerenteToken;
    private long categoriaId;
    private long productoId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        clienteToken = loginAndGetToken(mockMvc, objectMapper, "cliente", "Cliente123!");
        empleadoToken = loginAndGetToken(mockMvc, objectMapper, "empleado", "Empleado123!");
        gerenteToken = loginAndGetToken(mockMvc, objectMapper, "gerente", "Gerente123!");
        categoriaId = SecurityTestSupport.createCategoria(mockMvc, objectMapper, empleadoToken, "Cat-" + suffix);
        productoId = SecurityTestSupport.createProducto(mockMvc, objectMapper, gerenteToken, categoriaId, "Prod-" + suffix);
    }

    @Test
    void crearProducto_sinAutenticacion_retornaUnauthorized() throws Exception {
        String body = """
                {"nombre":"Pan","precio":990.0,"stock":5,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearProducto_comoCliente_retornaForbidden() throws Exception {
        String body = """
                {"nombre":"Pan","precio":990.0,"stock":5,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(clienteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearProducto_comoEmpleado_retornaForbidden() throws Exception {
        String body = """
                {"nombre":"Pan","precio":990.0,"stock":5,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearProducto_comoGerente_retornaOk() throws Exception {
        String body = """
                {"nombre":"Pan Integral","precio":1290.0,"stock":12,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(gerenteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarProducto_comoEmpleado_retornaForbidden() throws Exception {
        String body = """
                {"nombre":"Producto Editado","precio":1500.0,"stock":8,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(put("/api/productos/" + productoId)
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarProducto_comoGerente_retornaOk() throws Exception {
        String body = """
                {"nombre":"Producto Actualizado","precio":2200.0,"stock":18,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(put("/api/productos/" + productoId)
                        .header("Authorization", bearer(gerenteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
