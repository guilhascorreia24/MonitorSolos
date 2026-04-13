package com.vineyard.ingestion.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Entidade JPA que mapeia a tabela 'sensor_readings' do PostgreSQL.
 * Armazena cada leitura individual de um sensor.
 */
@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private Double value;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public SensorReading() {
    }

    /**
     * Construtor para criação de uma nova leitura a partir de um evento Kafka.
     */
    public SensorReading(Device device, SensorType sensorType, Double value, OffsetDateTime recordedAt) {
        this.device = device;
        this.sensorType = sensorType;
        this.value = value;
        this.recordedAt = recordedAt;
        this.createdAt = OffsetDateTime.now();
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "id=" + id +
                ", deviceId=" + (device != null ? device.getId() : null) +
                ", sensorType=" + (sensorType != null ? sensorType.getCode() : null) +
                ", value=" + value +
                ", recordedAt=" + recordedAt +
                '}';
    }
}
