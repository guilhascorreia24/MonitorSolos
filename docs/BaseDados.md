1️⃣ DEVICE

- Cada sensor físico instalado na vinha

- Contém informação da localização, status e data de instalação

- uuid garante unicidade global para integração com sensores via MQTT

2️⃣ SENSOR_READING  

- Guarda leituras individuais

- sensor_type identifica se é humidade do solo, temperatura, humidade do ar, etc.

- recorded_at para timestamp exato da leitura

- Observação: leituras de alta frequência também podem ser enviadas para InfluxDB para dashboards e análises temporais.

3️⃣ SENSOR_TYPE  

- Tabela de referência para tipos de sensores e suas unidades

- Útil para dashboards dinâmicos ou futuros sensores adicionados

4️⃣ ALERT 

- Guarda eventos importantes (ex: risco de fungos alto, solo seco)

- severity pode ser LOW / MEDIUM / HIGH

- triggered_at e resolved_at ajudam a monitorar histórico de alertas

5️⃣ USER  

- Usuários do sistema (produtores, técnicos)

- Relaciona quais DEVICEs cada usuário possui

## Arquitetura de Dados Completa — Smart Vineyard IoT  

```mermaid
flowchart LR

%% Edge Devices
subgraph "Edge / Sensor Nodes"
SensorNode1[Sensor Node 1]
SensorNode2[Sensor Node 2]
end

%% Comunicação
subgraph "Communication"
MQTT[MQTT Broker]
end

%% Streaming Layer
subgraph "Streaming"
Kafka[Kafka / RabbitMQ]
end

%% Backend Services
subgraph "Backend / Microservices"
IngestionService[Data Ingestion Service]
AnalyticsService[Analytics Service]
DeviceService[Device Management Service]
UserService[User Management Service]
AlertService[Alert Processing Service]
end

%% Databases
subgraph "Databases"
Postgres[(PostgreSQL)]
Influx[(InfluxDB)]
end

%% Visualization
subgraph "Dashboard / UI"
Grafana[Grafana Dashboard]
WebApp[Web App / Mobile App]
end

%% Edge to Communication
SensorNode1 --> MQTT
SensorNode2 --> MQTT

%% Communication to Streaming
MQTT --> Kafka

%% Streaming to Backend
Kafka --> IngestionService
Kafka --> AnalyticsService

%% Backend to Databases
IngestionService --> Postgres
DeviceService --> Postgres
UserService --> Postgres
AnalyticsService --> Influx
AlertService --> Postgres
AlertService --> Influx

%% Databases to Dashboard
Influx --> Grafana
Postgres --> WebApp
```