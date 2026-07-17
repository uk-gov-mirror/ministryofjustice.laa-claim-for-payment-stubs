package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.JacksonConfig;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DraftClaimMapperImpl.class, JsonNodeMapper.class, JacksonConfig.class})
class DraftClaimMapperTest {

  private static final UUID DRAFT_ID = UUID.randomUUID();
  private static final UUID PROVIDER_USER_ID = UUID.randomUUID();

  @Autowired private DraftClaimMapper draftClaimMapper;

  @Test
  void shouldMapToDraftClaim() throws Exception {
    JsonNode payloadJson =
        new ObjectMapper()
            .readTree("""
            {
              "id": "019f5c43-ba95-755c-8f28-c92bfb46013b",
              "ufn": "100323/098",
              "concluded": "2025-03-14",
              "escaped": false,
              "claimed": 56.0,
              "lineItems": [
                {
                  "id": "019f5c46-5788-7252-9172-b494faac8fe8",
                  "title": "Interim hearing",
                  "category": "Work Item",
                  "date": "2023-12-20",
                  "evidenceItems": [
                    "019f5c45-6070-7abd-9129-171e41387b7c",
                    "019f5c45-ff06-7419-ab4c-9165d89b6173"
                  ]
                }
              ],
              "evidence": [
                {
                  "id": "019f5c45-6070-7abd-9129-171e41387b7c",
                  "fileName": "amoto-invoice-001.pdf",
                  "fileSize": 5000000,
                  "submittedOn": "2026-06-17T10:15:30Z"
                },
                {
                  "id": "019f5c45-ff06-7419-ab4c-9165d89b6173",
                  "fileName": "amoto-invoice-002.pdf",
                  "fileSize": 4000000,
                  "submittedOn": "2026-06-17T10:16:45Z"
                }
              ]
            }
            """);

    Map<String, Object> payloadMap =
        Map.of(
            "id", "019f5c43-ba95-755c-8f28-c92bfb46013b",
            "ufn", "100323/098",
            "concluded", "2025-03-14",
            "escaped", false,
            "claimed", 56.00,
            "lineItems", List.of(
                Map.of(
                    "id", "019f5c46-5788-7252-9172-b494faac8fe8",
                    "title", "Interim hearing",
                    "category", "Work Item",
                    "date", "2023-12-20",
                    "evidenceItems", List.of(
                        "019f5c45-6070-7abd-9129-171e41387b7c",
                        "019f5c45-ff06-7419-ab4c-9165d89b6173"
                    )
                )
            ),
            "evidence", List.of(
                Map.of(
                    "id", "019f5c45-6070-7abd-9129-171e41387b7c",
                    "fileName", "amoto-invoice-001.pdf",
                    "fileSize", 5000000,
                    "submittedOn", "2026-06-17T10:15:30Z"
                ),
                Map.of(
                    "id", "019f5c45-ff06-7419-ab4c-9165d89b6173",
                    "fileName", "amoto-invoice-002.pdf",
                    "fileSize", 4000000,
                    "submittedOn", "2026-06-17T10:16:45Z"
                )
            )
        );

    DraftClaimEntity entity =
        DraftClaimEntity.builder()
            .id(DRAFT_ID)
            .providerUserId(PROVIDER_USER_ID)
            .payload(payloadJson)
            .build();

    DraftClaim result = draftClaimMapper.toDraftClaim(entity);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(DRAFT_ID);
    assertThat(result.getProviderUserId()).isEqualTo(PROVIDER_USER_ID);
    assertThat(result.getPayload()).isEqualTo(payloadMap);
  }

  @Test
  void shouldMapToDraftClaimEntity() throws Exception {
    Map<String, Object> payloadMap =
        Map.of(
            "id", "019f5c43-ba95-755c-8f28-c92bfb46013b",
            "ufn", "100323/098",
            "concluded", LocalDate.of(2025, 3, 14),
            "escaped", false,
            "claimed", 56.00,
            "lineItems", List.of(
                Map.of(
                    "id", "019f5c46-5788-7252-9172-b494faac8fe8",
                    "title", "Interim hearing",
                    "category", "Work Item",
                    "date", "2023-12-20",
                    "evidenceItems", List.of(
                          "019f5c45-6070-7abd-9129-171e41387b7c",
                          "019f5c45-ff06-7419-ab4c-9165d89b6173"
                    )
                )
            ),
            "evidence", List.of(
                Map.of(
                    "id", "019f5c45-6070-7abd-9129-171e41387b7c",
                    "fileName", "amoto-invoice-001.pdf",
                    "fileSize", 5000000,
                    "submittedOn", Instant.ofEpochSecond(1781691330)
                ),
                Map.of(
                    "id", "019f5c45-ff06-7419-ab4c-9165d89b6173",
                    "fileName", "amoto-invoice-002.pdf",
                    "fileSize", 4000000,
                    "submittedOn", Instant.ofEpochSecond(1781691405)
                )
            )
        );

    JsonNode payloadJson =
        new ObjectMapper()
            .readTree("""
            {
              "id": "019f5c43-ba95-755c-8f28-c92bfb46013b",
              "ufn": "100323/098",
              "concluded": "2025-03-14",
              "escaped": false,
              "claimed": 56.0,
              "lineItems": [
                {
                  "id": "019f5c46-5788-7252-9172-b494faac8fe8",
                  "title": "Interim hearing",
                  "category": "Work Item",
                  "date": "2023-12-20",
                  "evidenceItems": [
                    "019f5c45-6070-7abd-9129-171e41387b7c",
                    "019f5c45-ff06-7419-ab4c-9165d89b6173"
                  ]
                }
              ],
              "evidence": [
                {
                  "id": "019f5c45-6070-7abd-9129-171e41387b7c",
                  "fileName": "amoto-invoice-001.pdf",
                  "fileSize": 5000000,
                  "submittedOn": "2026-06-17T10:15:30Z"
                },
                {
                  "id": "019f5c45-ff06-7419-ab4c-9165d89b6173",
                  "fileName": "amoto-invoice-002.pdf",
                  "fileSize": 4000000,
                  "submittedOn": "2026-06-17T10:16:45Z"
                }
              ]
            }
            """);

    DraftClaimPost request =
        DraftClaimPost.builder()
            .id(DRAFT_ID)
            .providerUserId(PROVIDER_USER_ID)
            .payload(payloadMap)
            .build();

    DraftClaimEntity result = draftClaimMapper.toDraftClaimEntity(request);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(DRAFT_ID);
    assertThat(result.getProviderUserId()).isEqualTo(PROVIDER_USER_ID);
    assertThat(result.getPayload()).isEqualTo(payloadJson);
  }

  @Test
  void shouldUpdateDraftClaimEntity() throws Exception {
    UUID oldProviderUserId = UUID.randomUUID();
    UUID newProviderUserId = UUID.randomUUID();

    JsonNode oldPayloadJson =
        new ObjectMapper()
            .readTree(
                """
          {
            "field": "oldValue"
          }
          """);

    JsonNode newPayloadJson =
        new ObjectMapper()
            .readTree(
                """
          {
            "field": "newValue"
          }
          """);

    Map<String, Object> newPayloadMap = Map.of("field", "newValue");

    DraftClaimEntity entity =
        DraftClaimEntity.builder()
            .id(DRAFT_ID)
            .providerUserId(oldProviderUserId)
            .payload(oldPayloadJson)
            .build();

    DraftClaimPut request =
        DraftClaimPut.builder()
            .providerUserId(newProviderUserId)
            .payload(newPayloadMap)
            .build();

    draftClaimMapper.updateEntity(request, entity);

    assertThat(entity.getId()).isEqualTo(DRAFT_ID);
    assertThat(entity.getProviderUserId()).isEqualTo(newProviderUserId);
    assertThat(entity.getPayload()).isEqualTo(newPayloadJson);
  }
}
