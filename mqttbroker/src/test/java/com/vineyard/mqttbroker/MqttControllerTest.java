package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttControllerTest {

    @Mock
    private PayloadValidator payloadValidator;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @InjectMocks
    private MqttController mqttController;

    @Test
    void shouldProcessValidMessageSuccessfully() {
        String payload = "{\"some\":\"json\"}";
        SensorData mockData = new SensorData("dev1", Instant.now(), 10, 20, 30);

        // Configura o comportamento do mock
        when(payloadValidator.validate(payload)).thenReturn(mockData);

        // Executa o método
        mqttController.receiveSensorData(new GenericMessage<>(payload));

        // Verifica se o validador foi chamado
        verify(payloadValidator).validate(payload);
        // Verifica se o publicador foi chamado com os dados validados
        verify(kafkaPublisher).publish(mockData);
    }

    @Test
    void shouldNotPublishWhenValidationFails() {
        String payload = "invalid";
        when(payloadValidator.validate(payload)).thenThrow(new RuntimeException("Validation Error"));

        try { mqttController.receiveSensorData(new GenericMessage<>(payload)); } catch (Exception ignored) {}

        verify(kafkaPublisher, never()).publish(any());
    }
}