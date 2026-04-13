package com.vineyard.ingestion.model;

import jakarta.persistence.*;

/**
 * Entidade JPA que mapeia a tabela 'sensor_types' do PostgreSQL.
 * Tabela de referência com tipos de sensor pré-preenchidos (SOIL_MOISTURE, AIR_TEMP, etc.).
 */
@Entity
@Table(name = "sensor_types")
public class SensorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    public SensorType() {
    }

    // --- Getters ---

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SensorType{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
