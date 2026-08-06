package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;

/** The mapper between DraftClaimEntity and DraftClaim. */
@Mapper(componentModel = "spring", uses = JsonNodeMapper.class)
public interface DraftClaimMapper {

  /**
   * Maps the given draft claim entity to a draft claim.
   *
   * @param entity the draft claim entity
   * @return the draft claim
   */
  DraftClaim toDraftClaim(DraftClaimEntity entity);

  /**
   * Maps the given draft claim post request body to a draft claim entity.
   *
   * @param request the draft claim post request body
   * @return the draft claim entity
   */
  @Mapping(target = "version", ignore = true)
  DraftClaimEntity toDraftClaimEntity(DraftClaimPost request);

  /**
   * Maps the given draft claim put request body to a draft claim entity.
   *
   * @param request the draft claim put request body
   * @param entity the existing entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  void updateEntity(DraftClaimPut request, @MappingTarget DraftClaimEntity entity);
}
