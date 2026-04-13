package com.vineyard.analytics.service;

import com.vineyard.analytics.model.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * Quando risco é detectado:
 * 1. Gera evento de alerta no Kafka (TRIGGER) com severity adequada (MEDIUM,
 * HIGH, CRITICAL)
 * 2. Quando as condições passam, gera evento de resolução (RESOLVE)
 */
@Service
public class FungalRiskService {

    private static final Logger log = LoggerFactory.getLogger(FungalRiskService.class);

    private final TimeSeriesService timeSeriesService;
    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;

    // Limiares configuráveis via application.yml
    private final double humidityHigh;
    private final double humidityMedium;
    private final double tempMin;
    private final double tempMax;
    private final double riskTempMin;
    private final double riskTempMax;
    private final int durationCritical;
    private final int durationHigh;
    private final int durationMedium;

    public FungalRiskService(TimeSeriesService timeSeriesService,
            KafkaTemplate<String, AlertEvent> kafkaTemplate,
            @Value("${fungal-risk.humidity-threshold-high}") double humidityHigh,
            @Value("${fungal-risk.humidity-threshold-medium}") double humidityMedium,
            @Value("${fungal-risk.temp-min}") double tempMin,
            @Value("${fungal-risk.temp-max}") double tempMax,
            @Value("${fungal-risk.risk-temp-min}") double riskTempMin,
            @Value("${fungal-risk.risk-temp-max}") double riskTempMax,
            @Value("${fungal-risk.duration-critical}") int durationCritical,
            @Value("${fungal-risk.duration-high}") int durationHigh,
            @Value("${fungal-risk.duration-medium}") int durationMedium) {
        this.timeSeriesService = timeSeriesService;
        this.kafkaTemplate = kafkaTemplate;
        this.humidityHigh = humidityHigh;
        this.humidityMedium = humidityMedium;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.riskTempMin = riskTempMin;
        this.riskTempMax = riskTempMax;
        this.durationCritical = durationCritical;
        this.durationHigh = durationHigh;
        this.durationMedium = durationMedium;
    }

    public void evaluate(String deviceId) {
        // 1. Verificar nível CRITICAL (Janela longa + Humidade Alta + Temp Ideal)
        if (checkRisk(deviceId, humidityHigh, riskTempMin, riskTempMax, durationCritical)) {
            sendAlertTrigger(deviceId, "CRITICAL", "Risco CRÍTICO de fungos detectado (exposição prolongada).");
            return;
        }

        // 2. Verificar nível HIGH (Janela média + Humidade Alta + Temp Ideal)
        if (checkRisk(deviceId, humidityHigh, riskTempMin, riskTempMax, durationHigh)) {
            sendAlertTrigger(deviceId, "HIGH", "Risco ALTO de fungos detectado.");
            return;
        }

        // 3. Verificar nível MEDIUM (Janela curta + Humidade Média + Temp Alerta)
        if (checkRisk(deviceId, humidityMedium, tempMin, tempMax, durationMedium)) {
            sendAlertTrigger(deviceId, "MEDIUM", "Atenção: Condições propensas ao desenvolvimento de fungos.");
            return;
        }

        // Se nenhum risco foi detectado, resolve alertas ativos
        log.debug("✅ Sem risco detectado para device '{}'", deviceId);
        sendAlertResolve(deviceId);
    }

    private boolean checkRisk(String deviceId, double humThreshold, double tMin, double tMax, int hours) {
        Double meanHumidity = timeSeriesService.queryMean(deviceId, "air_humidity", hours);
        Double meanTemperature = timeSeriesService.queryMean(deviceId, "air_temperature", hours);

        if (meanHumidity == null || meanTemperature == null)
            return false;

        long dataPoints = timeSeriesService.queryCount(deviceId, "air_humidity", hours);
        if (dataPoints < (hours * 2))
            return false; // Mínimo de 2 leituras por hora na janela

        return meanHumidity > humThreshold && meanTemperature >= tMin && meanTemperature <= tMax;
    }

    private void sendAlertTrigger(String deviceId, String severity, String message) {
        AlertEvent event = new AlertEvent(deviceId, severity, message, "TRIGGER", OffsetDateTime.now());
        kafkaTemplate.send("alerts", deviceId, event);
        log.warn("📢 Alert [{}] sent for device: {}", severity, deviceId);
    }

    private void sendAlertResolve(String deviceId) {
        AlertEvent event = new AlertEvent(deviceId, "LOW", "Condições de risco cessaram.", "RESOLVE",
                OffsetDateTime.now());
        kafkaTemplate.send("alerts", deviceId, event);

        log.info("📢 Evento de RESOLVE de alerta enviado para Kafka: {}", deviceId);
    }
}
