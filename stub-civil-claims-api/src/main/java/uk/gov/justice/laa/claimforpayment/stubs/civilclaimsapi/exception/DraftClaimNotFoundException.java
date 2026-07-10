package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception;

/**
 * The exception thrown when claim not found.
 */
public class DraftClaimNotFoundException extends RuntimeException {

  /**
   * Constructor for DraftClaimNotFoundException.
   *
   * @param message the error message
   */
  public DraftClaimNotFoundException(String message) {
    super(message);
  }
}
