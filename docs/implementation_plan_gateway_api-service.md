# Implementation Plan - Testing API Gateway

The goal is to implement a robust testing suite for the `api-gateway` service to ensure that routing logic, CORS policies, and health checks are functioning correctly.

## Proposed Changes

### [gateway-api](file:///home/guilherme/repos/consumeApp/MonitorSolos/api-gateway)

#### [MODIFY] [pom.xml](file:///home/guilherme/repos/consumeApp/MonitorSolos/api-gateway/pom.xml)
- Add `spring-cloud-contract-wiremock` to allow mocking of downstream services during integration tests.

#### [NEW] [GatewayRoutesTest.java](file:///home/guilherme/repos/consumeApp/MonitorSolos/api-gateway/src/test/java/com/vineyard/gateway/GatewayRoutesTest.java)
- Create a comprehensive integration test suite using `WebTestClient` and `WireMock`.
- **Test Routes**:
    - Verify `/api/devices/**` routes to `device-service`.
    - Verify `/api/alerts/**` routes to `alert-service`.
    - Verify `/api/analytics/**` routes to `analytics-service`.
- **Test CORS**:
    - Validate that `OPTIONS` requests and cross-origin headers are correctly handled according to the `application.yml` configuration.
- **Test Actuator**:
    - Ensure `/actuator/health` and `/actuator/info` are accessible.

## Verification Plan

### Automated Tests
- Run `mvn test` in the `api-gateway` directory to execute the new integration tests.
- Verify that `WebTestClient` correctly identifies routed responses from WireMock.

### Manual Verification
- None required as automated integration tests will cover the gateway logic.
