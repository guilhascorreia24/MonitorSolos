package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayloadValidatorTest {

    private final PayloadValidator validator = new PayloadValidator();

    @Test
    void shouldValidateAndParseValidPayload() {
        String json = """
            {
                "deviceId": "sensor-001",
                "timestamp": "2023-10-05T10:00:00Z",
                "soilMoisture": 50.5,
                "airTemperature": 25.0,
                "airHumidity": 60.0
            }
            """;

        SensorData result = validator.validate(json);

        assertThat(result).isNotNull();
        assertThat(result.deviceId()).isEqualTo("sensor-001");
        assertThat(result.soilMoisture()).isEqualTo(50.5);
    }

    @Test
    void shouldThrowExceptionWhenDeviceIdIsMissing() {
        String json = """
            {
                "timestamp": "2023-10-05T10:00:00Z",
                "soilMoisture": 50.0,
                "airTemperature": 25.0,
                "airHumidity": 60.0
            }
            """;

        assertThatThrownBy(() -> validator.validate(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Device ID is required");
    }

    @Test
    void shouldThrowExceptionWhenTemperatureIsOutOfRange() {
        String json = """
            {
                "deviceId": "sensor-001",
                "timestamp": "2023-10-05T10:00:00Z",
                "soilMoisture": 50.0,
                "airTemperature": 100.0,
                "airHumidity": 60.0
            }
            """;

        assertThatThrownBy(() -> validator.validate(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Air temperature value must be between -20 and 50");
    }

    @Test
    void shouldThrowExceptionOnMalformedJson() {
        String invalidJson = "{ \"deviceId\": \"sensor-001\", \"broken... ";

        assertThatThrownBy(() -> validator.validate(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao processar JSON");
    }
}