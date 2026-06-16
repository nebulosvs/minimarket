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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessTests {

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
        productoId = SecurityTestSupport.createProducto(mockMvc, objectMapper, empleadoToken, categoriaId, "Prod-" + suffix);
    }

    @Test
    void shouldDenyUnauthenticatedAccessToProductos() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowClienteToListProductos() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowEmpleadoToListCategorias() throws Exception {
        mockMvc.perform(get("/api/categorias")
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyClienteAccessToInventario() throws Exception {
        mockMvc.perform(get("/api/inventario")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowEmpleadoAccessToInventario() throws Exception {
        mockMvc.perform(get("/api/inventario")
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyClienteAccessToVentasList() throws Exception {
        mockMvc.perform(get("/api/ventas")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowEmpleadoAccessToVentasList() throws Exception {
        mockMvc.perform(get("/api/ventas")
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyClienteAccessToCarritoList() throws Exception {
        mockMvc.perform(get("/api/carrito")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowClienteToPostCarrito() throws Exception {
        long clienteId = SecurityTestSupport.findUsuarioIdByUsername(
                mockMvc, objectMapper, gerenteToken, "cliente");
        String body = """
                {"usuario":{"id":%d},"producto":{"id":%d},"cantidad":2}
                """.formatted(clienteId, productoId);

        mockMvc.perform(post("/api/carrito")
                        .header("Authorization", bearer(clienteToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyClienteFromCreatingProducto() throws Exception {
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
    void shouldAllowEmpleadoToCreateProducto() throws Exception {
        String body = """
                {"nombre":"Aceite","precio":2990.0,"stock":10,"categoria":{"id":%d}}
                """.formatted(categoriaId);

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(empleadoToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyEmpleadoFromDeletingProducto() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId)
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowGerenteToDeleteProducto() throws Exception {
        long extraProductoId = SecurityTestSupport.createProducto(
                mockMvc, objectMapper, empleadoToken, categoriaId, "Producto temporal");

        mockMvc.perform(delete("/api/productos/" + extraProductoId)
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyClienteFromDeletingCategoria() throws Exception {
        mockMvc.perform(delete("/api/categorias/" + categoriaId)
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyEmpleadoFromDeletingCategoria() throws Exception {
        mockMvc.perform(delete("/api/categorias/" + categoriaId)
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowGerenteToDeleteCategoriaWithoutProductos() throws Exception {
        long categoriaTemporalId = SecurityTestSupport.createCategoria(
                mockMvc, objectMapper, empleadoToken, "Temp-" + UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(delete("/api/categorias/" + categoriaTemporalId)
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyEmpleadoFromListingUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer(empleadoToken)))
                .andExpect(status().isForbidden());
    }
}
