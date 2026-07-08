package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.DraftClaimEntity;

/** Repository for managing draft entities. */
@Repository
public interface DraftClaimRepository extends JpaRepository<DraftClaimEntity, UUID> {

  Optional<DraftClaimEntity> findByProviderUserId(UUID draftClaimId, UUID providerUserId);
  
  Page<DraftClaimEntity> findByProviderUserId(UUID providerUserId, Pageable pageable);
}
