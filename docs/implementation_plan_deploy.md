# Implementation Plan: Securing Credentials with Environment Variables

This plan addresses the requirement to "privatize" credentials by moving hardcoded secrets into a central `.env` file and ensuring they are not committed to version control.

## User Review Required

> [!CAUTION]
> This change will move all hardcoded passwords and tokens to a `.env` file. You will need to keep this file secure and NEVER share it. The provided `.env.example` should be used as a template for other environments.

> [!IMPORTANT]
> To run the services locally (outside Docker) after this change, you will need to have the environment variables exported in your shell or use a tool that loads `.env` files.

## Proposed Changes

### Security & Environment Setup

#### [NEW] [.env](file:///home/guilherme/repos/consumeApp/MonitorSolos/.env)
- Create a central file to store secrets like `POSTGRES_PASSWORD`, `INFLUXDB_TOKEN`, etc.

#### [NEW] [.env.example](file:///home/guilherme/repos/consumeApp/MonitorSolos/.env.example)
- Create a template file with empty values to show which variables are required.

#### [NEW] [.gitignore](file:///home/guilherme/repos/consumeApp/MonitorSolos/.gitignore)
- Ensure `.env` is excluded from Git to prevent accidental leaks.

### Service Configurations

#### [MODIFY] Multiple `application.yml`
- Update all Spring Boot `application.yml` files (alert, analytics, device, ingestion, mqttbroker) to use environment variable placeholders with current values as defaults.
- Example: `password: ${DB_PASSWORD:password}`

#### [MODIFY] [docker-compose.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.yml)
- Update the compose file to reference the `.env` variables instead of hardcoding values in the `environment:` section.

#### [MODIFY] [docker-compose.prod.yml](file:///home/guilherme/repos/consumeApp/MonitorSolos/docker-compose.prod.yml)
- Align the production compose file with the same environment variable names.

## Verification Plan

### Manual Verification
- Verify that `docker compose up` still works and services can connect to databases.
- Check that `.env` is correctly ignored by Git (`git check-ignore .env`).
- Verify that the services can start locally with environment variables set.
