package com.vineyard.alert.model;

import java.time.OffsetDateTime;

/**
 * Evento de alerta recebido via Kafka.
 */
public record AlertEvent(
    String deviceId,
    String severity,
    String message,
    String type, // TRIGGER or RESOLVE
    OffsetDateTime timestamp
) {
}
