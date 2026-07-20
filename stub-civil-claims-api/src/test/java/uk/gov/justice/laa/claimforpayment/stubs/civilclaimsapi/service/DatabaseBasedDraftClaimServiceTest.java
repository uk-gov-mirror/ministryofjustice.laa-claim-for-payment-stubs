package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.DraftClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.JsonNodeMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.DraftClaimRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseBasedDraftClaimServiceTest {

  @Mock private DraftClaimRepository mockDraftClaimRepository;

  @Mock private DraftClaimMapper mockDraftClaimMapper;

  @Mock private JsonNodeMapper jsonNodeMapper;

  @InjectMocks private DatabaseBasedDraftClaimService draftClaimService;

  @Captor ArgumentCaptor<DraftClaimEntity> savedDraftClaimCaptor;

  @Test
  void shouldGetDraftClaimById() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    JsonNode payloadJson =
        new ObjectMapper()
            .readTree(
                """
                {
                  "someField": "someValue"
                }
                """);

    Map<String, Object> payloadMap = Map.of("someField", "someValue");

    DraftClaimEntity entity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaim draftClaim =
        DraftClaim.builder()
            .id(draftClaimId)
            .payload(payloadMap)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(entity));
    when(mockDraftClaimMapper.toDraftClaim(entity)).thenReturn(draftClaim);

    DraftClaim result = draftClaimService.getDraftClaim(draftClaimId, providerUserId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(draftClaimId);
    assertThat(result.getPayload()).isEqualTo(payloadMap);
  }

  @Test
  void shouldNotGetDraftClaimById_whenDraftClaimNotFoundThenThrowsException() {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.empty());

    assertThrows(
        DraftClaimNotFoundException.class,
        () -> draftClaimService.getDraftClaim(draftClaimId, providerUserId));

    verifyNoInteractions(mockDraftClaimMapper);
  }

  @Test
  void shouldCreateClaim() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    JsonNode payloadJson =
        new ObjectMapper()
            .readTree(
                """
                {
                  "someField": "someValue"
                }
                """);

    Map<String, Object> payloadMap = Map.of("someField", "someValue");

    DraftClaimPost requestBody =
        DraftClaimPost.builder().id(draftClaimId).payload(payloadMap).build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payloadJson)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimMapper.toDraftClaimEntity(requestBody)).thenReturn(savedEntity);

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(savedEntity);

    UUID result = draftClaimService.createDraftClaim(requestBody);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(draftClaimId);
  }

  @Test
  void shouldUpdateClaim() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

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

    DraftClaimPut requestBody =
        DraftClaimPut.builder().payload(newPayloadMap).providerUserId(providerUserId).build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(oldPayloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaimEntity updatedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(newPayloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaim updatedClaim =
        DraftClaim.builder()
            .id(draftClaimId)
            .payload(newPayloadMap)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(savedEntity));

    doNothing().when(mockDraftClaimMapper).updateEntity(requestBody, savedEntity);

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(updatedEntity);

    when(mockDraftClaimMapper.toDraftClaim(updatedEntity)).thenReturn(updatedClaim);

    DraftClaim result = draftClaimService.updateDraftClaim(requestBody, draftClaimId);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(updatedClaim);

    verify(mockDraftClaimRepository).save(savedEntity);
  }

  @Test
  void shouldPatchClaimWhenFieldsDefined() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

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

    DraftClaimPatch requestBody = DraftClaimPatch.builder().payload(newPayloadMap).build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(oldPayloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaimEntity updatedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(newPayloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaim updatedClaim =
        DraftClaim.builder()
            .id(draftClaimId)
            .payload(newPayloadMap)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(savedEntity));

    when(jsonNodeMapper.toJsonNode(newPayloadMap)).thenReturn(newPayloadJson);

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(updatedEntity);

    when(mockDraftClaimMapper.toDraftClaim(updatedEntity)).thenReturn(updatedClaim);

    DraftClaim result =
        draftClaimService.patchDraftClaim(requestBody, draftClaimId, providerUserId);

    verify(mockDraftClaimRepository).save(savedDraftClaimCaptor.capture());

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(updatedClaim);
    assertThat(savedDraftClaimCaptor.getValue().getPayload()).isEqualTo(newPayloadJson);
  }

  @Test
  void shouldPatchClaimWhenFieldsUndefined() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    JsonNode oldPayloadJson =
        new ObjectMapper()
            .readTree(
                """
                {
                  "field": "oldValue"
                }
                """);

    Map<String, Object> oldPayloadMap = Map.of("field", "oldValue");

    DraftClaimPatch requestBody = DraftClaimPatch.builder().build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(oldPayloadJson)
            .providerUserId(providerUserId)
            .build();

    DraftClaim updatedClaim =
        DraftClaim.builder()
            .id(draftClaimId)
            .payload(oldPayloadMap)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(savedEntity));

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(savedEntity);

    when(mockDraftClaimMapper.toDraftClaim(savedEntity)).thenReturn(updatedClaim);

    DraftClaim result =
        draftClaimService.patchDraftClaim(requestBody, draftClaimId, providerUserId);

    verify(mockDraftClaimRepository).save(savedDraftClaimCaptor.capture());

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(updatedClaim);
    assertThat(savedDraftClaimCaptor.getValue().getPayload()).isEqualTo(oldPayloadJson);
  }

  @Test
  void shouldDeleteDraftClaim() throws Exception {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    JsonNode payloadJson =
        new ObjectMapper()
            .readTree(
                """
                {
                  "field": "oldValue"
                }
                """);

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payloadJson)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(savedEntity));

    doNothing()
        .when(mockDraftClaimRepository)
        .deleteByIdAndProviderUserId(draftClaimId, providerUserId);

    draftClaimService.deleteDraftClaim(draftClaimId, providerUserId);

    verify(mockDraftClaimRepository, times(1))
        .deleteByIdAndProviderUserId(draftClaimId, providerUserId);
  }

  @Test
  void shouldNotDeleteDraftClaimAndThrowExceptionIfDraftClaimDoesNotExist() {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.empty());

    assertThrows(
        DraftClaimNotFoundException.class,
        () -> draftClaimService.deleteDraftClaim(draftClaimId, providerUserId));

    verify(mockDraftClaimRepository, never())
        .deleteByIdAndProviderUserId(draftClaimId, providerUserId);
  }

  @Test
  void shouldGetAllDraftClaimsForProviderUser() {
    UUID providerUserId = UUID.randomUUID();

    UUID firstClaimId = UUID.randomUUID();
    UUID secondClaimId = UUID.randomUUID();

    DraftClaimEntity firstDraftClaimEntity =
        DraftClaimEntity.builder()
            .id(firstClaimId)
            .payload(null)
            .providerUserId(providerUserId)
            .build();

    DraftClaimEntity secondDraftClaimEntity =
        DraftClaimEntity.builder()
            .id(secondClaimId)
            .payload(null)
            .providerUserId(providerUserId)
            .build();

    DraftClaim firstDraftClaim =
        DraftClaim.builder().id(firstClaimId).payload(null).providerUserId(providerUserId).build();

    DraftClaim secondDraftClaim =
        DraftClaim.builder().id(secondClaimId).payload(null).providerUserId(providerUserId).build();

    Pageable pageable = PageRequest.of(1, 1);
    Page<DraftClaimEntity> page =
        new PageImpl<DraftClaimEntity>(List.of(firstDraftClaimEntity, secondDraftClaimEntity));

    when(mockDraftClaimRepository.findByProviderUserId(providerUserId, pageable)).thenReturn(page);
    when(mockDraftClaimMapper.toDraftClaim(firstDraftClaimEntity)).thenReturn(firstDraftClaim);
    when(mockDraftClaimMapper.toDraftClaim(secondDraftClaimEntity)).thenReturn(secondDraftClaim);

    Page<DraftClaim> result = draftClaimService.getAllDraftClaimsForProvider(providerUserId, 1, 1);

    assertThat(result).hasSize(2).contains(firstDraftClaim, secondDraftClaim);
  }
}
