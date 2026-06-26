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
class VentaAuthorizationTest {

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
    void registrarVenta_comoEmpleado_retornaOkConTotalYDetalles() throws Exception {
        long empleadoId = SecurityTestSupport.findUsuarioIdByUsername(
                mockMvc, objectMapper, gerenteToken, "empleado");
        long timestamp = new Date().getTime();
        String body = """
                {
                  "usuario":{"id":%d},
                  "fecha":%d,
                  "detalles":[
                    {"producto":{"id":%d},"cantidad":2,"precio":1990.0}
                  ]
                }
                """.formatted(empleadoId, timestamp, productoId);

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3980.0))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.detalles[0].producto.id").value(productoId));
    }

    @Test
    void registrarVenta_comoCliente_retornaForbidden() throws Exception {
        long clienteId = SecurityTestSupport.findUsuarioIdByUsername(
                mockMvc, objectMapper, gerenteToken, "cliente");
        long timestamp = new Date().getTime();
        String body = """
                {
                  "usuario":{"id":%d},
                  "fecha":%d,
                  "detalles":[
                    {"producto":{"id":%d},"cantidad":1,"precio":1990.0}
                  ]
                }
                """.formatted(clienteId, timestamp, productoId);

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", bearer(clienteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarVenta_comoGerente_retornaForbidden() throws Exception {
        long gerenteId = SecurityTestSupport.findUsuarioIdByUsername(
                mockMvc, objectMapper, gerenteToken, "gerente");
        long timestamp = new Date().getTime();
        String body = """
                {
                  "usuario":{"id":%d},
                  "fecha":%d,
                  "detalles":[
                    {"producto":{"id":%d},"cantidad":1,"precio":1990.0}
                  ]
                }
                """.formatted(gerenteId, timestamp, productoId);

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", bearer(gerenteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarVenta_sinAutenticacion_retornaUnauthorized() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {
                  "usuario":{"id":1},
                  "fecha":%d,
                  "detalles":[
                    {"producto":{"id":%d},"cantidad":1,"precio":1990.0}
                  ]
                }
                """.formatted(timestamp, productoId);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
