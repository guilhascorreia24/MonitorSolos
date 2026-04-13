# Producer Dashboard Frontend — Implementation Plan

## Objetivo

Desenvolver uma aplicação web moderna e intuitiva para o produtor de vinho. Esta aplicação será a "cara" do projeto, servindo como centro de controlo para monitorizar sensores, visualizar o dashboard do Grafana e gerir o inventário de dispositivos e alertas.

## User Review Required

> [!IMPORTANT]
> A aplicação será construída com **Next.js 14 (App Router)** e **Tailwind CSS**.
> Ela comunicará com o **API Gateway** na porta **8000**.
> Será necessário atualizar o `api-gateway` para suportar **CORS**, permitindo que o browser faça pedidos ao backend.

## Proposed Changes

### 1. Novo Projeto: `dashboard-webapp`

#### [NEW] Estrutura do Projeto
- **Framework**: Next.js 14.
- **Estilização**: Tailwind CSS + ShadcnUI (ou similar para componentes premium).
- **Iconografia**: Lucide React.
- **Gráficos**: Recharts.

#### [NEW] Componentes Principais
- **Layout**: Sidebar persistente com navegação (Dashboard, Dispositivos, Alertas).
- **Dashboard Home**: Cartões de resumo (Total de Sensores, Alertas Ativos, Estado da Vinha).
- **Device Management**: Lista dinâmica com estados (Online/Offline) e botões de ação (Editar/Remover).
- **Alert Center**: Feed de alertas com cores baseadas na severidade e botão "Resolver".
- **Grafana Integration**: Embed dos dashboards já existentes via iFrame (opcional, para visualização técnica).

---

### 2. Integração Backend (CORS)

#### [MODIFY] [application.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/api-gateway/src/main/resources/application.yml)
- Adicionar configuração global de CORS para permitir pedidos vindos de `http://localhost:3000`.

---

### 3. Infraestrutura Docker

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
- Adicionar o serviço `dashboard-webapp`.
- Expor na porta **3100** (para não conflitar com o Grafana na 3000).

## Open Questions

> [!NOTE]
> Desejas que a página inicial mostre logo os gráficos do Grafana via iFrame, ou preferes que eu construa gráficos nativos em React (usando Recharts) que consomem a API? 
> Sugiro gráficos nativos para uma experiência mais fluída e "premium".

## Verification Plan

### Automated Tests
1. **Linter**: `npm run lint` no diretório do frontend.
2. **Build**: `npm run build` para garantir que não há erros de tipagem ou compilação.

### Manual Verification
1. Abrir `http://localhost:3100`.
2. Verificar se a lista de dispositivos é carregada corretamente a partir do `api-gateway`.
3. Testar a funcionalidade de "Resolver" um alerta e verificar se desaparece da lista.
