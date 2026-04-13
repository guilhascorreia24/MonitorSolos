package com.vineyard.mqttbroker;

import java.time.Instant;

public record SensorData(
    String deviceId,
    Instant timestamp,
    double soilMoisture,
    double airTemperature,
    double airHumidity
) {
    // Validações adicionais ou métodos de conveniência podem ser adicionados aqui
}