package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
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

  @Captor ArgumentCaptor<DraftClaimPost> draftClaimPostCaptor;

  @Captor ArgumentCaptor<DraftClaimPut> draftClaimPutCaptor;

  @Captor ArgumentCaptor<DraftClaimPatch> draftClaimPatchCaptor;

  @Nested
  class GetDraftClaims {

    UUID providerUserId = UUID.randomUUID();
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void returnsOkStatusAndDraftClaim() throws Exception {
      Map<String, Object> payload = Map.of("someField", "someValue");

      when(mockDraftClaimService.getDraftClaim(draftClaimId, providerUserId))
          .thenReturn(
              DraftClaim.builder()
                  .id(draftClaimId)
                  .payload(payload)
                  .providerUserId(providerUserId)
                  .build());

      mockMvc
          .perform(
              get("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(draftClaimId.toString()))
          .andExpect(jsonPath("$.payload.someField").value("someValue"))
          .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));
    }

    @Test
    void returnsNotFoundStatusWhenDraftClaimDoesNotExist() throws Exception {
      when(mockDraftClaimService.getDraftClaim(draftClaimId, providerUserId))
          .thenThrow(new DraftClaimNotFoundException("Draft claim not found"));

      mockMvc
          .perform(
              get("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isNotFound());
    }

    @Test
    void returnsUnauthorisedStatusWhenAuthenticationIsMissing() throws Exception {
      mockMvc
          .perform(get("/api/v1/drafts/{draftClaimId}", draftClaimId))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsMissing() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(jwt().authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isForbidden());
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsBlank() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", ""))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class CreateDraftClaim {

    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Map<String, Object> payload = Map.of("someField", "someValue");

    String requestBody =
        String.format(
            """
            {
              "id": "%s",
              "payload": {
                "someField": "someValue"
              },
              "providerUserId": "%s"
            }
            """,
            draftClaimId, providerUserId);

    @Test
    void returnsCreatedStatusAndLocationHeader() throws Exception {
      when(mockDraftClaimService.createDraftClaim(any(DraftClaimPost.class)))
          .thenReturn(draftClaimId);

      mockMvc
          .perform(
              post("/api/v1/drafts")
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", containsString("/api/v1/drafts/" + draftClaimId)));

      verify(mockDraftClaimService).createDraftClaim(draftClaimPostCaptor.capture());

      assertThat(draftClaimPostCaptor.getValue().getId()).isEqualTo(draftClaimId);
      assertThat(draftClaimPostCaptor.getValue().getPayload()).isEqualTo(payload);
      assertThat(draftClaimPostCaptor.getValue().getProviderUserId()).isEqualTo(providerUserId);
    }

    @Test
    void returnsBadRequestStatusWhenBodyIsInvalid() throws Exception {
      String requestBody =
          """
          {
            "foo": "bar"
          }
          """;

      mockMvc
          .perform(
              post("/api/v1/drafts")
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsUnauthorisedStatusWhenAuthenticationIsMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/drafts")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isUnauthorized());

      verifyNoInteractions(mockDraftClaimService);
    }
  }

  @Nested
  class UpdateDraftClaim {

    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Map<String, Object> payload = Map.of("someField", "someValue");

    String requestBody =
        String.format(
            """
            {
              "payload": {
                "someField": "someValue"
              },
              "providerUserId": "%s"
            }
            """,
            providerUserId);

    @Test
    void returnsNoContentStatus() throws Exception {
      when(mockDraftClaimService.updateDraftClaim(any(DraftClaimPut.class), eq(draftClaimId)))
          .thenReturn(
              DraftClaim.builder()
                  .id(draftClaimId)
                  .payload(payload)
                  .providerUserId(providerUserId)
                  .build());

      mockMvc
          .perform(
              put("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isNoContent());

      verify(mockDraftClaimService)
          .updateDraftClaim(draftClaimPutCaptor.capture(), eq(draftClaimId));

      assertThat(draftClaimPutCaptor.getValue().getPayload()).isEqualTo(payload);
    }

    @Test
    void returnsBadRequestStatusWhenBodyIsInvalid() throws Exception {
      String requestBody =
          """
          {
            "foo": "bar"
          }
          """;

      mockMvc
          .perform(
              put("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsNotFoundStatusWhenDraftClaimDoesNotExist() throws Exception {
      when(mockDraftClaimService.updateDraftClaim(any(DraftClaimPut.class), eq(draftClaimId)))
          .thenThrow(new DraftClaimNotFoundException("Draft claim not found"));

      mockMvc
          .perform(
              put("/api/v1/drafts/{draftClaimId}", draftClaimId)
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
    void returnsUnauthorisedStatusWhenAuthenticationIsMissing() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isUnauthorized());

      verifyNoInteractions(mockDraftClaimService);
    }
  }

  @Nested
  class PatchDraftClaim {

    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Map<String, Object> payload = Map.of("someField", "someValue");

    String requestBody =
        """
        {
          "payload": {
            "someField": "someValue"
          }
        }
        """;

    @Test
    void returnsOkStatusAndPatchedClaim() throws Exception {
      when(mockDraftClaimService.patchDraftClaim(
              any(DraftClaimPatch.class), eq(draftClaimId), eq(providerUserId)))
          .thenReturn(
              DraftClaim.builder()
                  .id(draftClaimId)
                  .payload(payload)
                  .providerUserId(providerUserId)
                  .build());

      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(draftClaimId.toString()))
          .andExpect(jsonPath("$.payload.someField").value("someValue"))
          .andExpect(jsonPath("$.providerUserId").value(providerUserId.toString()));

      verify(mockDraftClaimService)
          .patchDraftClaim(draftClaimPatchCaptor.capture(), eq(draftClaimId), eq(providerUserId));

      assertThat(draftClaimPatchCaptor.getValue().getPayload()).isEqualTo(payload);
    }

    @Test
    void returnsBadRequestStatusWhenBodyIsInvalid() throws Exception {
      String requestBody =
          """
          {
            "foo": "bar"
          }
          """;

      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsNotFoundStatusWhenDraftClaimDoesNotExist() throws Exception {
      when(mockDraftClaimService.patchDraftClaim(
              any(DraftClaimPatch.class), eq(draftClaimId), eq(providerUserId)))
          .thenThrow(new DraftClaimNotFoundException("Draft claim not found"));

      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
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
    void returnsUnauthorisedStatusWhenAuthenticationIsMissing() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isUnauthorized());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsMissing() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(jwt().authorities(() -> "SCOPE_" + claimsWriteScope))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsBlank() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", ""))
                          .authorities(() -> "SCOPE_" + claimsWriteScope))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      verifyNoInteractions(mockDraftClaimService);
    }
  }

  @Nested
  class DeleteDraftClaim {

    UUID providerUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID draftClaimId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    void returnsNoContentStatus() throws Exception {
      doNothing().when(mockDraftClaimService).deleteDraftClaim(draftClaimId, providerUserId);

      mockMvc
          .perform(
              delete("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isNoContent());

      verify(mockDraftClaimService).deleteDraftClaim(eq(draftClaimId), eq(providerUserId));
    }

    @Test
    void returnsNotFoundStatusWhenDraftClaimDoesNotExist() throws Exception {
      doThrow(new DraftClaimNotFoundException("Draft claim not found"))
          .when(mockDraftClaimService)
          .deleteDraftClaim(draftClaimId, providerUserId);

      mockMvc
          .perform(
              delete("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", providerUserId.toString()))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isNotFound());
    }

    @Test
    void returnsUnauthorisedStatusWhenAuthenticationIsMissing() throws Exception {
      mockMvc
          .perform(delete("/api/v1/drafts/{draftClaimId}", draftClaimId))
          .andExpect(status().isUnauthorized());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsMissing() throws Exception {
      mockMvc
          .perform(
              delete("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(jwt().authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isForbidden());

      verifyNoInteractions(mockDraftClaimService);
    }

    @Test
    void returnsForbiddenStatusWhenJwtTokenIsBlank() throws Exception {
      mockMvc
          .perform(
              delete("/api/v1/drafts/{draftClaimId}", draftClaimId)
                  .with(
                      jwt()
                          .jwt(jwt -> jwt.claim("USER_NAME", ""))
                          .authorities(() -> "SCOPE_" + claimsWriteScope)))
          .andExpect(status().isForbidden());

      verifyNoInteractions(mockDraftClaimService);
    }
  }

  @Test
  void getDraftClaims_returnsOkStatusAndAllClaims() throws Exception {
    UUID providerUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID providerUserId2 = UUID.randomUUID();

    UUID claimId1 = UUID.randomUUID();
    UUID claimId2 = UUID.randomUUID();

    List<DraftClaim> draftClaims =
        List.of(
            DraftClaim.builder()
                .id(claimId1)
                .payload(Map.of())
                .providerUserId(providerUserId1)
                .build(),
            DraftClaim.builder()
                .id(claimId2)
                .payload(Map.of())
                .providerUserId(providerUserId2)
                .build());

    Page<DraftClaim> draftClaim1 =
        new PageImpl<>(List.of(draftClaims.getFirst()), Pageable.ofSize(1), 1);
    int pageNumber = 1;

    int pageSize = 1;

    when(mockDraftClaimService.getAllDraftClaimsForProvider(providerUserId1, pageNumber, pageSize))
        .thenReturn(draftClaim1);

    mockMvc
        .perform(
            get("/api/v1/drafts")
                .param("page", String.valueOf(pageNumber))
                .param("limit", String.valueOf(pageSize))
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.draftClaims[0].id").value(claimId1.toString()))
        .andExpect(jsonPath("$.draftClaims", hasSize(1)));
  }
}
