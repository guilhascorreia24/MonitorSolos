package com.vineyard.analytics.model;

import java.time.Instant;

/**
 * Record DTO para deserialização dos eventos Kafka do tópico 'sensor-events'.
 * Estrutura idêntica ao SensorData do ingestion-service e do mqttbroker.
 */
public record SensorData(
    String deviceId,
    Instant timestamp,
    double soilMoisture,
    double airTemperature,
    double airHumidity
) {
}
