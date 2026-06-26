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

import java.util.Date;
import java.util.UUID;

import static com.minimarket.support.SecurityTestSupport.bearer;
import static com.minimarket.support.SecurityTestSupport.loginAndGetToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String clienteToken;
    private String empleadoToken;
    private String gerenteToken;
    private long productoId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        clienteToken = loginAndGetToken(mockMvc, objectMapper, "cliente", "Cliente123!");
        empleadoToken = loginAndGetToken(mockMvc, objectMapper, "empleado", "Empleado123!");
        gerenteToken = loginAndGetToken(mockMvc, objectMapper, "gerente", "Gerente123!");
        long categoriaId = SecurityTestSupport.createCategoria(mockMvc, objectMapper, empleadoToken, "Cat-" + suffix);
        productoId = SecurityTestSupport.createProducto(mockMvc, objectMapper, gerenteToken, categoriaId, "Prod-" + suffix);
    }

    @Test
    void registrarMovimientoEntrada_comoEmpleado_retornaOk() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {"producto":{"id":%d},"cantidad":10,"tipoMovimiento":"Entrada","fechaMovimiento":%d}
                """.formatted(productoId, timestamp);

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoMovimiento").value("Entrada"))
                .andExpect(jsonPath("$.cantidad").value(10));
    }

    @Test
    void registrarMovimientoSalida_comoGerente_retornaOk() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {"producto":{"id":%d},"cantidad":3,"tipoMovimiento":"Salida","fechaMovimiento":%d}
                """.formatted(productoId, timestamp);

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", bearer(gerenteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoMovimiento").value("Salida"));
    }

    @Test
    void registrarMovimiento_comoCliente_retornaForbidden() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {"producto":{"id":%d},"cantidad":5,"tipoMovimiento":"Entrada","fechaMovimiento":%d}
                """.formatted(productoId, timestamp);

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", bearer(clienteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarMovimiento_sinAutenticacion_retornaUnauthorized() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {"producto":{"id":%d},"cantidad":5,"tipoMovimiento":"Entrada","fechaMovimiento":%d}
                """.formatted(productoId, timestamp);

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
