package com.vineyard.device.controller;

import com.vineyard.device.entity.Device;
import com.vineyard.device.service.DeviceManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceManager deviceManager;

    public DeviceController(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @GetMapping
    public List<Device> list() {
        return deviceManager.getAllDevices();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Device> get(@PathVariable UUID uuid) {
        return deviceManager.getDeviceByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Device create(@RequestBody Device device) {
        return deviceManager.createDevice(device);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<Device> update(@PathVariable UUID uuid, @RequestBody Device device) {
        return deviceManager.updateDevice(uuid, device)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        if (deviceManager.deleteDevice(uuid)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
