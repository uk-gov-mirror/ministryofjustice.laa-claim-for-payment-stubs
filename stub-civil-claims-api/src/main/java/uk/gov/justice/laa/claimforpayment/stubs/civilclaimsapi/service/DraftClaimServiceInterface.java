package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.UUID;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimRequestBody;

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
  UUID createDraftClaim(DraftClaimRequestBody requestBody, UUID providerUserId);


  /**
   * Updates a draft claim.
   *
   * @param requestBody the updated draft claim
   * @return the id of the updated draft claim
   */
  UUID updateDraftClaim(DraftClaimRequestBody requestBody, UUID providerUserId);
}
