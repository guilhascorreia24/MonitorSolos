package com.vineyard.analytics.kafka;

import com.vineyard.analytics.model.SensorData;
import com.vineyard.analytics.service.AnalyticsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer que escuta o tópico 'sensor-events'.
 * Utiliza o group-id 'analytics-group' (separado do 'ingestion-group')
 * para que ambos os serviços recebam os mesmos eventos independentemente.
 */
@Component
public class SensorEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SensorEventConsumer.class);

    private final AnalyticsService analyticsService;

    public SensorEventConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Listener do Kafka para o tópico 'sensor-events'.
     * Cada mensagem recebida é delegada ao AnalyticsService para processamento.
     */
    @KafkaListener(topics = "sensor-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(SensorData data) {
        log.info("<<< [KAFKA] Evento recebido no analytics: deviceId={}, timestamp={}",
                data.deviceId(), data.timestamp());

        try {
            analyticsService.processEvent(data);
        } catch (IllegalArgumentException e) {
            // Dados inválidos — loga o erro mas não faz retry
            log.error("❌ Evento rejeitado (dados inválidos): {} — Erro: {}", data.deviceId(), e.getMessage());
        } catch (Exception e) {
            // Erro inesperado — loga e relança para o Kafka fazer retry
            log.error("💥 Erro inesperado ao processar evento analytics: {} — {}",
                    data.deviceId(), e.getMessage(), e);
            throw e;
        }
    }
}
