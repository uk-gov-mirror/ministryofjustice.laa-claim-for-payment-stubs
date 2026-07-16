package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            .readTree(
                """
          {
            "someString": "string",
            "someBoolean": true,
            "someInteger": 1,
            "someDate": "2026-07-16"
          }
          """);

    Map<String, Object> payloadMap = Map.of(
        "someString", "string",
        "someBoolean", true,
        "someInteger", 1,
        "someDate", "2026-07-16"
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
    JsonNode payloadJson =
        new ObjectMapper()
            .readTree(
                """
          {
            "someString": "string",
            "someBoolean": true,
            "someInteger": 1,
            "someDate": "2026-07-16"
          }
          """);

    Map<String, Object> payloadMap = Map.of(
        "someString", "string",
        "someBoolean", true,
        "someInteger", 1,
        "someDate", LocalDate.of(2026, 7, 16)
    );

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
