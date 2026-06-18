package ru.berkut.spring.hubflow;

import ru.berkut.spring.hubflow.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerIsPublicBehindContextPath() throws Exception {
        when(authService.register(any(AuthService.RegisterRequest.class)))
            .thenReturn(new AuthService.AuthResponse(
                "access-token",
                "refresh-token",
                UUID.randomUUID(),
                "user@example.com"
            ));

        mockMvc.perform(post("/hub-flow/api/v1/auth/register")
                .contextPath("/hub-flow/api/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "user@example.com",
                      "password": "secret123",
                      "firstName": "Test",
                      "lastName": "User",
                      "phone": "+77000000000"
                    }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void openApiDocsArePublicBehindContextPath() throws Exception {
        mockMvc.perform(get("/hub-flow/api/v1/v3/api-docs")
                .contextPath("/hub-flow/api/v1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("HubFlow API"));
    }
}
