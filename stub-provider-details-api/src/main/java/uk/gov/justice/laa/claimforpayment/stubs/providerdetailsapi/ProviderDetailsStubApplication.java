package uk.gov.justice.laa.claimforpayment.stubs.providerdetailsapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Entry point for the Spring Boot microservice application. */
@SpringBootApplication
public class ProviderDetailsStubApplication {

  /**
   * The application main method.
   *
   * @param args the application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(ProviderDetailsStubApplication.class, args);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  WireMockServer wireMockServer() {
    return new WireMockServer(
        WireMockConfiguration.options()
            .port(8093)
            .usingFilesUnderDirectory("src/main/resources/wiremock"));
  }
}
