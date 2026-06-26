package com.minimarket.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static com.minimarket.support.SecurityTestSupport.bearer;
import static com.minimarket.support.SecurityTestSupport.loginAndGetToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiCrudIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String empleadoToken;
    private String gerenteToken;
    private long categoriaId;
    private long productoId;
    private String categoriaNombre;
    private String productoNombre;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        empleadoToken = loginAndGetToken(mockMvc, objectMapper, "empleado", "Empleado123!");
        gerenteToken = loginAndGetToken(mockMvc, objectMapper, "gerente", "Gerente123!");
        categoriaNombre = "Lac-" + suffix;
        productoNombre = "Leche-" + suffix;
        categoriaId = SecurityTestSupport.createCategoria(mockMvc, objectMapper, empleadoToken, categoriaNombre);
        productoId = SecurityTestSupport.createProducto(mockMvc, objectMapper, gerenteToken, categoriaId, productoNombre);
    }

    @Test
    void shouldCreateCategoriaSuccessfully() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String nombre = "Cat-crud-" + suffix;

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldCreateProductoSuccessfully() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String nombre = "Prod-crud-" + suffix;
        String body = """
                {"nombre":"%s","precio":1990.0,"stock":25,"categoria":{"id":%d}}
                """.formatted(nombre, categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(gerenteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(nombre))
                .andExpect(jsonPath("$.categoria.id").value(categoriaId));
    }

    @Test
    void shouldDenyEmpleadoFromCreatingProducto() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String nombre = "Prod-deny-" + suffix;
        String body = """
                {"nombre":"%s","precio":1990.0,"stock":25,"categoria":{"id":%d}}
                """.formatted(nombre, categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegisterInventarioMovementAsEmpleado() throws Exception {
        long timestamp = new Date().getTime();
        String body = """
                {"producto":{"id":%d},"cantidad":10,"tipoMovimiento":"Entrada","fechaMovimiento":%d}
                """.formatted(productoId, timestamp);

        String response = mockMvc.perform(post("/api/inventario")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode inventario = objectMapper.readTree(response);
        assertEquals("Entrada", inventario.get("tipoMovimiento").asText());
        assertTrue(inventario.hasNonNull("id"));
    }

    @Test
    void shouldListUsuariosAsGerente() throws Exception {
        String response = mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode usuarios = objectMapper.readTree(response);
        assertTrue(usuarios.isArray());
        assertTrue(usuarios.size() >= 3);
    }

    @Test
    void shouldCreateVentaAsEmpleado() throws Exception {
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
                .andExpect(jsonPath("$.total").value(3980.0));
    }

    @Test
    void shouldDenyClienteFromCreatingVenta() throws Exception {
        String clienteToken = loginAndGetToken(mockMvc, objectMapper, "cliente", "Cliente123!");
        long clienteId = SecurityTestSupport.findUsuarioIdByUsername(
                mockMvc, objectMapper, gerenteToken, "cliente");
        long timestamp = new Date().getTime();
        String body = """
                {
                  "usuario":{"id":%d},
                  "fecha":%d,
                  "detalles":[
                    {"producto":{"id":%d},"cantidad":2,"precio":1990.0}
                  ]
                }
                """.formatted(clienteId, timestamp, productoId);

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", bearer(clienteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
