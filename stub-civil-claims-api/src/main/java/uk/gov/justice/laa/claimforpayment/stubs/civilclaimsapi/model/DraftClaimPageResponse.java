package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Represents a paginated response containing a list of draft claims along with pagination metadata
 * such as current.
 */
@Schema(name = "DraftClaimPageResponse", description = "Paged list of draft claims")
public record DraftClaimPageResponse(
    @Schema(description = "Draft claims in this page") List<DraftClaim> draftClaims,
    @Schema(description = "Current page index") int page,
    @Schema(description = "Maximum number of draft claims per page") int limit,
    @Schema(description = "Total number of draft claims across all pages") long total,
    @Schema(description = "Total number of pages available") int totalPages) {}
