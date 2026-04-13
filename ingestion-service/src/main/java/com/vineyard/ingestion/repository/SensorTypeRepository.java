package com.vineyard.ingestion.repository;

import com.vineyard.ingestion.model.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade SensorType (tabela de referência).
 * Os tipos são pré-preenchidos pelo init.sql: SOIL_MOISTURE, SOIL_TEMP, AIR_TEMP, AIR_HUMIDITY.
 */
@Repository
public interface SensorTypeRepository extends JpaRepository<SensorType, Integer> {

    /**
     * Procura um tipo de sensor pelo código (ex: "SOIL_MOISTURE", "AIR_TEMP").
     */
    Optional<SensorType> findByCode(String code);
}
