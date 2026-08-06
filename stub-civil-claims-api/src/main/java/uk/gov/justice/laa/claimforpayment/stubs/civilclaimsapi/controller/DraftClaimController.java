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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.DraftClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.CreateDraftClaimResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPageResponse;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPatch;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPost;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.DraftClaimPut;
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
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Invalid provider user ID"),
      })
  @PostMapping
  public ResponseEntity<CreateDraftClaimResponse> createDraftClaim(
      @Parameter(description = "Draft claim input data", required = true) @Valid @RequestBody
          DraftClaimPost requestBody) {

    UUID draftClaimId = draftClaimService.createDraftClaim(requestBody);
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
        @ApiResponse(responseCode = "403", description = "Invalid provider user ID"),
        @ApiResponse(
            responseCode = "404",
            description = "Draft claim not found",
            content = @Content)
      })
  @GetMapping("/{draftClaimId}")
  public ResponseEntity<DraftClaim> getDraftClaim(
      @Parameter(description = "ID of the draft claim to retrieve", required = true) @PathVariable
          UUID draftClaimId,
      @AuthenticationPrincipal Jwt jwt) {

    UUID providerUserId = getProviderUserId(jwt);
    log.debug("Fetching draft claim with ID: {}", draftClaimId);
    try {
      DraftClaim draftClaim = draftClaimService.getDraftClaim(draftClaimId, providerUserId);
      return ResponseEntity.ok(draftClaim);
    } catch (DraftClaimNotFoundException e) {
      log.error(e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Retrieves all draftclaims for the user.
   *
   * @return a list of all draft claims for the user
   */
  @Operation(summary = "Get paged draft claims for the authenticated user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paged list of draft claims linked to a provider user",
            content = @Content(schema = @Schema(implementation = DraftClaimPageResponse.class)))
      })
  @PreAuthorize(
      "hasAuthority('SCOPE_' + @environment.getProperty('app.security.authorities.claims-write'))")
  @GetMapping
  public ResponseEntity<DraftClaimPageResponse> getDraftClaims(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int limit,
      @AuthenticationPrincipal Jwt jwt) {

    String id = jwt.getClaimAsString("USER_NAME");
    if (id == null || id.isBlank()) {
      throw new ResponseStatusException(FORBIDDEN, "providerUserId missing in token");
    }
    UUID providerUserId = UUID.fromString(id);
    log.debug("Fetching all draft claims for provider user " + providerUserId);

    Page<DraftClaim> draftClaims =
        draftClaimService.getAllDraftClaimsForProvider(providerUserId, page, limit);

    DraftClaimPageResponse response;

    if (draftClaims == null || draftClaims.isEmpty()) {
      log.debug("No draft claims found for provider user " + providerUserId);
      response = new DraftClaimPageResponse(List.of(), page, limit, 0, 0);
    } else {
      log.debug(
          "Found {} draft claims for provider user {}",
          draftClaims.getNumberOfElements(),
          providerUserId);
      response =
          new DraftClaimPageResponse(
              draftClaims.toList(),
              page,
              limit,
              draftClaims.getTotalElements(),
              draftClaims.getTotalPages());
    }
    return ResponseEntity.ok(response);
  }

  /**
   * Updates an existing draft by its ID.
   *
   * @param requestBody the updated draft data
   * @return a response entity with no content if update is successful
   */
  @Operation(summary = "Update a claim")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Draft claim updated successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Invalid provider user ID"),
        @ApiResponse(
            responseCode = "404",
            description = "Draft claim not found",
            content = @Content)
      })
  @PutMapping("/{draftClaimId}")
  public ResponseEntity<Void> updateDraftClaim(
      @Parameter(description = "Updated claim data", required = true) @Valid @RequestBody
          DraftClaimPut requestBody,
      @Parameter(description = "ID of the draft claim to update", required = true) @PathVariable
          UUID draftClaimId) {

    log.debug("Updating draft claim with ID: {}", draftClaimId);
    try {
      draftClaimService.updateDraftClaim(requestBody, draftClaimId);
      return ResponseEntity.noContent().build();
    } catch (DraftClaimNotFoundException e) {
      log.error(e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Patches an existing draft by its ID.
   *
   * @param requestBody the updated draft data fields
   * @return a response entity if patch is successful
   */
  @Operation(summary = "Patch a claim")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Draft claim patched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DraftClaim.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Invalid provider user ID"),
        @ApiResponse(
            responseCode = "404",
            description = "Draft claim not found",
            content = @Content)
      })
  @PatchMapping("/{draftClaimId}")
  public ResponseEntity<DraftClaim> patchDraftClaim(
      @Parameter(description = "Updated claim fields", required = true) @Valid @RequestBody
          DraftClaimPatch requestBody,
      @Parameter(description = "ID of the draft claim to patch", required = true) @PathVariable
          UUID draftClaimId,
      @RequestHeader(value = "If-Match", required = true) String version,
      @AuthenticationPrincipal Jwt jwt) {

    UUID providerUserId = getProviderUserId(jwt);
    log.debug("Patching draft claim with ID: {}", draftClaimId);
    try {
      DraftClaim draftClaim =
          draftClaimService.patchDraftClaim(
              requestBody, draftClaimId, providerUserId, parseIfMatchHeader(version));
      return ResponseEntity.ok(draftClaim);
    } catch (DraftClaimNotFoundException e) {
      log.error(e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Deletes draft claim.
   *
   * @param draftClaimId the ID of the draft claim to delete
   * @return a response entity with no content if the draft claim is deleted successfully
   */
  @Operation(summary = "Delete a draft claim")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Draft claim deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Invalid provider user ID"),
        @ApiResponse(
            responseCode = "404",
            description = "Draft claim not found",
            content = @Content)
      })
  @DeleteMapping("/{draftClaimId}")
  public ResponseEntity<Void> deleteDraftClaim(
      @Parameter(description = "ID of the draft claim to delete", required = true) @PathVariable
          UUID draftClaimId,
      @AuthenticationPrincipal Jwt jwt) {

    UUID providerUserId = getProviderUserId(jwt);
    log.debug("Deleting draft claim with ID: {}", draftClaimId);
    try {
      draftClaimService.deleteDraftClaim(draftClaimId, providerUserId);
      return ResponseEntity.noContent().build();
    } catch (DraftClaimNotFoundException e) {
      log.error(e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  private UUID getProviderUserId(Jwt jwt) {
    String id = jwt.getClaimAsString("USER_NAME");
    if (id == null || id.isBlank()) {
      throw new ResponseStatusException(FORBIDDEN, "providerUserId missing in token");
    }
    return UUID.fromString(id);
  }

  private Long parseIfMatchHeader(String ifMatch) {
    try {
      // Strip surrounding quotes and whitespace
      String cleanVersion = ifMatch.replace("\"", "").trim();
      return Long.parseLong(cleanVersion);
    } catch (NumberFormatException e) {
      return 0L; // Default to 0 if parsing fails
    }
  }
}
