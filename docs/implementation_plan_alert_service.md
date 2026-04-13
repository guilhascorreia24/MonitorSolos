# Alert Service — Implementation Plan

## Objetivo

Criar o **Alert Service**, o terceiro microserviço da camada backend. Este serviço será o responsável central pelo ciclo de vida dos alertas, incluindo:

1. **Consumir eventos de alerta** do Kafka (tópico `alerts`, consumer group `alert-group`).
2. **Persistir alertas** na tabela `alerts` do PostgreSQL.
3. **Enviar notificações** (simuladas via logs) quando um novo alerta é registado.
4. **Expor uma API REST** para permitir que dashboards visualizem e resolvam alertas.

Além disso, o **Analytics Service** será refatorado para deixar de escrever diretamente na base de dados de alertas e passar a publicar eventos no Kafka.

## User Review Required

> [!IMPORTANT]
> O **Alert Service** passará a ser o proprietário da tabela `alerts`. O **Analytics Service** perderá o acesso de escrita a essa tabela.
> O serviço correrá na porta **8083**.

## Proposed Changes

### Refatoração: Analytics Service

#### [MODIFY] [Analytics Service (pom.xml)](file:///home/guilherme/repos/consumeApp/MonitorSolos/analytics-service/pom.xml)
- Nenhuma mudança necessária nas dependências (já possui `spring-kafka`).

#### [MODIFY] [Analytics Service (application.yml)](file:///home/guilherme/repos/consumeApp/MonitorSolos/analytics-service/src/main/resources/application.yml)
- Adicionar configuração de produtor Kafka para o tópico `alerts`.

#### [MODIFY] [Analytics Service (FungalRiskService.java)](file:///home/guilherme/repos/consumeApp/MonitorSolos/analytics-service/src/main/java/com/vineyard/analytics/service/FungalRiskService.java)
- Substituir `AlertRepository` por `KafkaTemplate<String, AlertEvent>`.
- Publicar evento no tópico `alerts` quando o risco for detectado ou resolvido.

---

### Novo Serviço: Alert Service

```
alert-service/
├── Dockerfile
├── pom.xml
├── src/main/java/com/vineyard/alert/
│   ├── AlertServiceApplication.java      # Main class
│   ├── model/
│   │   └── AlertEvent.java               # DTO para eventos Kafka
│   ├── entity/
│   │   ├── Alert.java                    # Entidade JPA (tabela alerts)
│   │   └── Device.java                   # Entidade JPA (read-only lookup)
│   ├── repository/
│   │   ├── AlertRepository.java          # JPA repo
│   │   └── DeviceRepository.java         # JPA repo
│   ├── kafka/
│   │   └── AlertEventConsumer.java       # Consome tópico 'alerts'
│   ├── service/
│   │   ├── AlertManager.java             # Lógica de negócio (registo e resolução)
│   │   └── NotificationService.java      # Simulação de envio de notificações
│   └── controller/
│       └── AlertController.java          # API REST (GET /api/alerts, POST /resolve)
└── src/main/resources/
    └── application.yml                   # Config: Kafka, PostgreSQL
```

#### [NEW] `pom.xml`
- Spring Boot 3.2.3, Java 21 (ou 17).
- Dependências: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-kafka`, `postgresql`.

#### [NEW] `AlertEvent.java`
- Record para transportar o alerta: `deviceId`, `severity`, `message`, `type` (TRIGGER/RESOLVE), `timestamp`.

#### [NEW] `AlertController.java`
- `GET /api/alerts`: Listar alertas (filtráveis por ativos/resolvidos).
- `PATCH /api/alerts/{id}/resolve`: Marcar um alerta como resolvido manualmente.

#### [NEW] `NotificationService.java`
- Mock que imprime no log: `[NOTIFICATION] Alerta HIGH para sensor-01: Risco de fungos`.

---

### Infraestrutura Docker

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
- Adicionar o serviço `alert-service`.
- Variáveis de ambiente para Kafka e Postgres.

## Open Questions

> [!NOTE]
> Devo implementar algum mecanismo de notificação externa real (ex: Email via Mailtrap) ou a simulação por logs é suficiente para esta fase?
> Vou assumir apenas LOGS para agora, conforme as instruções implícitas.

## Verification Plan

### Automated Tests
1. **Build**: `./mvnw clean package` no diretório do novo serviço.
2. **Kafka Flow**: Disparar um risco no Analytics e verificar se o Alert Service consome e persiste.
3. **API Check**: `curl http://localhost:8083/api/alerts` deve retornar a lista de alertas criados.

### Manual Verification
1. Observar os logs do `alert-service` para ver a "notificação" a ser enviada.
2. Usar o simulador de sensores para gerar condições de risco e validar que o alerta aparece no Postgres via query SQL.
