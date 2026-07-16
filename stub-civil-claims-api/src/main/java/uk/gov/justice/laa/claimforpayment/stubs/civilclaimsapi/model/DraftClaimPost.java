package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the request body for creating a draft claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = DraftClaimPost.BodyBuilder.class)
@Schema(name = "DraftClaimPost", description = "Input model for creating a draft claim")
public class DraftClaimPost implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @JsonProperty("id")
  @Schema(description = "id")
  private UUID id;

  @NotNull
  @JsonProperty("payload")
  @Schema(description = "payload")
  private Map<String, Object> payload;

  @NotNull
  @JsonProperty("providerUserId")
  @Schema(description = "provider user id")
  private UUID providerUserId;

  /** Builder for DraftClaimPost. */
  @JsonPOJOBuilder(withPrefix = "")
  public static class BodyBuilder {}
}
