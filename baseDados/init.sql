-- Active: 1773598523315@@127.0.0.1@5432@smart_vineyard@public
-- Smart Vineyard Monitor - Database Initialization
-- PostgreSQL Schema based on BaseDados.md

-- Enable UUID extension if not available (Postgres 13+ has gen_random_uuid() built-in)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1️⃣ USER
-- Users of the system (producers, technicians)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    role VARCHAR(50) DEFAULT 'USER', -- ADMIN, USER
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2️⃣ DEVICE
-- Represents the physical IoT Sensor Node
CREATE TABLE devices (
    id SERIAL PRIMARY KEY,
    uuid UUID DEFAULT uuid_generate_v4() NOT NULL UNIQUE, -- Global unique identifier
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255), -- GPS coordinates or descriptive location
    status VARCHAR(50) DEFAULT 'OFFLINE', -- ONLINE, OFFLINE, MAINTENANCE
    installation_date TIMESTAMP WITH TIME ZONE,
    firmware_version VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3️⃣ SENSOR_TYPE
-- Reference table for sensor types and units
CREATE TABLE sensor_types (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE, -- e.g., SOIL_MOISTURE
    name VARCHAR(100) NOT NULL,       -- e.g., 'Soil Moisture'
    unit VARCHAR(20) NOT NULL,        -- e.g., '%'
    description TEXT
);

-- Seed initial data for Sensor Types
INSERT INTO sensor_types (code, name, unit, description) VALUES
('SOIL_MOISTURE', 'Soil Moisture', '%', 'Volumetric water content in the soil'),
('SOIL_TEMP', 'Soil Temperature', '°C', 'Temperature of the soil'),
('AIR_TEMP', 'Air Temperature', '°C', 'Ambient air temperature'),
('AIR_HUMIDITY', 'Air Humidity', '%', 'Relative humidity of the air');

-- 4️⃣ SENSOR_READING
-- Individual sensor readings (Note: High frequency data is also sent to InfluxDB)
CREATE TABLE sensor_readings (
    id BIGSERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES devices(id) ON DELETE CASCADE,
    sensor_type_id INTEGER REFERENCES sensor_types(id),
    value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index to speed up queries by device and time
CREATE INDEX idx_readings_device_time ON sensor_readings(device_id, recorded_at DESC);

-- 5️⃣ ALERT
-- Events like high fungal risk or dry soil
CREATE TABLE alerts (
    id SERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES devices(id) ON DELETE CASCADE,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH')),
    message TEXT NOT NULL,
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- View for Active Alerts (Syntactic sugar for queries)
CREATE VIEW active_alerts AS
SELECT * FROM alerts
WHERE resolved_at IS NULL;

-- Index for performance on active alerts
CREATE INDEX idx_alerts_unresolved ON alerts(device_id) WHERE resolved_at IS NULL;