package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MqttController {

    private final PayloadValidator payloadValidator;
    private final KafkaPublisher kafkaPublisher;

    public MqttController(PayloadValidator payloadValidator, KafkaPublisher kafkaPublisher) {
        this.payloadValidator = payloadValidator;
        this.kafkaPublisher = kafkaPublisher;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void receiveSensorData(Message<String> message) {
        String payload = message.getPayload();
        System.out.println("Mensagem MQTT recebida: " + payload);

        // 1. Valida e converte
        SensorData data = payloadValidator.validate(payload);

        // 2. Publica no Kafka
        kafkaPublisher.publish(data);
    }
}