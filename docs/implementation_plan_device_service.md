# Device Service — Implementation Plan

## Objetivo

Criar o **Device Service**, o componente central para gestão do inventário de dispositivos IoT. Este serviço será o "dono" das tabelas `devices` e `users` no PostgreSQL, providenciando APIs internas e externas para gestão de metadados.

## User Review Required

> [!IMPORTANT]
> O **Device Service** assumirá a responsabilidade principal sobre a tabela `devices`. 
> O serviço correrá na porta **8084**.

## Proposed Changes

### Novo Serviço: Device Service

```
device-service/
├── Dockerfile
├── pom.xml
├── src/main/java/com/vineyard/device/
│   ├── DeviceServiceApplication.java      # Main class
│   ├── entity/
│   │   ├── Device.java                   # Entidade JPA
│   │   └── User.java                     # Entidade JPA
│   ├── repository/
│   │   ├── DeviceRepository.java          # JPA repo
│   │   └── UserRepository.java            # JPA repo
│   ├── service/
│   │   └── DeviceManager.java             # Lógica de negócio
│   └── controller/
│       └── DeviceController.java          # API REST (CRUD /api/devices)
└── src/main/resources/
    └── application.yml                   # Config: PostgreSQL
```

#### [NEW] `pom.xml`
- Spring Boot 3.2.3, Java 17.
- Dependências: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `postgresql`, `lombok` (opcional, mas usarei para manter o código limpo se disponível).

#### [NEW] `DeviceController.java`
- `GET /api/devices`: Listar todos os dispositivos.
- `GET /api/devices/{uuid}`: Detalhes de um dispositivo específico.
- `POST /api/devices`: Registar um novo dispositivo (com validação de UUID).
- `PUT /api/devices/{uuid}`: Atualizar localização, nome ou versão de firmware.
- `DELETE /api/devices/{id}`: Remover um dispositivo.

#### [NEW] `Device.java` & `User.java`
- Mapeamento JPA completo das tabelas definidas no `init.sql`.

---

### Infraestrutura Docker

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
- Adicionar o serviço `device-service`.
- Configurar dependências e variáveis de ambiente (DB_URL, etc).

## Open Questions

> [!NOTE]
> Devemos desativar a criação automática de dispositivos no **Ingestion Service**? 
> Sugiro manter por enquanto para não quebrar o fluxo de simulação, mas marcar como "depreciado" a favor da pré-registo via Device Service.

## Verification Plan

### Automated Tests
1. **Build**: `./mvnw clean package` no diretório do novo serviço.
2. **API CRUD**:
   - `POST /api/devices` -> Criar um device de teste.
   - `GET /api/devices` -> Verificar se aparece na lista.
   - `PUT /api/devices/{uuid}` -> Alterar a localização.

### Manual Verification
1. Verificar via `psql` se as alterações realizadas pela API refletem-se corretamente na tabela `devices`.
