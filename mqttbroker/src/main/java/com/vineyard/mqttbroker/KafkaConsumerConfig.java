package com.vineyard.mqttbroker;

import com.vineyard.mqttbroker.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Componente de teste para consumir e verificar mensagens do tópico Kafka.
 * Este listener irá imprimir no log qualquer mensagem que chegar no tópico 'sensor-events'.
 */
@Component
public class KafkaConsumerConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    // O groupId é lido do application.yml
    // O listener agora espera um objeto SensorData, graças à configuração do JsonDeserializer
    @KafkaListener(topics = "sensor-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(SensorData payload) {
        log.info("<<< [KAFKA] Mensagem de teste recebida: {}", payload);
    }
}