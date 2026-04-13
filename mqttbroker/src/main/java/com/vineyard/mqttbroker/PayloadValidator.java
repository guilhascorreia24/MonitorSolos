package com.vineyard.mqttbroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vineyard.mqttbroker.SensorData;
import org.springframework.stereotype.Component;

@Component
public class PayloadValidator {

    private final ObjectMapper objectMapper;

    public PayloadValidator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public SensorData validate(String payload) {
        try {
            SensorData data = objectMapper.readValue(payload, SensorData.class);
            
            if (data.deviceId() == null || data.deviceId().isBlank()) {
                throw new IllegalArgumentException("Device ID is required");
            }
            
            if (data.timestamp() == null) {
                throw new IllegalArgumentException("Timestamp is required");
            }

            if (data.soilMoisture() < 0 || data.soilMoisture() > 100) {
                throw new IllegalArgumentException("Soil moisture value must be between 0 and 100");
            }


            if (data.airTemperature() < -20 || data.airTemperature() > 50) {
                throw new IllegalArgumentException("Air temperature value must be between -20 and 50");
            }

            
            return data;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Falha ao processar JSON do sensor: " + e.getMessage(), e);
        }
    }
}