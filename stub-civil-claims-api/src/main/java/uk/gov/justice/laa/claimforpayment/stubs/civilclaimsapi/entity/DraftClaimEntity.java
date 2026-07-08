package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The entity class for draft claims. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DRAFT_CLAIMS")
public class DraftClaimEntity {
  @Id
  private UUID id;

  private String payload;

  @Column(name = "provider_user_id")
  private UUID providerUserId;
}
