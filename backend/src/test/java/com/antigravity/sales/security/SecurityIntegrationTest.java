package com.antigravity.sales.security;

import com.antigravity.sales.api.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthenticatedAccessToLeadsIsDenied() throws Exception {
        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndAccessLeads() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setTenantName("Test Corp");
        register.setDocument("12345678000199");
        register.setDocumentType("CNPJ");
        register.setEmail("admin@test.com");
        register.setPassword("password123");
        register.setDisplayName("Admin User");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.webhookSecret").exists())
                .andReturn();

        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void leadWebhookWithoutSecretIsRejected() throws Exception {
        mockMvc.perform(post("/api/webhooks/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John\",\"email\":\"john@test.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void settingsResponseDoesNotExposeSmtpPassword() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setTenantName("Settings Corp");
        register.setDocument("98765432000100");
        register.setDocumentType("CNPJ");
        register.setEmail("settings@test.com");
        register.setPassword("password123");
        register.setDisplayName("Settings Admin");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/settings/proposals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smtpPassword").doesNotExist())
                .andExpect(jsonPath("$.hasSmtpPassword").exists());
    }
}
