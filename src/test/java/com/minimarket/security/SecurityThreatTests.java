package com.minimarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.support.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.minimarket.support.SecurityTestSupport.bearer;
import static com.minimarket.support.SecurityTestSupport.loginAndGetToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityThreatTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldIncludeXssProtectionHeaders() throws Exception {
        mockMvc.perform(get("/api/public"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'"));
    }

    @Test
    void shouldRejectSqlInjectionPatternInLoginUsername() throws Exception {
        String body = """
                {"username":"admin' OR '1'='1","password":"x"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutCsrfTokenOnStatelessApi() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"test\",\"precio\":1,\"stock\":1,\"categoria\":{\"id\":1}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToInventario() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyClienteAccessToInventario() throws Exception {
        String clienteToken = loginAndGetToken(mockMvc, objectMapper, "cliente", "Cliente123!");

        mockMvc.perform(get("/api/inventario")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }
}
