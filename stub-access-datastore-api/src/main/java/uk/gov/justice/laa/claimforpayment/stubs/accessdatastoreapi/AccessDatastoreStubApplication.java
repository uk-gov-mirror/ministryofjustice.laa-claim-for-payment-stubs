package uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Entry point for the Spring Boot microservice application.
 */
@SpringBootApplication
public class AccessDatastoreStubApplication {

  /**
   * The application main method.
   *
   * @param args the application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(AccessDatastoreStubApplication.class, args);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  WireMockServer wireMockServer() {
    return new WireMockServer(
        com.github.tomakehurst.wiremock.core.WireMockConfiguration.options().port(8093));
  }

  @Bean
  WireMockStubs wireMockStubs(WireMockServer wireMockServer) {
    return new WireMockStubs(wireMockServer);
  }
}

class WireMockStubs {

  WireMockStubs(WireMockServer wireMockServer) {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/hello-world"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/plain").withBody("Hello World")));
  }
}
