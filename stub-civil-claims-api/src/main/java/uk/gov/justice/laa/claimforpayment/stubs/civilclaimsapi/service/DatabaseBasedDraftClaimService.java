package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.DraftClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.DraftClaimRepository;

/** Service class for handling claims requests. */
@RequiredArgsConstructor
@Service
public class DatabaseBasedDraftClaimService implements DraftClaimServiceInterface {

  private final DraftClaimRepository draftClaimRepository;
  private final DraftClaimMapper draftClaimMapper;

  /**
   * Gets a claim for a given id.
   *
   * @param draftClaimId the draft claim id
   * @return the requested claim
   */
  @Override
  public DraftClaim getDraftClaim(UUID draftClaimId, UUID providerUserId) {
    DraftClaimEntity draftClaimEntity = checkIfDraftClaimExists(draftClaimId, providerUserId);

    JsonNode payload = draftClaimEntity.getPayload();

    if (payload.isTextual()) {
      try {
        payload = new ObjectMapper().readTree(payload.textValue());
        draftClaimEntity.setPayload(payload);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    return draftClaimMapper.toDraftClaim(draftClaimEntity);
  }

  /**
   * Creates a claim.
   *
   * @param requestBody the claim to be created
   * @return the id of the created claim
   */
  @Override
  public UUID createDraftClaim(DraftClaimPost requestBody) {
    DraftClaimEntity draftClaimEntity = draftClaimMapper.toDraftClaimEntity(requestBody);
    draftClaimRepository.save(draftClaimEntity);
    return draftClaimEntity.getId();
  }

  @Override
  public DraftClaim updateDraftClaim(DraftClaimPut requestBody, UUID draftClaimId) {
    DraftClaimEntity draftClaimEntity =
        checkIfDraftClaimExists(draftClaimId, requestBody.getProviderUserId());
    draftClaimMapper.updateEntity(requestBody, draftClaimEntity);
    DraftClaimEntity savedDraftClaimEntity = draftClaimRepository.save(draftClaimEntity);
    return draftClaimMapper.toDraftClaim(savedDraftClaimEntity);
  }

  @Override
  public DraftClaim patchDraftClaim(
      DraftClaimPatch requestBody, UUID draftClaimId, UUID providerUserId) {
    DraftClaimEntity draftClaimEntity = checkIfDraftClaimExists(draftClaimId, providerUserId);
    if (requestBody.getPayload() != null) {
      JsonNode payload = draftClaimMapper.map(requestBody.getPayload());
      draftClaimEntity.setPayload(payload);
    }
    draftClaimRepository.save(draftClaimEntity);
    return draftClaimMapper.toDraftClaim(draftClaimEntity);
  }

  /**
   * Deletes a claim.
   *
   * @param draftClaimId the id of the claim to be deleted
   * @param providerUserId the id of the authenticated user
   */
  @Override
  public void deleteDraftClaim(UUID draftClaimId, UUID providerUserId) {
    checkIfDraftClaimExists(draftClaimId, providerUserId);
    draftClaimRepository.deleteByIdAndProviderUserId(draftClaimId, providerUserId);
  }

  private DraftClaimEntity checkIfDraftClaimExists(UUID draftClaimId, UUID providerUserId)
      throws DraftClaimNotFoundException {
    return draftClaimRepository
        .findByIdAndProviderUserId(draftClaimId, providerUserId)
        .orElseThrow(
            () ->
                new DraftClaimNotFoundException(
                    String.format("No draft claim found with id: %s", draftClaimId)));
  }
}
