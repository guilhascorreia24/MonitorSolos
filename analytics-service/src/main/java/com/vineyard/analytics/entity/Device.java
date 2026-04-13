package com.vineyard.analytics.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade JPA que mapeia a tabela 'devices' do PostgreSQL.
 * Utilizada apenas para leitura (lookup por nome) neste serviço.
 */
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String location;

    @Column(length = 50)
    private String status;

    @Column(name = "installation_date")
    private OffsetDateTime installationDate;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Device() {
    }

    // --- Getters ---

    public Integer getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Device{id=" + id + ", name='" + name + "', status='" + status + "'}";
    }
}
