package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
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
   * @return the id of the created draft claim
   */
  UUID createDraftClaim(DraftClaimPost requestBody);

  /**
   * Updates a draft claim.
   *
   * @param requestBody the updated draft claim
   * @return the id of the updated draft claim
   */
  DraftClaim updateDraftClaim(DraftClaimPut requestBody, UUID draftClaimId);

  /**
   * Patches a draft claim.
   *
   * @param requestBody the updated draft claim fields
   * @return the updated draft claim
   */
  DraftClaim patchDraftClaim(DraftClaimPatch requestBody, UUID draftClaimId, UUID providerUserId);

  /**
   * Deletes a draft claim.
   *
   * @param draftClaimId the id of the draft claim to be deleted
   */
  void deleteDraftClaim(UUID draftClaimId, UUID providerUserId);

  /**
   * Gets all draft claims for a given provider user ID.
   *
   * @param providerUserId the ID of the provider user
   * @param pageNumber the page number to retrieve
   * @param pageSize the number of draft claims per page
   * @return a list of draft claims for the provider user
   */
  Page<DraftClaim> getAllDraftClaimsForProvider(UUID providerUserId, int pageNumber, int pageSize);
}
