# Grafana Visualization — Implementation Plan

## Objetivo

Implementar a camada de visualização do projeto utilizando o **Grafana**. O objetivo é criar um dashboard de "nível premium" que permita ao produtor monitorizar as condições das vinhas em tempo real, visualizar históricos e acompanhar alertas críticos de risco de fungos.

## User Review Required

> [!IMPORTANT]
> O Grafana será exposto na porta **3000**. 
> As credenciais por defeito (que podem ser alteradas no primeiro login) serão: 
> - **User**: `admin`
> - **Pass**: `admin`

## Proposed Changes

### 1. Infraestrutura Docker

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
- Adicionar o serviço `grafana`.
- Montar volumes para persistência de dados e provisionamento automático.

### 2. Provisionamento de Data Sources

#### [NEW] `grafana/provisioning/datasources/datasources.yml`
Configurar automaticamente as conexões:
- **InfluxDB v2 (Flux)**: Para dados de sensores (humidade, temperatura).
- **PostgreSQL**: Para metadados de dispositivos e tabela de alertas.

### 3. Dashboard "Smart Vineyard"

#### [NEW] `grafana/provisioning/dashboards/dashboards.yml`
Configurar o carregamento automático do dashboard a partir de um ficheiro JSON.

#### [NEW] `grafana/dashboards/vineyard_main.json`
Criar um dashboard rico com:
- **Painéis de Séries Temporais**: Humidade do solo e ar, Temperatura do ar.
- **Painéis de Estado (Stat)**: Última leitura e status de conectividade.
- **Tabela de Alertas**: Lista de alertas ativos vindos do PostgreSQL.
- **Indicador de Risco**: Visualização clara do risco de fungos.

---

## Open Questions

> [!NOTE]
> Desejas que eu configure algum alerta direto no Grafana (ex: notificação via Discord/Slack) ou por agora focamo-nos apenas na visualização dos alertas que já vêm do **Alert Service**?
> Vou assumir apenas VISUALIZAÇÃO dos alertas existentes por agora.

## Verification Plan

### Automated Tests
1. **Container Check**: Verificar se o container `grafana` está `Running`.
2. **Connectivity**: Confirmar que o Grafana consegue ligar-se ao InfluxDB e Postgres através do menu "Data Sources" (verificação manual após build).

### Manual Verification
1. Aceder a `http://localhost:3000`.
2. Verificar se o dashboard "Smart Vineyard Main" aparece automaticamente.
3. Validar se os dados do simulador de sensores aparecem nos gráficos.
