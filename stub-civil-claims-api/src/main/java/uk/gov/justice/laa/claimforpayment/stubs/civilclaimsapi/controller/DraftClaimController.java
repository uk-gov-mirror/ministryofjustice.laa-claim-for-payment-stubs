package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.AddClaimEvidenceResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.AddLineItemResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidenceRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimPageResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.CreateClaimResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.CreateDraftClaimResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItemRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service.DraftClaimServiceInterface;

/** REST controller for managing draft claims. */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/drafts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Draft claims", description = "Operations related to draft provider claims")
public class DraftClaimController {

  private final DraftClaimServiceInterface draftClaimService;

  /**
   * Creates a new claim.
   *
   * @param requestBody the claim input data
   * @return a response entity with the location of the created claim
   */
  @Operation(summary = "Create a new draft claim")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Draft claim created successfully",
            headers =
                @Header(
                    name = "Location",
                    description = "URI of the created draft claim resource",
                    schema =
                        @Schema(
                            type = "string",
                            example = "/api/v1/drafts/541b1c4b-ec12-434e-a2a9-59daae30ca45")),
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateDraftClaimResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
      })
  @PostMapping
  public ResponseEntity<CreateDraftClaimResponse> createDraftClaim(
      @Parameter(description = "Draft claim input data", required = true) @Valid @RequestBody
          DraftClaimRequestBody requestBody,
      @AuthenticationPrincipal Jwt jwt) {

    String id = jwt.getClaimAsString("USER_NAME");
    if (id == null || id.isBlank()) {
      throw new ResponseStatusException(FORBIDDEN, "providerUserId missing in token");
    }
    UUID providerUserId = UUID.fromString(id);

    UUID draftClaimId = draftClaimService.createDraftClaim(requestBody, providerUserId);
    URI location = URI.create("/api/v1/drafts/" + draftClaimId);
    return ResponseEntity.created(location).body(new CreateDraftClaimResponse(draftClaimId));
  }

  /**
   * Retrieves a claim by its ID.
   *
   * @param draftClaimId the ID of the draft claim to retrieve
   * @return the claim with the specified ID
   */
  @Operation(summary = "Get a draft claim by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Draft claim found",
            content = @Content(schema = @Schema(implementation = DraftClaim.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Draft claim not found",
            content = @Content)
      })
  @GetMapping("/{draftClaimId}")
  public ResponseEntity<DraftClaim> getClaim(
      @Parameter(description = "ID of the draft claim to retrieve", required = true) @PathVariable
          UUID draftClaimId,
      @AuthenticationPrincipal Jwt jwt) {

    String id = jwt.getClaimAsString("USER_NAME");
    if (id == null || id.isBlank()) {
      throw new ResponseStatusException(FORBIDDEN, "providerUserId missing in token");
    }
    UUID providerUserId = UUID.fromString(id);
    log.debug("Fetching draft claim with ID: {}", draftClaimId);
    DraftClaim draftClaim = draftClaimService.getDraftClaim(draftClaimId, providerUserId);
    return ResponseEntity.ok(draftClaim);
  }
}
