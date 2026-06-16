package com.minimarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/app/index_normal"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/app/index_protegido"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/private"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnJwtOnValidLogin() throws Exception {
        String loginBody = """
                {"username":"gerente","password":"Gerente123!"}
                """;

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertTrue(json.hasNonNull("token"));
    }

    @Test
    void shouldReturnForbiddenWhenRoleIsInsufficient() throws Exception {
        String clienteToken = loginAndGetToken("cliente", "Cliente123!");
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowGerenteToAccessUsuarios() throws Exception {
        String gerenteToken = loginAndGetToken("gerente", "Gerente123!");
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        String expiredToken = buildExpiredToken("gerente");
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private String buildExpiredToken(String username) {
        byte[] keyBytes = Decoders.BASE64.decode("V2lFTHVIT2VjYzVvVUpjQ2hjNjNLMmVzeHBJQ1p3U0M=");
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(now.getTime() - 10_000))
                .expiration(new Date(now.getTime() - 5_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
