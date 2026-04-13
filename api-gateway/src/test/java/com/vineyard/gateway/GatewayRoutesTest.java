package com.vineyard.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "DEVICE_SERVICE_URI=http://localhost:${wiremock.server.port}",
                "ALERT_SERVICE_URI=http://localhost:${wiremock.server.port}",
                "ANALYTICS_SERVICE_URI=http://localhost:${wiremock.server.port}"
        })
@AutoConfigureWireMock(port = 0)
public class GatewayRoutesTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testDeviceServiceRouting() {
        stubFor(get(urlEqualTo("/api/devices/all"))
                .willReturn(aResponse()
                        .withBody("{\"message\": \"devices\"}")
                        .withHeader("Content-Type", "application/json")));

        webClient.get().uri("/api/devices/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("devices");
    }

    @Test
    public void testAlertServiceRouting() {
        stubFor(get(urlEqualTo("/api/alerts/active"))
                .willReturn(aResponse()
                        .withBody("{\"message\": \"alerts\"}")
                        .withHeader("Content-Type", "application/json")));

        webClient.get().uri("/api/alerts/active")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("alerts");
    }

    @Test
    public void testAnalyticsServiceRouting() {
        stubFor(get(urlEqualTo("/api/analytics/history"))
                .willReturn(aResponse()
                        .withBody("{\"message\": \"analytics\"}")
                        .withHeader("Content-Type", "application/json")));

        webClient.get().uri("/api/analytics/history")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("analytics");
    }

    @Test
    public void testCorsConfiguration() {
        webClient.options().uri("/api/devices/all")
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "*")
                .expectHeader().valueEquals("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
    }

    @Test
    public void testActuatorHealth() {
        webClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
}
