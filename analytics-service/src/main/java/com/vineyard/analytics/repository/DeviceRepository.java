package com.vineyard.analytics.repository;

import com.vineyard.analytics.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a tabela 'devices'.
 * Utilizado apenas para lookup (leitura) neste serviço.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {

    /**
     * Encontra um dispositivo pelo nome (deviceId enviado pelo sensor).
     */
    Optional<Device> findByName(String name);
}
