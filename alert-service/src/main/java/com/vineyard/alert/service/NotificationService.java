package com.vineyard.alert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendNotification(String message, String severity) {
        // Mock notification (Email, SMS, Push, etc.)
        log.info("🔔 [NOTIFICATION] New alert registered! Severity: {}, Message: '{}'", severity, message);
    }
}
