package com.minimarket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuditTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowGerenteToAccessAuditEvents() throws Exception {
        String gerenteToken = loginAndGetToken(mockMvc, objectMapper, "gerente", "Gerente123!");

        mockMvc.perform(get("/api/security/audit/events")
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyClienteAccessToAuditEvents() throws Exception {
        String clienteToken = loginAndGetToken(mockMvc, objectMapper, "cliente", "Cliente123!");

        mockMvc.perform(get("/api/security/audit/events")
                        .header("Authorization", bearer(clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPublicAccessToSecurityInfo() throws Exception {
        mockMvc.perform(get("/api/security/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("MiniMarket Plus API"));
    }

    @Test
    void shouldAllowPublicAccessToOAuth2Status() throws Exception {
        mockMvc.perform(get("/api/security/oauth2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocol").value("OAuth 2.0 / OpenID Connect"));
    }

    @Test
    void shouldRecordFailedLoginInAuditForGerenteReview() throws Exception {
        String loginBody = """
                {"username":"gerente","password":"ClaveIncorrecta123!"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());

        String gerenteToken = loginAndGetToken(mockMvc, objectMapper, "gerente", "Gerente123!");

        mockMvc.perform(get("/api/security/audit/events")
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'FAILED_LOGIN')]").exists());
    }
}
