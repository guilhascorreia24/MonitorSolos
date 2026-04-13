# Ingestion Service — Spring Boot Microservice

Criar o microserviço **ingestion-service** que consome eventos do Kafka (`sensor-events`), valida os dados recebidos e persiste metadados no PostgreSQL, conforme definido no [step3.md](file:///home/guilherme/repos/consumeApp/MonitorSolos/docs/device-broker/step3.md).

## Contexto

O projeto já possui:
- **mqttbroker** — recebe dados MQTT dos sensores e publica no tópico Kafka `sensor-events`
- **PostgreSQL** — base de dados `smart_vineyard` com tabelas `devices`, `sensor_readings`, `alerts`, etc. ([init.sql](file:///home/guilherme/repos/consumeApp/MonitorSolos/baseDados/init.sql))
- **Docker Compose** — infra com Mosquitto, Zookeeper, Kafka, PostgreSQL e InfluxDB
- O formato do evento Kafka é o record `SensorData(deviceId, timestamp, soilMoisture, airTemperature, airHumidity)`

## User Review Required

> [!IMPORTANT]
> O serviço será criado na pasta `ingestion-service/` ao lado de `mqttbroker/`. Usa o **mesmo stack**: Spring Boot 3.2.3, Java 17, Maven.

> [!IMPORTANT]
> O `group-id` do Kafka consumer será `ingestion-group` (diferente do `vineyard-group` do mqttbroker) para que ambos recebam os mesmos eventos independentemente.

> [!WARNING]
> O serviço fará **upsert** de dispositivos — se o `deviceId` já existir na tabela `devices`, atualiza `status` e `updated_at`; se não existir, cria um novo registo.

## Proposed Changes

### Novo Módulo: `ingestion-service/`

Estrutura completa do microserviço:

```
ingestion-service/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/vineyard/ingestion/
    │   ├── IngestionServiceApplication.java   — Main class
    │   ├── model/
    │   │   ├── SensorData.java                — Record (cópia do DTO Kafka)
    │   │   ├── Device.java                    — Entidade JPA (tabela devices)
    │   │   └── SensorEvent.java               — Entidade JPA (tabela sensor_event)
    │   ├── repository/
    │   │   ├── DeviceRepository.java          — Spring Data JPA
    │   │   └── SensorEventRepository.java     — Spring Data JPA
    │   ├── service/
    │   │   └── IngestionService.java          — Lógica de negócio (validação + persistência)
    │   └── kafka/
    │       └── SensorEventConsumer.java       — @KafkaListener no tópico sensor-events
    └── resources/
        └── application.yml                    — Configs (Kafka, PostgreSQL, JPA)
```

---

#### [NEW] pom.xml
Dependências:
- `spring-boot-starter-data-jpa` — JPA/Hibernate
- `spring-kafka` — Kafka consumer
- `postgresql` — Driver JDBC
- `jackson-databind` + `jackson-datatype-jsr310` — Serialização JSON
- `spring-boot-starter-validation` — Bean validation
- `spring-boot-starter-web` — Health endpoint básico (actuator)

---

#### [NEW] Dockerfile
Multi-stage build idêntico ao do mqttbroker (build com JDK 17, run com JRE 17).

---

#### [NEW] SensorData.java
Record idêntico ao do mqttbroker para deserialização Kafka:
```java
public record SensorData(String deviceId, Instant timestamp,
    double soilMoisture, double airTemperature, double airHumidity) {}
```

---

#### [NEW] Device.java — Entidade JPA
Mapeia a tabela `devices` existente no PostgreSQL:

| Campo | Coluna DB | Descrição |
|-------|-----------|-----------|
| `id` | `id SERIAL PK` | ID auto-gerado |
| `uuid` | `uuid UUID` | Identificador global |
| `name` | `name VARCHAR(100)` | Nome do dispositivo |
| `location` | `location VARCHAR(255)` | Localização |
| `status` | `status VARCHAR(50)` | ONLINE / OFFLINE / MAINTENANCE |
| `lastActivity` | `updated_at TIMESTAMP` | Última atividade (atualizado a cada evento) |
| `createdAt` | `created_at TIMESTAMP` | Data de criação |

---

#### [NEW] SensorEvent.java — Entidade JPA
Armazena cada evento recebido na tabela `sensor_readings`:

| Campo | Coluna DB | Descrição |
|-------|-----------|-----------|
| `id` | `id BIGSERIAL PK` | ID auto-gerado |
| `device` | `device_id INTEGER FK` | Referência ao device |
| `sensorType` | `sensor_type_id INTEGER FK` | Tipo do sensor |
| `value` | `value DOUBLE` | Valor da leitura |
| `recordedAt` | `recorded_at TIMESTAMP` | Timestamp do evento |

---

#### [NEW] DeviceRepository.java
```java
Optional<Device> findByName(String name);
```
Método para procurar dispositivo pelo `deviceId` (mapeado como `name`).

---

#### [NEW] SensorEventRepository.java
Interface simples `JpaRepository<SensorEvent, Long>`.

---

#### [NEW] IngestionService.java — Lógica Principal
1. **Validar** os dados do `SensorData` (ranges, nulls)
2. **Upsert device**: procurar por `name == deviceId`
   - Se **existe** → atualizar `status = ONLINE`, `updated_at = now()`
   - Se **não existe** → criar novo device com status `ONLINE`
3. **Persistir leituras**: criar 3 registos em `sensor_readings` (soilMoisture, airTemperature, airHumidity) com referências ao `sensor_types` existente

---

#### [NEW] SensorEventConsumer.java — Kafka Listener
```java
@KafkaListener(topics = "sensor-events", groupId = "ingestion-group")
public void consume(SensorData data) { ... }
```

---

#### [NEW] application.yml
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_vineyard
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: validate  # Não altera schema, apenas valida
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ingestion-group
      value-deserializer: JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.vineyard.ingestion"
```

---

### Modificação: docker-compose.yml (raiz)

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
Adicionar o serviço `ingestion-service` com:
- `build: ingestion-service/.`
- `depends_on: kafka, postgres` (do compose da base de dados)
- Porta `8081:8081`
- Environment variables para Kafka e PostgreSQL

## Open Questions

> [!IMPORTANT]
> O PostgreSQL está num **docker-compose separado** (`baseDados/docker-compose.yml`). Para o ingestion-service se conectar, há duas opções:
> 1. **Mover o PostgreSQL** para o docker-compose da raiz (recomendado — tudo num só compose)
> 2. **Usar rede partilhada** entre os dois composes
> 
> Qual abordagem prefere?

## Verification Plan

### Automated Tests
1. `./mvnw compile` — verificar que compila sem erros
2. Docker build: `docker build -t ingestion-service ./ingestion-service`

### Manual Verification
1. Subir infra: `docker compose up -d`
2. Executar `sensor_simulation.py` para gerar eventos MQTT
3. Verificar nos logs do ingestion-service que consome eventos Kafka
4. Verificar no PostgreSQL que os dados foram persistidos:
   ```sql
   SELECT * FROM devices;
   SELECT * FROM sensor_readings ORDER BY recorded_at DESC LIMIT 10;
   ```
