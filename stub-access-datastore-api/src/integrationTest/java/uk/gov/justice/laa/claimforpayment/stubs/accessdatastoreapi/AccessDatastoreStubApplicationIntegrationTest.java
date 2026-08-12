package uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi;

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
class AccessDatastoreStubApplicationIntegrationTest {

  @Autowired private WireMockServer wireMockServer;

  @Autowired private ObjectMapper objectMapper;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Test
  void contextLoads() {
    assertThat(wireMockServer.isRunning()).isTrue();
  }

  @Test
  void healthEndpointReturnsUp() throws Exception {
    String uri = "http://localhost:8092/health";

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode body = objectMapper.readTree(response.body());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(body.get("status").asString()).isEqualTo("UP");
  }

  @Test
  void getApplicationEndpointReturnsApplicationDetails() throws Exception {
    String applicationId = "123";

    String uri = String.format("http://localhost:8092/applications/%s", applicationId);

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode body = objectMapper.readTree(response.body());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(body.get("applicationId").asString()).isEqualTo(applicationId);
    assertThat(body.get("laaReference").asString()).isEqualTo("LAA-123456");
    assertThat(body.get("status").asString()).isEqualTo("APPLICATION_SUBMITTED");
    assertThat(body.get("submittedAt").asString()).isEqualTo("2026-07-22T10:00:00Z");
    assertThat(body.get("clientFirstName").asString()).isEqualTo("John");
    assertThat(body.get("clientLastName").asString()).isEqualTo("Doe");
    assertThat(body.get("matterType").asString()).isEqualTo("SPECIAL_CHILDREN_ACT");
  }
}
