package com.vineyard.device.service;

import com.vineyard.device.entity.Device;
import com.vineyard.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceManager {

    private final DeviceRepository deviceRepository;

    public DeviceManager(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceByUuid(UUID uuid) {
        return deviceRepository.findByUuid(uuid);
    }

    @Transactional
    public Device createDevice(Device device) {
        if (device.getUuid() == null) {
            device.setUuid(UUID.randomUUID());
        }
        device.setCreatedAt(OffsetDateTime.now());
        device.setUpdatedAt(OffsetDateTime.now());
        if (device.getStatus() == null) {
            device.setStatus("OFFLINE");
        }
        return deviceRepository.save(device);
    }

    @Transactional
    public Optional<Device> updateDevice(UUID uuid, Device updatedData) {
        return deviceRepository.findByUuid(uuid).map(device -> {
            if (updatedData.getName() != null) device.setName(updatedData.getName());
            if (updatedData.getLocation() != null) device.setLocation(updatedData.getLocation());
            if (updatedData.getStatus() != null) device.setStatus(updatedData.getStatus());
            if (updatedData.getFirmwareVersion() != null) device.setFirmwareVersion(updatedData.getFirmwareVersion());
            device.setUpdatedAt(OffsetDateTime.now());
            return deviceRepository.save(device);
        });
    }

    @Transactional
    public boolean deleteDevice(UUID uuid) {
        return deviceRepository.findByUuid(uuid).map(device -> {
            deviceRepository.delete(device);
            return true;
        }).orElse(false);
    }
}
