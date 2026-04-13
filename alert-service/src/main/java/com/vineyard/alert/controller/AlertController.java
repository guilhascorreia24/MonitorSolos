package com.vineyard.alert.controller;

import com.vineyard.alert.entity.Alert;
import com.vineyard.alert.service.AlertManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertManager alertManager;

    public AlertController(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @GetMapping
    public List<Alert> listAlerts() {
        return alertManager.getAllAlerts();
    }

    @PatchMapping("/{id}/resolve")
    public void resolveAlert(@PathVariable Integer id) {
        alertManager.resolveAlertManually(id);
    }
}
