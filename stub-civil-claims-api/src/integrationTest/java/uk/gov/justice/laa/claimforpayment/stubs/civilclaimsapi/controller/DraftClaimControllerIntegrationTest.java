package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.CivilClaimsStubApplication;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;

@SpringBootTest(classes = CivilClaimsStubApplication.class, properties = "security.enabled=true")
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import({TestJwtConfig.class})
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName",
  "checkstyle:MethodName"
})
class DraftClaimControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  private UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Autowired private JwtEncoder jwtEncoder;

  @Value("${app.security.authorities.claims-write}")
  private String claimsWriteScope;

  @Test
  void shouldGetDraftClaim() throws Exception {
    UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
  }

  @Test
  void shouldNotGetDraftClaimWhenItDoesNotExist() throws Exception {
    UUID id = UUID.fromString("0ec9d4b6-900d-48ac-8685-ebb9fcd335dd");

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldCreateDraftClaim() throws Exception {
    UUID id = UUID.fromString("17dd7c98-ff17-4342-bef1-0b589a656f58");
    String payload = "{\"someField\": \"someValue\"}";

    String requestBody =
        String.format(
            """
        {
          "id": "%s",
          "payload": "{\\"someField\\": \\"someValue\\"}",
          "providerUserId": "%s"
        }
        """,
            id, providerUserId);

    mockMvc
        .perform(
            post("/api/v1/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.payload").value(payload))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
  }

  @Test
  void shouldUpdateDraftClaim() throws Exception {
    UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    String payload = "{\"someField\": \"someValue\"}";

    String requestBody =
        String.format(
            """
        {
          "payload": "{\\"someField\\": \\"someValue\\"}",
          "providerUserId": "%s"
        }
        """,
            providerUserId);

    mockMvc
        .perform(
            put("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.payload").value(payload))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
  }

  @Test
  void shouldNotUpdateDraftClaimWhenItDoesNotExist() throws Exception {
    UUID id = UUID.fromString("0ec9d4b6-900d-48ac-8685-ebb9fcd335dd");

    String requestBody =
        String.format(
            """
        {
          "payload": "{\\"someField\\": \\"someValue\\"}",
          "providerUserId": "%s"
        }
        """,
            providerUserId);

    mockMvc
        .perform(
            put("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldPatchDraftClaim() throws Exception {
    UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    String payload = "{'someField':'someValue'}";

    String requestBody =
        String.format("""
        {
          "payload": "%s"
        }
        """, payload);

    mockMvc
        .perform(
            patch("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.payload").value(payload))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.payload").value(payload))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
  }

  @Test
  void shouldNotPatchDraftClaimWhenItDoesNotExist() throws Exception {
    UUID id = UUID.fromString("0ec9d4b6-900d-48ac-8685-ebb9fcd335dd");

    String payload = "{'someField':'someValue'}";

    String requestBody =
        String.format("""
        {
          "payload": "%s"
        }
        """, payload);

    mockMvc
        .perform(
            patch("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteDraftClaim() throws Exception {
    UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    mockMvc
        .perform(
            delete("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/drafts/{draftClaimId}", id)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldNotDeleteDraftClaimWhenItDoesNotExist() throws Exception {
    UUID id = UUID.fromString("0ec9d4b6-900d-48ac-8685-ebb9fcd335dd");

    mockMvc
        .perform(
            delete("/api/v1/drafts/{draftClaimId}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());
  }
}
