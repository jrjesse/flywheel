package com.antigravity.sales.api.controller;

import com.antigravity.sales.api.dto.auth.RegisterRequest;
import com.antigravity.sales.api.dto.user.InviteUserRequest;
import com.antigravity.sales.core.model.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void vendorSeesOnlyAssignedLeads() throws Exception {
        Registration admin = registerAdmin("admin-assign@test.com");
        String vendorToken = inviteVendor(admin.token(), "vendor-assign@test.com");

        mockMvc.perform(post("/api/webhooks/leads")
                        .header("X-Webhook-Secret", admin.webhookSecret())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Lead Pool\",\"email\":\"pool@test.com\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leads/unassigned")
                        .header("Authorization", "Bearer " + vendorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        MvcResult leadsResult = mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andReturn();

        Long leadId = objectMapper.readTree(leadsResult.getResponse().getContentAsString())
                .get(0).get("id").asLong();

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + vendorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(patch("/api/leads/" + leadId + "/claim")
                        .header("Authorization", "Bearer " + vendorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedToUserId").exists());

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + vendorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void adminCanAssignLeadToVendor() throws Exception {
        Registration admin = registerAdmin("admin-assign2@test.com");
        Registration vendor = inviteVendorWithId(admin.token(), "vendor-assign2@test.com");

        mockMvc.perform(post("/api/webhooks/leads")
                        .header("X-Webhook-Secret", admin.webhookSecret())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Lead Direto\",\"email\":\"direto@test.com\"}"))
                .andExpect(status().isOk());

        MvcResult leadsResult = mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andReturn();

        Long leadId = objectMapper.readTree(leadsResult.getResponse().getContentAsString())
                .get(0).get("id").asLong();

        mockMvc.perform(patch("/api/leads/" + leadId + "/assign")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + vendor.userId() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leads")
                        .header("Authorization", "Bearer " + vendor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private Registration registerAdmin(String email) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setTenantName("Assign Corp");
        register.setDocument("DOC-" + email.hashCode());
        register.setDocumentType("CNPJ");
        register.setEmail(email);
        register.setPassword("adminpass123");
        register.setDisplayName("Admin");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Registration(body.get("token").asText(), body.get("webhookSecret").asText(), null);
    }

    private String inviteVendor(String adminToken, String email) throws Exception {
        return inviteVendorWithId(adminToken, email).token();
    }

    private Registration inviteVendorWithId(String adminToken, String email) throws Exception {
        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail(email);
        invite.setDisplayName("Vendedor");
        invite.setPassword("vendorpass123");
        invite.setRole(UserRole.AGENT);

        MvcResult inviteResult = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isOk())
                .andReturn();

        String userId = objectMapper.readTree(inviteResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"vendorpass123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        return new Registration(token, null, userId);
    }

    private record Registration(String token, String webhookSecret, String userId) {}
}
