package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;

/** Repository for managing claim evidence entities. */
@Repository
public interface ClaimEvidenceRepository extends JpaRepository<ClaimEvidenceEntity, UUID> {}
