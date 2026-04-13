package com.vineyard.ingestion.service;

import com.vineyard.ingestion.model.Device;
import com.vineyard.ingestion.model.SensorData;
import com.vineyard.ingestion.model.SensorReading;
import com.vineyard.ingestion.model.SensorType;
import com.vineyard.ingestion.repository.DeviceRepository;
import com.vineyard.ingestion.repository.SensorReadingRepository;
import com.vineyard.ingestion.repository.SensorTypeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Serviço principal de ingestão.
 * Responsável por:
 * 1. Validar dados recebidos do Kafka
 * 2. Fazer upsert do dispositivo (criar ou atualizar)
 * 3. Persistir as leituras dos sensores no PostgreSQL
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final DeviceRepository deviceRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorTypeRepository sensorTypeRepository;

    /**
     * Mapeamento entre os campos do SensorData e os códigos da tabela sensor_types.
     */
    private static final Map<String, String> SENSOR_FIELD_TO_TYPE_CODE = Map.of(
        "soilMoisture", "SOIL_MOISTURE",
        "airTemperature", "AIR_TEMP",
        "airHumidity", "AIR_HUMIDITY"
    );

    public IngestionService(DeviceRepository deviceRepository,
                            SensorReadingRepository sensorReadingRepository,
                            SensorTypeRepository sensorTypeRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorTypeRepository = sensorTypeRepository;
    }

    /**
     * Processa um evento de sensor recebido do Kafka.
     * Operação transacional: se qualquer passo falhar, faz rollback de tudo.
     */
    @Transactional
    public void processEvent(SensorData data) {
        log.debug(">>> Processando evento: {}", data);

        // 1. Validar dados
        validate(data);

        // 2. Upsert do dispositivo
        Device device = upsertDevice(data.deviceId(), data.timestamp());

        // 3. Persistir leituras dos sensores
        OffsetDateTime recordedAt = data.timestamp().atOffset(ZoneOffset.UTC);
        persistReading(device, "soilMoisture", data.soilMoisture(), recordedAt);
        persistReading(device, "airTemperature", data.airTemperature(), recordedAt);
        persistReading(device, "airHumidity", data.airHumidity(), recordedAt);

        log.info("✅ Evento processado com sucesso para device '{}' — 3 leituras persistidas", data.deviceId());
    }

    /**
     * Valida os dados do sensor. Lança exceção se inválidos.
     */
    private void validate(SensorData data) {
        if (data.deviceId() == null || data.deviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID é obrigatório");
        }

        if (data.timestamp() == null) {
            throw new IllegalArgumentException("Timestamp é obrigatório");
        }

        if (data.soilMoisture() < 0 || data.soilMoisture() > 100) {
            throw new IllegalArgumentException(
                "Soil moisture deve estar entre 0 e 100, recebido: " + data.soilMoisture());
        }

        if (data.airTemperature() < -40 || data.airTemperature() > 60) {
            throw new IllegalArgumentException(
                "Air temperature deve estar entre -40 e 60, recebido: " + data.airTemperature());
        }

        if (data.airHumidity() < 0 || data.airHumidity() > 100) {
            throw new IllegalArgumentException(
                "Air humidity deve estar entre 0 e 100, recebido: " + data.airHumidity());
        }
    }

    /**
     * Upsert do dispositivo:
     * - Se já existe (por nome/deviceId) → atualiza status para ONLINE e updated_at
     * - Se não existe → cria novo dispositivo com status ONLINE
     */
    private Device upsertDevice(String deviceId, Instant eventTimestamp) {
        Device device = deviceRepository.findByName(deviceId)
            .map(existing -> {
                existing.setStatus("ONLINE");
                existing.setUpdatedAt(OffsetDateTime.now());
                log.debug("📡 Device '{}' atualizado → status=ONLINE", deviceId);
                return existing;
            })
            .orElseGet(() -> {
                Device newDevice = new Device(deviceId);
                log.info("🆕 Novo device registado: '{}'", deviceId);
                return newDevice;
            });

        return deviceRepository.save(device);
    }

    /**
     * Persiste uma leitura individual do sensor na tabela sensor_readings.
     */
    private void persistReading(Device device, String fieldName, double value, OffsetDateTime recordedAt) {
        String typeCode = SENSOR_FIELD_TO_TYPE_CODE.get(fieldName);
        if (typeCode == null) {
            log.warn("⚠️ Tipo de sensor desconhecido para campo: {}", fieldName);
            return;
        }

        SensorType sensorType = sensorTypeRepository.findByCode(typeCode)
            .orElseThrow(() -> new IllegalStateException(
                "Sensor type '" + typeCode + "' não encontrado na tabela sensor_types. " +
                "Verifique se o init.sql foi executado."));

        SensorReading reading = new SensorReading(device, sensorType, value, recordedAt);
        sensorReadingRepository.save(reading);

        log.debug("💾 Leitura persistida: {} = {} {}", typeCode, value, sensorType.getUnit());
    }
}
