package uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

/** WireMock stubs. */
public class WireMockStubs {

  /**
   * The constructor for the WireMock stubs.
   *
   * @param wireMockServer the WireMock server.
   */
  public WireMockStubs(WireMockServer wireMockServer) {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/health"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody(
                    """
                    {
                       "status": "UP"
                    }
                    """)));

    wireMockServer.stubFor(
        get(urlPathMatching("/applications/[^/]+"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                    """
                    {
                      "applicationId": "{{request.pathSegments.[1]}}",
                      "laaReference": "LAA-123456",
                      "status": "APPLICATION_SUBMITTED",
                      "submittedAt": "2026-07-22T10:00:00Z",
                      "clientFirstName": "John",
                      "clientLastName": "Doe",
                      "matterType": "SPECIAL_CHILDREN_ACT"
                    }
                    """)
                    .withTransformers("response-template")));
  }
}
