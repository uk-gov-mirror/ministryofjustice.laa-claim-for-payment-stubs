package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.UUID;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;

/** An interface to some method of managing draft claims. */
public interface DraftClaimServiceInterface {

  /**
   * Gets a draft claim for a given id and provider user id.
   *
   * @param draftClaimId the draft claim id
   * @param providerUserId the provider user id
   * @return the requested draft claim
   */
  DraftClaim getDraftClaim(UUID draftClaimId, UUID providerUserId);

  /**
   * Creates a draft claim.
   *
   * @param requestBody the draft claim to be created
   * @param providerUserId the provider user id
   * @return the id of the created draft claim
   */
  UUID createDraftClaim(DraftClaimPost requestBody, UUID providerUserId);

  /**
   * Updates a draft claim.
   *
   * @param requestBody the updated draft claim
   * @return the id of the updated draft claim
   */
  UUID updateDraftClaim(DraftClaimPut requestBody, UUID draftClaimId, UUID providerUserId);

  /**
   * Deletes a draft claim.
   *
   * @param draftClaimId the id of the draft claim to be deleted
   */
  void deleteDraftClaim(UUID draftClaimId, UUID providerUserId);
}
