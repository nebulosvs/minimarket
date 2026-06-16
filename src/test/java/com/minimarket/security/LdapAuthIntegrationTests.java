package com.minimarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.ldap.enabled=true")
@AutoConfigureMockMvc
class LdapAuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAuthenticateLdapEmployeeAndReturnJwt() throws Exception {
        String body = """
                {"username":"ldap.empleado","password":"LdapEmpleado123!"}
                """;

        String response = mockMvc.perform(post("/api/auth/ldap/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertTrue(json.hasNonNull("token"));
    }

    @Test
    void shouldRejectInvalidLdapCredentials() throws Exception {
        String body = """
                {"username":"ldap.empleado","password":"wrong-password"}
                """;

        mockMvc.perform(post("/api/auth/ldap/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAuthenticateLdapGerenteAndReturnJwt() throws Exception {
        String body = """
                {"username":"ldap.gerente","password":"LdapGerente123!"}
                """;

        String response = mockMvc.perform(post("/api/auth/ldap/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode json = objectMapper.readTree(response);
        assertTrue(json.hasNonNull("token"));
    }
}
