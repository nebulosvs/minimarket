package com.minimarket.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }

    public static String loginAndGetToken(MockMvc mockMvc, ObjectMapper objectMapper, String username, String password)
            throws Exception {
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

    public static long createCategoria(MockMvc mockMvc, ObjectMapper objectMapper, String token, String nombre)
            throws Exception {
        String body = """
                {"nombre":"%s"}
                """.formatted(nombre);

        String response = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asLong();
    }

    public static long createProducto(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            String token,
            long categoriaId,
            String nombre
    ) throws Exception {
        String body = """
                {"nombre":"%s","precio":1990.0,"stock":25,"categoria":{"id":%d}}
                """.formatted(nombre, categoriaId);

        String response = mockMvc.perform(post("/api/productos")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(response).get("id").asLong();
    }

    public static long findUsuarioIdByUsername(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            String gerenteToken,
            String username
    ) throws Exception {
        String response = mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", bearer(gerenteToken)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode usuarios = objectMapper.readTree(response);
        for (JsonNode usuario : usuarios) {
            if (username.equalsIgnoreCase(usuario.get("username").asText())) {
                return usuario.get("id").asLong();
            }
        }
        throw new IllegalStateException("Usuario no encontrado: " + username);
    }
}
