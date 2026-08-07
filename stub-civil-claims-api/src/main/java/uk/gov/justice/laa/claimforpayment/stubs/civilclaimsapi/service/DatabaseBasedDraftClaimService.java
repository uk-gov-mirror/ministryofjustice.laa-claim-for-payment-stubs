package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.DraftClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.JsonNodeMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.DraftClaimRepository;

/** Service class for handling claims requests. */
@RequiredArgsConstructor
@Service
@Slf4j
public class DatabaseBasedDraftClaimService implements DraftClaimServiceInterface {

  private final DraftClaimRepository draftClaimRepository;
  private final DraftClaimMapper draftClaimMapper;
  private final JsonNodeMapper jsonNodeMapper;

  /**
   * Gets a claim for a given id.
   *
   * @param draftClaimId the draft claim id
   * @return the requested claim
   */
  @Override
  public DraftClaim getDraftClaim(UUID draftClaimId, UUID providerUserId) {
    DraftClaimEntity draftClaimEntity = checkIfDraftClaimExists(draftClaimId, providerUserId);
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
    DraftClaimEntity savedDraftClaimEntity = draftClaimRepository.save(draftClaimEntity);
    return savedDraftClaimEntity.getId();
  }

  @Override
  @Transactional
  public DraftClaim updateDraftClaim(DraftClaimPut requestBody, UUID draftClaimId) {
    DraftClaimEntity draftClaimEntity =
        checkIfDraftClaimExists(draftClaimId, requestBody.getProviderUserId());
    if (!Objects.equals(draftClaimEntity.getVersion(), requestBody.getVersion())) {
      throw new ObjectOptimisticLockingFailureException(DraftClaimEntity.class, draftClaimId);
    }
    draftClaimMapper.updateEntity(requestBody, draftClaimEntity);
    log.debug("after mapping version=" + draftClaimEntity.getVersion());
    DraftClaimEntity savedDraftClaimEntity = draftClaimRepository.save(draftClaimEntity);
    return draftClaimMapper.toDraftClaim(savedDraftClaimEntity);
  }

  @Override
  @Transactional
  public DraftClaim patchDraftClaim(
      DraftClaimPatch requestBody, UUID draftClaimId, UUID providerUserId, Long version) {
    DraftClaimEntity draftClaimEntity = checkIfDraftClaimExists(draftClaimId, providerUserId);
    if (!Objects.equals(draftClaimEntity.getVersion(), version)) {
      throw new ObjectOptimisticLockingFailureException(DraftClaimEntity.class, draftClaimId);
    }
    if (requestBody.getPayload() != null) {
      JsonNode payload = jsonNodeMapper.toJsonNode(requestBody.getPayload());
      draftClaimEntity.setPayload(payload);
    }
    DraftClaimEntity savedDraftClaimEntity = draftClaimRepository.save(draftClaimEntity);
    return draftClaimMapper.toDraftClaim(savedDraftClaimEntity);
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

  @Override
  public Page<DraftClaim> getAllDraftClaimsForProvider(
      UUID providerUserId, int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<DraftClaimEntity> draftClaimPage =
        draftClaimRepository.findByProviderUserId(providerUserId, pageable);
    return draftClaimPage.map(draftClaimMapper::toDraftClaim);
  }

  private DraftClaimEntity checkIfDraftClaimExists(UUID draftClaimId, UUID providerUserId)
      throws DraftClaimNotFoundException {
    return draftClaimRepository
        .findByIdAndProviderUserId(draftClaimId, providerUserId)
        .orElseThrow(
            () ->
                new DraftClaimNotFoundException(
                    String.format(
                        "No draft claim found with id: %s for provider user: %s",
                        draftClaimId, providerUserId)));
  }
}
