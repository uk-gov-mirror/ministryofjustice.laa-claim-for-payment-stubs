package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidenceRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItemRequestBody;

/** An interface to some method of managing claims. */
public interface ClaimServiceInterface {

  /**
   * Gets all claims.
   *
   * @return the list of claims
   */
  List<Claim> getClaims();

  /**
   * Gets a claim for a given id.
   *
   * @param claimId the claim id
   * @return the requested claim
   */
  Claim getClaim(UUID claimId);

  /**
   * Creates a claim.
   *
   * @param claimRequestBody the claim to be created
   * @return the id of the created claim
   */
  UUID createClaim(ClaimRequestBody claimRequestBody, UUID providerUserId);

  /**
   * Updates a claim.
   *
   * @param id the id of the claim to be updated
   * @param claimRequestBody the updated claim
   */
  void updateClaim(UUID id, ClaimRequestBody claimRequestBody);

  /**
   * Deletes a claim.
   *
   * @param id the id of the claim to be deleted
   */
  void deleteClaim(UUID id);

  /**
   * Gets all claims for a given provider user ID.
   *
   * @param providerUserId the ID of the provider user
   * @param pageNumber the page number to retrieve
   * @param pageSize the number of claims per page
   * @return a list of submissions for the provider user
   */
  Page<Claim> getAllClaimsForProvider(UUID providerUserId, int pageNumber, int pageSize);

  UUID addLineItemToClaim(UUID claimId, LineItemRequestBody lineItem);

  UUID addEvidenceToClaim(UUID claimId, ClaimEvidenceRequestBody requestBody);

  void deleteEvidenceFromClaim(UUID claimId, UUID evidenceId);

  void linkEvidenceToLineItem(UUID claimId, UUID lineItemId, List<UUID> evidenceIds);

  void unlinkEvidenceFromLineItem(UUID claimId, UUID lineItemId, UUID evidenceId);
}
