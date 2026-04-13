package com.vineyard.analytics.controller;

import com.vineyard.analytics.model.DataPoint;
import com.vineyard.analytics.service.TimeSeriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*") // O Gateway já trata CORS, mas isto ajuda em dev direto
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final TimeSeriesService timeSeriesService;

    public AnalyticsController(TimeSeriesService timeSeriesService) {
        this.timeSeriesService = timeSeriesService;
    }

    /**
     * Retorna o histórico de um sensor específico.
     * Ex: GET /api/analytics/history/device-123?field=air_humidity&range=-24h
     */
    @GetMapping("/history/{deviceId}")
    public List<DataPoint> getHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "air_humidity") String field,
            @RequestParam(defaultValue = "-24h") String range) {
        log.info("📊 Requesting history: deviceId={}, field={}, range={}", deviceId, field, range);
        return timeSeriesService.getHistory(deviceId, field, range);
    }
}
