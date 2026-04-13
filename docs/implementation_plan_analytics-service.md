# Analytics Service — Implementation Plan

## Objetivo

Criar o **Analytics Service**, o segundo microserviço da camada backend. Este serviço é responsável por:

1. **Consumir eventos do Kafka** (tópico `sensor-events`, consumer group próprio `analytics-group`)
2. **Guardar séries temporais no InfluxDB** (measurement `vineyard_sensors`)
3. **Calcular risco de fungos** (regra: humidity > 85% E temperatura entre 18–25°C durante > 6h)
4. **Gerar alertas** persistidos na tabela `alerts` do PostgreSQL

## User Review Required

> [!IMPORTANT]
> O InfluxDB será adicionado ao `docker-compose.yml` como novo serviço na porta `8086`. O bucket será `vineyard_data`, org `vineyard`, e o token de acesso será `analytics-token-dev`.

> [!IMPORTANT]
> O serviço irá correr na porta **8082** (ingestion usa 8081, broker usa 8080).

## Proposed Changes

### Infraestrutura Docker

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)

- Adicionar serviço **InfluxDB 2.7** com configuração automática (bucket, org, token)
- Adicionar volume `influxdb_data` para persistência
- Adicionar serviço **analytics-service** com dependências (kafka, postgres, influxdb)
- Variáveis de ambiente: Kafka, PostgreSQL, e InfluxDB

---

### Analytics Service — Estrutura do Projeto

```
analytics-service/
├── Dockerfile
├── pom.xml
├── src/main/java/com/vineyard/analytics/
│   ├── AnalyticsServiceApplication.java      # Main class
│   ├── model/
│   │   └── SensorData.java                   # DTO Kafka (idêntico ao ingestion)
│   ├── kafka/
│   │   └── SensorEventConsumer.java           # Kafka consumer (analytics-group)
│   ├── service/
│   │   ├── AnalyticsService.java              # Orquestrador principal
│   │   ├── TimeSeriesService.java             # Escrita no InfluxDB
│   │   └── FungalRiskService.java             # Cálculo de risco de fungos
│   ├── repository/
│   │   ├── AlertRepository.java               # JPA repo para alerts
│   │   └── DeviceRepository.java              # JPA repo para devices (lookup)
│   └── entity/
│       ├── Alert.java                         # Entidade JPA — tabela alerts
│       └── Device.java                        # Entidade JPA — tabela devices (read-only)
└── src/main/resources/
    └── application.yml                        # Config: Kafka, InfluxDB, PostgreSQL
```

---

### Ficheiros a Criar

#### [NEW] `pom.xml`
- Spring Boot 3.2.3, Java 17 (consistente com ingestion-service)
- Dependências:
  - `spring-boot-starter-web` (health/actuator)
  - `spring-boot-starter-data-jpa` (PostgreSQL para alerts)
  - `spring-kafka` (consumer)
  - `influxdb-client-java` (InfluxDB v2 client)
  - `postgresql` (driver)
  - `jackson-databind` + `jackson-datatype-jsr310`

#### [NEW] `AnalyticsServiceApplication.java`
- Classe main Spring Boot

#### [NEW] `SensorData.java`
- Record DTO idêntico ao do ingestion-service:
  ```java
  public record SensorData(String deviceId, Instant timestamp,
      double soilMoisture, double airTemperature, double airHumidity) {}
  ```

#### [NEW] `SensorEventConsumer.java`
- `@KafkaListener(topics = "sensor-events", groupId = "analytics-group")`
- Delega para `AnalyticsService.processEvent()`

#### [NEW] `AnalyticsService.java`
- Orquestrador principal — recebe `SensorData` e:
  1. Chama `TimeSeriesService.writePoint()` → InfluxDB
  2. Chama `FungalRiskService.evaluate()` → verifica risco
  3. Se risco HIGH → grava alerta no PostgreSQL

#### [NEW] `TimeSeriesService.java`
- Usa `InfluxDBClient` (v2 API)
- Escreve **Point** com:
  - measurement: `vineyard_sensors`
  - tags: `device_id`, `sensor_type`
  - fields: `soil_moisture`, `air_temperature`, `air_humidity`
  - timestamp do evento original
- Método para consultar dados das últimas N horas (usado pelo FungalRiskService)

#### [NEW] `FungalRiskService.java`
- Implementa a regra de risco:
  ```
  if air_humidity > 85
  and air_temperature between 18 and 25
  for more than 6 hours
  → risk = HIGH
  ```
- Query ao InfluxDB: consulta média de humidity e temperature das últimas 6h
- Se condições cumpridas → cria `Alert` no PostgreSQL com severity HIGH
- Controlo de duplicados: não gera alerta se já existe um ativo (não resolvido) para o mesmo device

#### [NEW] `Alert.java` (Entity)
- Mapeia tabela `alerts` existente (id, device_id, severity, message, triggered_at, resolved_at, created_at)

#### [NEW] `Device.java` (Entity)
- Mapeia tabela `devices` existente (read-only, para foreign key lookup)

#### [NEW] `AlertRepository.java`
- `JpaRepository<Alert, Integer>`
- Método `findByDeviceIdAndResolvedAtIsNull()` (verificar alertas ativos)

#### [NEW] `DeviceRepository.java`
- `JpaRepository<Device, Integer>`
- Método `findByName(String name)` (lookup por deviceId/name)

#### [NEW] `application.yml`
- server.port: 8082
- Kafka consumer config (analytics-group)
- InfluxDB config (url, token, org, bucket)
- PostgreSQL config (mesmo datasource do ingestion)
- Logging config

#### [NEW] `Dockerfile`
- Multi-stage build (idêntico ao ingestion-service)
- Stage 1: Maven build com JDK 17
- Stage 2: JRE 17 runtime

## Open Questions

> [!NOTE]
> Nenhuma questão em aberto. A arquitetura está bem definida no `step3.md` e segue o padrão já estabelecido pelo ingestion-service.

## Verification Plan

### Automated Tests

1. **Build**: `cd analytics-service && ./mvnw clean package -DskipTests` — deve compilar sem erros
2. **Docker Compose**: `docker compose up --build analytics-service` — deve levantar e conectar ao Kafka, InfluxDB e PostgreSQL
3. **InfluxDB verification**: Verificar se os dados são escritos no InfluxDB via CLI:
   ```bash
   docker exec influxdb influx query 'from(bucket:"vineyard_data") |> range(start: -1h)' --org vineyard --token analytics-token-dev
   ```
4. **PostgreSQL verification**: Verificar se alertas são criados quando as condições são cumpridas:
   ```bash
   docker exec smart_vineyard_postgres psql -U admin -d smart_vineyard -c "SELECT * FROM alerts;"
   ```

### Manual Verification
- Monitorizar logs do analytics-service via `docker logs -f analytics-service`
- Verificar que os eventos são consumidos e processados corretamente
- Confirmar que leituras aparecem no InfluxDB
