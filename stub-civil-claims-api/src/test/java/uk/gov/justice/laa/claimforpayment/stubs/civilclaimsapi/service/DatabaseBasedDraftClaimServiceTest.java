package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.DraftClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.DraftClaimRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseBasedDraftClaimServiceTest {

  @Mock private DraftClaimRepository mockDraftClaimRepository;

  @Mock private DraftClaimMapper mockDraftClaimMapper;

  @InjectMocks private DatabaseBasedDraftClaimService draftClaimService;

  @Test
  void shouldGetDraftClaimById() {

    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();
    String payload =
        """
        {
          "someField": "someValue"
        }
        """;

    DraftClaimEntity entity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payload)
            .providerUserId(providerUserId)
            .build();

    DraftClaim draftClaim =
        DraftClaim.builder()
            .id(draftClaimId)
            .payload(payload)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(entity));
    when(mockDraftClaimMapper.toDraftClaim(entity)).thenReturn(draftClaim);

    DraftClaim result = draftClaimService.getDraftClaim(draftClaimId, providerUserId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(draftClaimId);
    assertThat(result.getPayload()).isEqualTo(payload);
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
  void shouldCreateClaim() {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();
    String payload =
        """
        {
          "someField": "someValue"
        }
        """;

    DraftClaimPost requestBody = DraftClaimPost.builder().id(draftClaimId).payload(payload).build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payload)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(savedEntity);

    UUID result = draftClaimService.createDraftClaim(requestBody, providerUserId);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(draftClaimId);
  }

  @Test
  void shouldUpdateClaim() {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();
    String payload =
        """
                    {
                      "someField": "someValue"
                    }
                    """;

    DraftClaimPut requestBody = DraftClaimPut.builder().payload(payload).build();

    DraftClaimEntity savedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payload)
            .providerUserId(providerUserId)
            .build();

    DraftClaimEntity updatedEntity =
        DraftClaimEntity.builder()
            .id(draftClaimId)
            .payload(payload)
            .providerUserId(providerUserId)
            .build();

    when(mockDraftClaimRepository.findByIdAndProviderUserId(draftClaimId, providerUserId))
        .thenReturn(Optional.of(savedEntity));

    when(mockDraftClaimRepository.save(any(DraftClaimEntity.class))).thenReturn(updatedEntity);

    UUID result = draftClaimService.updateDraftClaim(requestBody, draftClaimId, providerUserId);

    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(draftClaimId);
  }

  @Test
  void shouldDeleteDraftClaim() {
    UUID draftClaimId = UUID.randomUUID();
    UUID providerUserId = UUID.randomUUID();

    doNothing()
        .when(mockDraftClaimRepository)
        .deleteByIdAndProviderUserId(draftClaimId, providerUserId);

    draftClaimService.deleteDraftClaim(draftClaimId, providerUserId);

    verify(mockDraftClaimRepository, times(1))
        .deleteByIdAndProviderUserId(draftClaimId, providerUserId);
  }
}
