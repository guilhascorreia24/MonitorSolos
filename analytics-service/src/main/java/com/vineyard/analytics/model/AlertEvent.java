package com.vineyard.analytics.model;

import java.time.OffsetDateTime;

/**
 * Evento de alerta para ser enviado via Kafka.
 */
public record AlertEvent(
    String deviceId,
    String severity,
    String message,
    String type, // TRIGGER or RESOLVE
    OffsetDateTime timestamp
) {
}
