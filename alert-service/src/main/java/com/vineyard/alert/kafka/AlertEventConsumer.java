package com.vineyard.alert.kafka;

import com.vineyard.alert.model.AlertEvent;
import com.vineyard.alert.service.AlertManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertEventConsumer.class);

    private final AlertManager alertManager;

    public AlertEventConsumer(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @KafkaListener(topics = "alerts", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(AlertEvent event) {
        log.info("<<< [KAFKA] Alert event received: type={}, device={}, severity={}",
                event.type(), event.deviceId(), event.severity());

        try {
            alertManager.processAlertEvent(event);
        } catch (Exception e) {
            log.error("Error processing alert event: {}", e.getMessage(), e);
        }
    }
}
