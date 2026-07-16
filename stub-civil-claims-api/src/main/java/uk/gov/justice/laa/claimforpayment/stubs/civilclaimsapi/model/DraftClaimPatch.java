package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request body for updating certain fields in a draft claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = DraftClaimPatch.BodyBuilder.class)
@Schema(name = "DraftClaimPatch", description = "Input model for updating a draft claim")
public class DraftClaimPatch implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("payload")
  @Schema(description = "payload")
  private Map<String, Object> payload;

  /** Builder for DraftClaimPatch. */
  @JsonPOJOBuilder(withPrefix = "")
  public static class BodyBuilder {}

  @AssertTrue(message = "At least one field must be supplied")
  public boolean hasUpdates() {
    return payload != null;
  }
}
