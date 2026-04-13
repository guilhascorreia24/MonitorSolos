package com.vineyard.ingestion.kafka;

import com.vineyard.ingestion.model.SensorData;
import com.vineyard.ingestion.service.IngestionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer que escuta o tópico 'sensor-events'.
 * Utiliza o group-id 'ingestion-group' (separado do 'vineyard-group' do mqttbroker)
 * para que ambos os serviços recebam os mesmos eventos independentemente.
 */
@Component
public class SensorEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SensorEventConsumer.class);

    private final IngestionService ingestionService;

    public SensorEventConsumer(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Listener do Kafka para o tópico 'sensor-events'.
     * Cada mensagem recebida é delegada ao IngestionService para validação e persistência.
     */
    @KafkaListener(topics = "sensor-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(SensorData data) {
        log.info("<<< [KAFKA] Evento recebido: deviceId={}, timestamp={}", data.deviceId(), data.timestamp());

        try {
            ingestionService.processEvent(data);
        } catch (IllegalArgumentException e) {
            // Dados inválidos — loga o erro mas não faz retry (dados malformados não vão melhorar)
            log.error("❌ Evento rejeitado (dados inválidos): {} — Erro: {}", data.deviceId(), e.getMessage());
        } catch (Exception e) {
            // Erro inesperado — loga e relança para o Kafka fazer retry
            log.error("💥 Erro inesperado ao processar evento: {} — {}", data.deviceId(), e.getMessage(), e);
            throw e;
        }
    }
}
