package com.vineyard.analytics.service;

import com.vineyard.analytics.model.SensorData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Serviço principal de analytics.
 * Orquestra o processamento de cada evento recebido do Kafka:
 *
 * 1. Escreve dados no InfluxDB (séries temporais)
 * 2. Avalia risco de fungos (consulta histórico + regras)
 * 3. Gera alertas se necessário (PostgreSQL)
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TimeSeriesService timeSeriesService;
    private final FungalRiskService fungalRiskService;

    public AnalyticsService(TimeSeriesService timeSeriesService,
                            FungalRiskService fungalRiskService) {
        this.timeSeriesService = timeSeriesService;
        this.fungalRiskService = fungalRiskService;
    }

    /**
     * Processa um evento de sensor recebido do Kafka.
     *
     * @param data dados do sensor deserializados
     */
    public void processEvent(SensorData data) {
        log.debug(">>> Processando evento analytics: deviceId={}, timestamp={}", data.deviceId(), data.timestamp());

        // 1. Guardar série temporal no InfluxDB
        try {
            timeSeriesService.writePoint(data);
        } catch (Exception e) {
            log.error("❌ Erro ao escrever no InfluxDB: {} — {}", data.deviceId(), e.getMessage(), e);
            // Não falha todo o processamento — continua com avaliação de risco
        }

        // 2. Avaliar risco de fungos (consulta InfluxDB + gera alerta se necessário)
        try {
            fungalRiskService.evaluate(data.deviceId());
        } catch (Exception e) {
            log.error("❌ Erro ao avaliar risco de fungos: {} — {}", data.deviceId(), e.getMessage(), e);
        }

        log.info("✅ Evento analytics processado para device '{}'", data.deviceId());
    }
}
