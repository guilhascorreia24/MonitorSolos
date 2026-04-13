package com.vineyard.ingestion.repository;

import com.vineyard.ingestion.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade SensorReading.
 * Persiste leituras individuais dos sensores no PostgreSQL.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
}
