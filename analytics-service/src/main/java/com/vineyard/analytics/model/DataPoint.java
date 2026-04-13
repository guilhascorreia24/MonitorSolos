package com.vineyard.analytics.model;

import java.time.Instant;

/**
 * Representa um ponto de dados individual para gráficos.
 */
public record DataPoint(
    Instant timestamp,
    Double value
) {
}
