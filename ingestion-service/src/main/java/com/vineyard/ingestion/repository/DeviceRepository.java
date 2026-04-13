package com.vineyard.ingestion.repository;

import com.vineyard.ingestion.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade Device.
 * Permite procurar dispositivos pelo nome (que corresponde ao deviceId do Kafka).
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    /**
     * Procura um dispositivo pelo nome.
     * O campo 'name' é usado como identificador lógico do sensor (ex: "sensor-simulated-001").
     */
    Optional<Device> findByName(String name);
}
