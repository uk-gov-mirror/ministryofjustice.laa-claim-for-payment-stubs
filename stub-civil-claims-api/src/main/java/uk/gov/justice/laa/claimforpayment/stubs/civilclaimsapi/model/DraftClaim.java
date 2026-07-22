package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a draft claim. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftClaim implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  private UUID id;

  @Schema(description = "line items associated with the draft claim")
  @JsonProperty("lineItems")
  private List<LineItem> lineItems;

  @Schema(description = "evidence associated with the draft claim")
  @JsonProperty("evidence")
  private List<ClaimEvidence> evidence;

  @Schema(description = "payload")
  @JsonProperty("payload")
  private Map<String, Object> payload;

  @Schema(description = "ID of the provider user making the submission")
  @JsonProperty("providerUserId")
  private UUID providerUserId;
}
