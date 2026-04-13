# Arquitetura Geral do Sistema
```mermaid
flowchart LR

subgraph Edge
Device[IoT Sensor Node]
end

subgraph Ingestion
MQTTBroker[MQTT Broker]
end

subgraph Streaming
Kafka[Kafka / RabbitMQ]
end

subgraph Backend
API[Spring Boot API]
Processor[Data Processing Service]
end

subgraph Databases
Postgres[(PostgreSQL)]
Influx[(InfluxDB)]
end

subgraph Visualization
Grafana[Grafana Dashboard]
end

Device --> MQTTBroker
MQTTBroker --> Kafka

Kafka --> API
Kafka --> Processor

API --> Postgres
Processor --> Influx

Influx --> Grafana
```
# 2️⃣ Fluxo de Dados

Fluxo completo desde o sensor até ao dashboard.
```mermaid
sequenceDiagram

participant Sensor
participant MQTT
participant Kafka
participant Backend
participant DB
participant Grafana

Sensor->>MQTT: publish sensor data
MQTT->>Kafka: forward message
Kafka->>Backend: consume event
Backend->>DB: store metadata
Backend->>DB: store time series
DB->>Grafana: query data
Grafana->>User: dashboard
```
### Explicação do Fluxo de Dados

1) Sensor Nodes (ESP32 + Sensores)  
    - Cada dispositivo envia dados via MQTT (humidade, temperatura, etc.)

2) MQTT Broker  
    - Recebe dados em tempo real dos sensores  
    - Encaminha para Kafka para processamento escalável

3) Kafka / RabbitMQ (Streaming Layer)  
    - Garantia de entrega e desacoplamento
    - Permite processamento em tempo real e consumo por múltiplos serviços

4) Backend Microservices (Spring Boot, Java 21)  
    - Ingestion Service: valida e persiste dados em PostgreSQL
    - Analytics Service: calcula risco de fungos, recomenda rega, escreve séries temporais em InfluxDB
    - Alert Service: gera alertas de acordo com thresholds
    - Device & User Services: gestão de dispositivos e utilizadores

5) Databases
    - PostgreSQL: dados estruturados (dispositivos, alertas, usuários)
    - InfluxDB: séries temporais de leituras de sensores, usadas para dashboards

6) Visualização
    - Grafana: visualização de séries temporais e alertas
    - Web/Mobile App: interface para gestão de dispositivos e alertas

# 3️⃣ Microserviços Backend

A arquitetura backend pode ser dividida em serviços.
```mermaid
flowchart TB

GatewayService[MQTT Gateway Service]

IngestionService[Data Ingestion Service]

AnalyticsService[Analytics Service]

DeviceService[Device Management]

UserService[User Management]

GatewayService --> IngestionService
IngestionService --> AnalyticsService

DeviceService --> Postgres
UserService --> Postgres

AnalyticsService --> InfluxDB
```
# 4️⃣ Estrutura do Repositório

Uma estrutura que fica muito bem no GitHub.
```
smart-vineyard-iot

device-simulator/
    sensor-simulator

backend/

    api-service/
    ingestion-service/
    analytics-service
    device-service

infrastructure/

    docker/
    kubernetes/
    terraform

dashboard/

    grafana-dashboards

docs/

    architecture
    diagrams
```
# 5️⃣ Infraestrutura

Arquitetura cloud usando containers.
```mermaid
flowchart TB

subgraph AWS Cloud

K8s[Kubernetes Cluster]

MQTT[MQTT Broker]

Kafka[Kafka]

Spring[Spring Boot Services]

Postgres[(PostgreSQL)]

Influx[(InfluxDB)]

Grafana[Grafana]

end

Devices --> MQTT

MQTT --> Kafka

Kafka --> Spring

Spring --> Postgres
Spring --> Influx

Influx --> Grafana
```
6️⃣ Pipeline CI/CD

Com GitHub Actions.
```mermaid
flowchart LR

Dev[Developer Push]

GitHub[GitHub Repo]

CI[GitHub Actions]

Build[Build Docker Images]

Registry[Container Registry]

Deploy[Kubernetes Deploy]

Dev --> GitHub
GitHub --> CI
CI --> Build
Build --> Registry
Registry --> Deploy
```
# 7️⃣ Exemplo de Evento IoT

Mensagem enviada pelo sensor:
```
{
  "deviceId": "vineyard-sensor-01",
  "timestamp": "2026-03-15T10:30:00Z",
  "soilMoisture": 42.3,
  "airTemperature": 22.1,
  "airHumidity": 88.5
}
```
8️⃣ Processamento de Dados

O serviço analytics-service pode calcular:

risco de fungos

recomendação de rega

alertas

Exemplo de regra:
```
if humidity > 85
and temperature between 18 and 25
then risk = HIGH
```
# 9️⃣ Simulador de Sensores

Como isto é portfólio, podes criar um sensor simulator em Java.

Ele envia dados simulados via MQTT.
```
while(true) {

   SensorData data = new SensorData(
       randomSoilMoisture(),
       randomTemperature(),
       randomHumidity()
   );

   mqttClient.publish("vineyard/sensor", data);

   Thread.sleep(30000);
}
```

Isso permite:

demonstrar pipeline completo

gerar dados para Grafana

# 🔟 Dashboard Grafana

Exemplos de painéis:

- Humidade do solo

- Temperatura

- Humidade do ar

- Risco de fungos

- Histórico de sensores

# 1️⃣1️⃣ O que este projeto demonstra

Este projeto mostra competências em:

Backend

- Spring Boot

- Java 21

- Event-driven architecture

Streaming

- Kafka / RabbitMQ

- MQTT

Data

- PostgreSQL

- InfluxDB

DevOps

- Docker

- Kubernetes

- GitHub Actions

- Terraform

- AWS

💡 Dica MUITO importante para portfólio

Adiciona 3 coisas que impressionam recrutadores:

1️⃣ sensor simulator

gera dados realistas

2️⃣ dashboards reais

screenshots do Grafana

3️⃣ arquitetura bem documentada

diagramas como estes