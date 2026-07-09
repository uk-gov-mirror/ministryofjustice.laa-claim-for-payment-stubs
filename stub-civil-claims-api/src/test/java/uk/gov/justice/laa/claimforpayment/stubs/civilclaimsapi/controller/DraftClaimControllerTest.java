package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.security.SecurityConfig;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.security.XAuthSecurityConfig;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service.DatabaseBasedDraftClaimService;

@WebMvcTest(controllers = DraftClaimController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, TestJwtConfig.class, XAuthSecurityConfig.class})
@TestPropertySource(properties = "security.enabled=true")
@ExtendWith(MockitoExtension.class)
public class DraftClaimControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DatabaseBasedDraftClaimService mockDraftClaimService;

  @Value("${app.security.authorities.claims-write}")
  private String claimsWriteScope;

  @Test
  void getDraftClaimById_returnsOkStatusAndDraftClaim() throws Exception {

    UUID providerUserId = UUID.randomUUID();
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    String payload =
        """
        {
          "someField": "someValue"
        }
        """;

    when(mockDraftClaimService.getDraftClaim(draftClaimId, providerUserId))
        .thenReturn(
            DraftClaim.builder()
                .id(draftClaimId)
                .payload(payload)
                .providerUserId(providerUserId)
                .build());

    mockMvc
        .perform(
            get("/api/v1/drafts/123e4567-e89b-12d3-a456-426614174000")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value("123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(jsonPath("$.payload").value(payload))
        .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
  }

  @Test
  void createDraftClaim_returnsCreatedStatusAndLocationHeader() throws Exception {
    UUID providerUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    when(mockDraftClaimService.createDraftClaim(any(DraftClaimRequestBody.class), any(UUID.class))).thenReturn(draftClaimId);

    String requestBody =
        """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "payload": "{'someField':'someValue'}"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/drafts")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/api/v1/drafts/" + draftClaimId)));
  }

  @Test
  void updateClaim_returnsNoContentStatus() throws Exception {
    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    String payload =
        """
        {
          "someField": "someValue"
        }
        """;

    String requestBody =
        """
        {
          "id": "123e4567-e89b-12d3-a456-426614174000",
          "payload": "{'someField':'someValue'}"
        }
        """;

    when(mockDraftClaimService.getDraftClaim(draftClaimId, providerUserId))
        .thenReturn(
            DraftClaim.builder()
                .id(draftClaimId)
                .payload(payload)
                .providerUserId(providerUserId)
                .build());

    mockMvc
        .perform(
            put("/api/v1/drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());

    verify(mockDraftClaimService).updateDraftClaim(any(DraftClaimRequestBody.class), any(UUID.class));
  }

  @Test
  void deleteDraftClaim_returnsVoid() throws Exception {
    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    doNothing().when(mockDraftClaimService).deleteDraftClaim(draftClaimId, providerUserId);

    mockMvc
        .perform(
            delete("/api/v1/drafts/123e4567-e89b-12d3-a456-426614174000")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());

    verify(mockDraftClaimService).deleteDraftClaim(draftClaimId, providerUserId);
  }

}
