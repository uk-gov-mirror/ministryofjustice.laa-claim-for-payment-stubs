package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
  void shouldCreateDraftClaim() throws Exception {
    UUID id = UUID.fromString("17dd7c98-ff17-4342-bef1-0b589a656f58");
    String payload = "{'someField':'someValue'}";

    String requestBody =
        String.format("""
        {
          "id": "%s",
          "payload": "%s"
        }
        """, id, payload);

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

  private String encode(Map<String, Object> claims) {
    Instant now = Instant.now();

    JwtClaimsSet jwtClaims =
        JwtClaimsSet.builder()
            .issuer("https://issuer.test")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(60))
            .claims(c -> c.putAll(claims))
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaims)).getTokenValue();
  }
}
