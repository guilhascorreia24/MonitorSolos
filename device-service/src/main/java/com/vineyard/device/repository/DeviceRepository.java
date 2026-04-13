package com.vineyard.device.repository;

import com.vineyard.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByUuid(UUID uuid);
}
