package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request body for creating or updating a draft claim.
 *
 * <p>This model contains all necessary fields required to submit a draft claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = DraftClaimRequestBody.DraftRequestBodyBuilder.class)
@Schema(name = "DraftRequestBody", description = "Input model for creating or updating a claim")
public class DraftClaimRequestBody implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @JsonProperty("id")
  @Schema(description = "id")
  private UUID id;

  @NotNull
  @JsonProperty("payload")
  @Schema(description = "payload")
  private JsonNode payload;

  /** Builder for DraftRequestBody. */
  @JsonPOJOBuilder(withPrefix = "")
  public static class DraftRequestBodyBuilder {}
}
