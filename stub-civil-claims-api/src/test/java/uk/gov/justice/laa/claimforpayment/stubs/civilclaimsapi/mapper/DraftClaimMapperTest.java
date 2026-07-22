package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.JacksonConfig;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      DraftClaimMapperImpl.class,
      LineItemMapperImpl.class,
      JsonNodeMapper.class,
      JacksonConfig.class
    })
class DraftClaimMapperTest {

  private static final UUID DRAFT_ID = UUID.randomUUID();
  private static final UUID PROVIDER_USER_ID = UUID.randomUUID();
  private static final UUID LINE_ITEM_ID_1 = UUID.randomUUID();
  private static final UUID LINE_ITEM_ID_2 = UUID.randomUUID();
  private static final UUID EVIDENCE_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID EVIDENCE_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final ClaimEvidenceEntity CLAIM_EVIDENCE_ENTITY_1 =
      ClaimEvidenceEntity.builder().id(EVIDENCE_ID_1).fileKey("fileKey1").fileSize(1000L).build();

  private static final ClaimEvidence CLAIM_EVIDENCE_1 =
      ClaimEvidence.builder().id(EVIDENCE_ID_1).fileKey("fileKey1").fileSize(1000L).build();

  private static final ClaimEvidenceEntity CLAIM_EVIDENCE_ENTITY_2 =
      ClaimEvidenceEntity.builder().id(EVIDENCE_ID_2).fileKey("fileKey2").fileSize(2000L).build();

  private static final ClaimEvidence CLAIM_EVIDENCE_2 =
      ClaimEvidence.builder().id(EVIDENCE_ID_2).fileKey("fileKey2").fileSize(2000L).build();

  private static final LineItemEntity LINE_ITEM_ENTITY_1 =
      LineItemEntity.builder().id(LINE_ITEM_ID_1).evidenceItems(Set.of(CLAIM_EVIDENCE_ENTITY_1)).build();

  private static final LineItem LINE_ITEM_1 =
      LineItem.builder().id(LINE_ITEM_ID_1).evidenceItems(List.of(EVIDENCE_ID_1)).build();

  private static final LineItemEntity LINE_ITEM_ENTITY_2 =
      LineItemEntity.builder()
          .id(LINE_ITEM_ID_2)
          .evidenceItems(Set.of(CLAIM_EVIDENCE_ENTITY_1, CLAIM_EVIDENCE_ENTITY_2))
          .build();

  private static final LineItem LINE_ITEM_2 =
      LineItem.builder()
          .id(LINE_ITEM_ID_2)
          .evidenceItems(List.of(EVIDENCE_ID_1, EVIDENCE_ID_2))
          .build();

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
              "claimed": 56.0
            }
            """);

    Map<String, Object> payloadMap =
        Map.of(
            "id", "019f5c43-ba95-755c-8f28-c92bfb46013b",
            "ufn", "100323/098",
            "concluded", "2025-03-14",
            "escaped", false,
            "claimed", 56.00
        );

    DraftClaimEntity draftClaimEntity =
        DraftClaimEntity.builder()
            .id(DRAFT_ID)
            .providerUserId(PROVIDER_USER_ID)
            .payload(payloadJson)
            .lineItems(List.of(LINE_ITEM_ENTITY_1, LINE_ITEM_ENTITY_2))
            .evidence(List.of(CLAIM_EVIDENCE_ENTITY_1, CLAIM_EVIDENCE_ENTITY_2))
            .build();

    DraftClaim result = draftClaimMapper.toDraftClaim(draftClaimEntity);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(DRAFT_ID);
    assertThat(result.getProviderUserId()).isEqualTo(PROVIDER_USER_ID);
    assertThat(result.getPayload()).isEqualTo(payloadMap);
    assertThat(result.getLineItems()).hasSize(2);
    assertThat(result.getLineItems()).containsExactly(LINE_ITEM_1, LINE_ITEM_2);
    assertThat(result.getEvidence()).hasSize(2);
    assertThat(result.getEvidence()).containsExactly(CLAIM_EVIDENCE_1, CLAIM_EVIDENCE_2);
  }

  @Test
  void shouldMapToDraftClaimEntity() throws Exception {
    Map<String, Object> payloadMap =
        Map.of(
            "id", "019f5c43-ba95-755c-8f28-c92bfb46013b",
            "ufn", "100323/098",
            "concluded", LocalDate.of(2025, 3, 14),
            "escaped", false,
            "claimed", 56.00
        );

    JsonNode payloadJson =
        new ObjectMapper()
            .readTree("""
            {
              "id": "019f5c43-ba95-755c-8f28-c92bfb46013b",
              "ufn": "100323/098",
              "concluded": "2025-03-14",
              "escaped": false,
              "claimed": 56.0
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
    assertThat(result.getLineItems()).hasSize(0);
    assertThat(result.getEvidence()).hasSize(0);
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

    DraftClaimEntity draftClaimEntity =
        DraftClaimEntity.builder()
            .id(DRAFT_ID)
            .providerUserId(oldProviderUserId)
            .payload(oldPayloadJson)
            .lineItems(List.of(LINE_ITEM_ENTITY_1, LINE_ITEM_ENTITY_2))
            .evidence(List.of(CLAIM_EVIDENCE_ENTITY_1, CLAIM_EVIDENCE_ENTITY_2))
            .build();

    DraftClaimPut request =
        DraftClaimPut.builder()
            .providerUserId(newProviderUserId)
            .payload(newPayloadMap)
            .build();

    draftClaimMapper.updateEntity(request, draftClaimEntity);

    assertThat(draftClaimEntity.getId()).isEqualTo(DRAFT_ID);
    assertThat(draftClaimEntity.getProviderUserId()).isEqualTo(newProviderUserId);
    assertThat(draftClaimEntity.getPayload()).isEqualTo(newPayloadJson);
    assertThat(draftClaimEntity.getLineItems()).hasSize(2);
    assertThat(draftClaimEntity.getLineItems()).containsExactly(LINE_ITEM_ENTITY_1, LINE_ITEM_ENTITY_2);
    assertThat(draftClaimEntity.getEvidence()).hasSize(2);
    assertThat(draftClaimEntity.getEvidence()).containsExactly(CLAIM_EVIDENCE_ENTITY_1, CLAIM_EVIDENCE_ENTITY_2);
  }
}
