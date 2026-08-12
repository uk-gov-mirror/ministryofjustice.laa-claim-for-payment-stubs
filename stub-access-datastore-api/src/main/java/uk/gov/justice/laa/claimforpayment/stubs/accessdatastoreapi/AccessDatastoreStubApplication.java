package uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi.wiremock.WireMockStubs;

/** Entry point for the Spring Boot microservice application. */
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
    return new WireMockServer(WireMockConfiguration.options().port(8092));
  }

  @Bean
  WireMockStubs wireMockStubs(WireMockServer wireMockServer) {
    return new WireMockStubs(wireMockServer);
  }
}
