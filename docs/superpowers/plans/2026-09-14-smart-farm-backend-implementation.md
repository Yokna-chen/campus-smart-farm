# Smart Farm Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the modular Java 8 Spring Boot backend described in the smart-farm requirements.

**Architecture:** Domain services expose stable interfaces; REST controllers translate HTTP requests to domain commands; JPA repositories persist users, devices, metrics, irrigation records, audit records, and sync status. Vendor integrations are adapters with demo implementations.

**Tech Stack:** Java 8, Spring Boot 2.7, Spring Web, Spring Data JPA, Spring Security Crypto, H2/MySQL, JUnit 5, Maven.

**Spec:** `docs/superpowers/specs/2026-09-14-smart-farm-backend-design.md`

## Global Constraints

- Java 8 source and target compatibility.
- Spring Boot 2.7.x.
- Third-party credentials never returned by APIs or written to ordinary logs.
- External integrations remain read-only except Hydrawise irrigation commands.
- H2 is the default local database; MySQL is configured through environment variables.

### Task 1: Project Skeleton and Domain Persistence

**Files:** `backend/pom.xml`, application/configuration, JPA entities and repositories, schema seed configuration.

- [ ] Write context smoke tests and entity repository tests.
- [ ] Run tests to verify the empty project fails.
- [ ] Implement the Spring Boot application, entities, repositories, seed users/devices and database configuration.
- [ ] Run tests and package the project.

### Task 2: Authentication and Authorization

**Files:** auth service/token store/controller/filter/DTOs and tests.

- [ ] Write failing tests for login success, invalid password, missing token and role enforcement.
- [ ] Run tests and verify expected failures.
- [ ] Implement BCrypt authentication, bearer token filter and role checks.
- [ ] Run auth tests and the full suite.

### Task 3: Integration Ports and Synchronization

**Files:** integration ports, demo FusionSolar/Hydrawise adapters, sync service, scheduler, sync status/log model and tests.

- [ ] Write failing tests for mapping snapshots, freshness and bounded retry.
- [ ] Implement adapters and synchronization services.
- [ ] Verify integration tests and persistence behavior.

### Task 4: Irrigation Operations and Audit

**Files:** irrigation service/controller/DTOs, operation policy, audit service and tests.

- [ ] Write failing tests for duration, schedule window, conflict, authorization and audit behavior.
- [ ] Implement command flow and status confirmation.
- [ ] Run irrigation tests and full suite.

### Task 5: Overview, Device Queries and Error Contract

**Files:** overview/device controllers, DTOs, exception handler and tests.

- [ ] Write failing MVC tests for required endpoints and stale-data markers.
- [ ] Implement REST endpoints and consistent error responses.
- [ ] Run full Maven verification and package output.
