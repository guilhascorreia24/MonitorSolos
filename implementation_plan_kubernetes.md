# Plan: Kubernetes Deployment Manifests creation

This plan outlines the creation of a complete Kubernetes deployment environment for the Smart Vineyard ecosystem. We will translate the existing Docker Compose configuration into standard Kubernetes manifests, following best practices for microservices and cloud-native applications.

## User Review Required

> [!IMPORTANT]
> **Cluster Environment**: Since no live cluster was detected on your machine, I will prepare all the files for you to run in environments like **Minikube, Kind, or a Cloud Provider (EKS/GKE)**.
> **Image Registry**: The manifests will use placeholders for the container registry (defaulting to the local build name). You will need to build and push these images to a registry if deploying to a remote cluster.
> **Kafka/Stateful Services**: For this initial manifest set, I will use simple single-instance deployments with persistent volume claims. For production, a more robust setup (like Helm charts for Kafka/Postgres) is usually recommended.

---

## Proposed Changes

### New Directory: `infrastructure/kubernetes/` [NEW]

We will organize the manifests into subdirectories for better maintainability.

#### [NEW] [namespace.yaml](file:///home/guilherme/repos/consumeApp/MonitorSolos/infrastructure/kubernetes/namespace.yaml)
- Define a dedicated `vineyard` namespace.

#### [NEW] [configmaps.yaml](file:///home/guilherme/repos/consumeApp/MonitorSolos/infrastructure/kubernetes/configmap.yaml)
- Centralize all non-sensitive configuration:
  - Kafka bootstrap servers (`kafka:29092`)
  - DB URLs and Port mappings
  - InfluxDB Org/Bucket names

#### [NEW] [secrets.yaml](file:///home/guilherme/repos/consumeApp/MonitorSolos/infrastructure/kubernetes/secrets.yaml)
- Secure sensitive data (Base64 encoded):
  - `POSTGRES_PASSWORD`
  - `INFLUXDB_ADMIN_TOKEN`
  - `GRAFANA_PASSWORD`

#### [NEW] [persistence.yaml](file:///home/guilherme/repos/consumeApp/MonitorSolos/infrastructure/kubernetes/persistence.yaml)
- Define `PersistentVolumeClaims` for:
  - PostgreSQL Data
  - InfluxDB Data
  - Mosquitto Data/Log

### Infrastructure Layer [NEW]
Files under `infrastructure/kubernetes/base/`:

#### [NEW] `postgres.yaml`
- Deployment and ClusterIP Service for PostgreSQL.
- Includes `init.sql` bootstrap via ConfigMap if possible, or volume mount.

#### [NEW] `kafka-zookeeper.yaml`
- Simplified single-node Kafka and Zookeeper deployment.

#### [NEW] `influxdb.yaml` & `mosquitto.yaml`
- Individual deployments and services.

#### [NEW] `grafana.yaml`
- Deployment and Service (NodePort or LoadBalancer for access).

### Microservices Layer [NEW]
Files under `infrastructure/kubernetes/services/`:

#### [NEW] `microservices-deployments.yaml` (or separate files)
- Individual `Deployment` and `Service` for:
  - `api-gateway` (Entry point)
  - `ingestion-service`
  - `analytics-service`
  - `alert-service`
  - `device-service`
  - `mqttbroker-spring`
  - `dashboard-webapp` (Next.js frontend)

---

## Open Questions

> [!IMPORTANT]
> **Access Method**: How do you intend to access the Dashboard and API? 
> 1. **Port-Forwarding** (Simplest for local test)
> 2. **NodePort** (Access via IP:Port)
> 3. **Ingress** (Requires an Ingress Controller like NGINX)
> I will default to **ClusterIP + guidance for Port-Forwarding** in the walkthrough, as it's the most portable.

---

## Verification Plan

### Automated Verification
- Run `kubectl apply --dry-run=client -f ./infrastructure/kubernetes/` (if I can install kubectl) to verify syntax.

### Manual Verification
1. Once you have a cluster (like Minikube):
   - Run `kubectl apply -f ./infrastructure/kubernetes/`
   - Verify pod status with `kubectl get pods -n vineyard`
   - Test connectivity between services via internal DNS.
