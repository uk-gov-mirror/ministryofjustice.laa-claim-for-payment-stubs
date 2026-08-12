package uk.gov.justice.laa.claimforpayment.stubs.accessdatastoreapi;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;


@SpringBootApplication
public class AccessDatastoreStubApplication {

    private final WireMockServer wireMockServer;

    public AccessDatastoreStubApplication(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    public static void main(String[] args) {
        SpringApplication.run(AccessDatastoreStubApplication.class, args);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        return new WireMockServer(options().port(8080));
    }

    @EventListener(ApplicationReadyEvent.class)
    void configureStubs() {

        wireMockServer.stubFor(
            get(urlPathEqualTo("/hello-world"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello World")
                )
        );
    }
}