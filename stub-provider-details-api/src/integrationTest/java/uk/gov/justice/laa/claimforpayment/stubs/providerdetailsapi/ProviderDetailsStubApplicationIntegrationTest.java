package uk.gov.justice.laa.claimforpayment.stubs.providerdetailsapi;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProviderDetailsStubApplicationIntegrationTest {

  @Autowired private WireMockServer wireMockServer;

  @Autowired private ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Test
  void contextLoads() {
    assertThat(wireMockServer.isRunning()).isTrue();
  }

  @Test
  void healthEndpointReturnsUp() throws Exception {
    String uri = "http://localhost:8093/health";

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode body = objectMapper.readTree(response.body());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(body.get("status").asString()).isEqualTo("UP");
  }

  @Test
  void getApplicationEndpointReturnsApplicationDetails() throws Exception {
    String firmId = "123";

    String uri = String.format("http://localhost:8093/api/v1/provider-firms/%s", firmId);

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode body = objectMapper.readTree(response.body());
    JsonNode firm = body.get("firm");

    assertThat(response.statusCode()).isEqualTo(200);

    assertThat(firm.get("advocateLevel").isNull()).isTrue();
    assertThat(firm.get("barCouncilRoll").isNull()).isTrue();
    assertThat(firm.get("ccmsFirmId").asInt()).isEqualTo(14604);
    assertThat(firm.get("companyHouseNumber").isNull()).isTrue();
    assertThat(firm.get("constitutionalStatus").asString()).isEqualTo("Limited Company");
    assertThat(firm.get("firmId").asString()).isEqualTo(firmId);
    assertThat(firm.get("firmName").asString()).isEqualTo("TEST FIRM");
    assertThat(firm.get("firmNumber").asString()).isEqualTo("123");
    assertThat(firm.get("firmType").asString()).isEqualTo("Legal Services Provider");
    assertThat(firm.get("highRiskSupplier").isNull()).isTrue();
    assertThat(firm.get("holdAllPaymentsFlag").asString()).isEqualTo("N");
    assertThat(firm.get("holdReason").isNull()).isTrue();
    assertThat(firm.get("indemnityReceivedDate").isNull()).isTrue();
    assertThat(firm.get("nonProfitOrganisation").asString()).isEqualTo("N");
    assertThat(firm.get("parentFirmId").isNull()).isTrue();
    assertThat(firm.get("smallBusinessFlag").asString()).isEqualTo("N");
    assertThat(firm.get("solicitorAdvocateYN").isNull()).isTrue();
    assertThat(firm.get("websiteUrl").isNull()).isTrue();
    assertThat(firm.get("womenOwnedFlag").asString()).isEqualTo("N");
  }
}
