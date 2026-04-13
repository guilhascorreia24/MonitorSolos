package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaPublisher kafkaPublisher;

    @Test
    void shouldPublishToCorrectTopicWithDeviceIdAsKey() {
        SensorData data = new SensorData("device-123", Instant.now(), 50.0, 22.0, 60.0);

        kafkaPublisher.publish(data);

        // Verifica se o send foi chamado com o tópico "sensor-events", a chave "device-123" e o objeto data
        verify(kafkaTemplate).send(eq("sensor-events"), eq("device-123"), eq(data));
    }
}