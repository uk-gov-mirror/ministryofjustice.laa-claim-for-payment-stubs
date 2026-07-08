package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;

/** The mapper between DraftClaimEntity and DraftClaim. */
@Mapper(componentModel = "spring")
public interface DraftClaimMapper {

  /**
   * Maps the given draft claim entity to a draft claim.
   *
   * @param entity the draft claim entity
   * @return the draft claim
   */
  DraftClaim toDraftClaim(DraftClaimEntity entity);
}
