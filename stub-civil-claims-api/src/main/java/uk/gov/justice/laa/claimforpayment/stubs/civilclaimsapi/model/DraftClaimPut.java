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
 * Represents the request body for updating a draft claim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(builder = DraftClaimPut.BodyBuilder.class)
@Schema(name = "DraftClaimPut", description = "Input model for updating a draft claim")
public class DraftClaimPut implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotNull
  @JsonProperty("payload")
  @Schema(description = "payload")
  private Map<String, Object> payload;

  @NotNull
  @JsonProperty("version")
  @Schema(description = "version")
  private Long version;

  @NotNull
  @JsonProperty("providerUserId")
  @Schema(description = "provider user id")
  private UUID providerUserId;

  /** Builder for DraftClaimPut. */
  @JsonPOJOBuilder(withPrefix = "")
  public static class BodyBuilder {}
}
