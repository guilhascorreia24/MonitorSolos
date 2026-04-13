package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "sensor-events";

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(SensorData data) {
        // Usa o deviceId como chave para garantir ordem de partição no Kafka
        kafkaTemplate.send(TOPIC, data.deviceId(), data);
        System.out.println("Evento publicado no Kafka: " + data);
    }
}