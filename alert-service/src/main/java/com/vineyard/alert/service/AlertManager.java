package com.vineyard.alert.service;

import com.vineyard.alert.entity.Alert;
import com.vineyard.alert.entity.Device;
import com.vineyard.alert.model.AlertEvent;
import com.vineyard.alert.repository.AlertRepository;
import com.vineyard.alert.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertManager {

    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationService notificationService;

    public AlertManager(AlertRepository alertRepository, DeviceRepository deviceRepository, NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.deviceRepository = deviceRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void processAlertEvent(AlertEvent event) {
        if ("TRIGGER".equals(event.type())) {
            handleTrigger(event);
        } else if ("RESOLVE".equals(event.type())) {
            handleResolve(event);
        }
    }

    private void handleTrigger(AlertEvent event) {
        Optional<Device> deviceOpt = deviceRepository.findByName(event.deviceId());
        if (deviceOpt.isEmpty()) {
            log.warn("Device '{}' not found. Cannot trigger alert.", event.deviceId());
            return;
        }

        Device device = deviceOpt.get();
        List<Alert> active = alertRepository.findByDeviceIdAndResolvedAtIsNull(device.getId());

        if (active.isEmpty()) {
            Alert alert = new Alert(device, event.severity(), event.message(), event.timestamp());
            alertRepository.save(alert);
            log.info("Alert registered: device={}, severity={}", event.deviceId(), event.severity());
            notificationService.sendNotification(event.message(), event.severity());
        } else {
            log.debug("Alert already active for device {}", event.deviceId());
        }
    }

    private void handleResolve(AlertEvent event) {
        Optional<Device> deviceOpt = deviceRepository.findByName(event.deviceId());
        if (deviceOpt.isEmpty()) return;

        List<Alert> active = alertRepository.findByDeviceIdAndResolvedAtIsNull(deviceOpt.get().getId());
        for (Alert alert : active) {
            alert.setResolvedAt(event.timestamp());
            alertRepository.save(alert);
            log.info("Alert resolved: id={}, device={}", alert.getId(), event.deviceId());
        }
    }

    @Transactional
    public void resolveAlertManually(Integer alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setResolvedAt(OffsetDateTime.now());
            alertRepository.save(alert);
            log.info("Alert {} resolved manually", alertId);
        });
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }
}
