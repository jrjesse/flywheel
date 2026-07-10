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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void adminCanInviteVendorAndVendorCanLogin() throws Exception {
        String adminToken = registerAdmin("admin-team@test.com");

        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("vendedor@test.com");
        invite.setDisplayName("João Vendedor");
        invite.setPassword("senha12345");
        invite.setRole(UserRole.AGENT);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.email").value("vendedor@test.com"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"vendedor@test.com\",\"password\":\"senha12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENT"));
    }

    @Test
    void adminCanListTeamMembers() throws Exception {
        String adminToken = registerAdmin("admin-list@test.com");

        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("agent-list@test.com");
        invite.setDisplayName("Maria");
        invite.setPassword("senha12345");
        invite.setRole(UserRole.AGENT);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void vendorCannotInviteUsers() throws Exception {
        String adminToken = registerAdmin("admin-block@test.com");

        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("vendedor-block@test.com");
        invite.setDisplayName("Carlos");
        invite.setPassword("senha12345");
        invite.setRole(UserRole.AGENT);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"vendedor-block@test.com\",\"password\":\"senha12345\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String vendorToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        InviteUserRequest another = new InviteUserRequest();
        another.setEmail("outro@test.com");
        another.setDisplayName("Outro");
        another.setPassword("senha12345");
        another.setRole(UserRole.AGENT);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + vendorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(another)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotInviteAdminRole() throws Exception {
        String adminToken = registerAdmin("admin-role@test.com");

        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("fake-admin@test.com");
        invite.setDisplayName("Fake Admin");
        invite.setPassword("senha12345");
        invite.setRole(UserRole.ADMIN);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isBadRequest());
    }

    private String registerAdmin(String email) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setTenantName("Team Corp");
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
        return body.get("token").asText();
    }
}
