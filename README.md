# MonitorSolos - Smart Vineyard IoT Platform

MonitorSolos is a complete microservices-based IoT platform developed to monitor vineyards in real-time. It collects sensor data (soil moisture, air temperature, air humidity), processes it, generates alerts for critical conditions (like high risk of diseases or bad conditions), and visualizes everything in a dashboard.

## 🏗 Architecture

The platform follows a microservices architecture communicating through an Event-Driven model (using Kafka) and RESTful APIs, with the following components:

- **IoT Devices / Sensor Simulation:** Python script (`sensor_simulation.py`) simulating sensor data.
- **MQTT Broker (Mosquitto):** Receives MQTT messages from IoT sensors.
- **MQTT Spring Boot Broker:** Subscribes to MQTT topics and publishes incoming telemetry to Apache Kafka.
- **Apache Kafka & Zookeeper:** Event streaming platform handling data distribution between microservices.
- **Ingestion Service:** Consumes Kafka streams and persists device metadata and raw data to PostgreSQL.
- **Analytics Service:** Processes time-series data, detects anomalies (like fungus risks based on temperature and humidity ranges), and sends real-time data to InfluxDB.
- **Alert Service:** Listens to analytical events and manages real-time alerts.
- **Device Service:** Manages IoT devices, configurations, and user data.
- **API Gateway:** A Spring Cloud Gateway providing a unified REST API entry point for the frontend, routing requests to the underlying services.
- **Databases:** 
  - PostgreSQL (Relational data, Devices, Alerts)
  - InfluxDB (Time-series data for analytics)
- **Monitoring & Visualization:**
  - Grafana (Backend dashboards connecting to Postgres and InfluxDB)
  - Dashboard WebApp (Next.js responsive dashboard for vineyard producers)

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed on your machine:
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- Python 3.8+ (for running the sensor simulator)

### 1. Environment Variables

To run the application, you need to configure the environment variables. A template is provided:

```bash
cp .env.example .env
```

Edit the `.env` file and securely set the passwords and tokens according to your needs. **Never commit the `.env` file to version control.**

### 2. Start the Infrastructure

The entire platform can be brought up using Docker Compose:

```bash
docker-compose up -d --build
```

This command will spin up the Mosquitto broker, Kafka, Zookeeper, the databases (Postgres, InfluxDB), the Core Microservices, the API Gateway, and the Next.js Dashboard.

### 3. Start Data Simulation

To simulate a sensor sending data to the MQTT broker, you can use the provided Python script. First, install the `paho-mqtt` dependency, then run the script:

```bash
pip install paho-mqtt
python sensor_simulation.py
```

This script will cyclically send "NORMAL", "RISK_MEDIUM", "RISK_HIGH", and "CRITICAL" states to the backend.

### 4. Accessing the Platform

Once everything is up and running, you can access the following services:

- **Producer Dashboard (Next.js):** [http://localhost:3100](http://localhost:3100)
- **API Gateway:** [http://localhost:8000](http://localhost:8000)
- **Grafana:** [http://localhost:3000](http://localhost:3000) (Use credentials from your `.env`)

## 📂 Project Structure

- `alert-service/` - Microservice handling alerts
- `analytics-service/` - Microservice for data analysis and condition monitoring
- `api-gateway/` - Spring Cloud API Gateway
- `baseDados/` - PostgreSQL Initialization scripts
- `dashboard-webapp/` - Next.js Producer Dashboard interface
- `device-service/` - Microservice managing device metadata
- `grafana/` - Grafana provisioning and dashboard configurations
- `ingestion-service/` - Microservice persisting raw sensor data
- `mosquitto/` - MQTT Broker configurations
- `mqttbroker/` - Spring Boot MQTT to Kafka bridging service
- `postgres/` - PostgreSQL related data (ensure to ignore volumes via `.gitignore`)
- `infrastructure/` - Kubernetes manifests (for production deployment)
- `docker-compose.yml` - Docker setup for local development

## 🔒 Security & Publishing

This repository is configured not to track sensitive data volumes or environment files (`.env`). All secret keys, passwords, and tokens should reside in the ignored `.env` file locally, or managed via Kubernetes Secrets in a production deployment.
