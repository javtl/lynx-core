# 🐾 LYNX Core


![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![MongoDB](https://img.shields.io/badge/MongoDB-Latest-47A248)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED)
![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939)

## The Immutable Supply Chain Protocol

> **Hexagonal architecture meets append-only ledger.** A production-grade backend for industrial traceability that works with or without blockchain. Built from first principles—no shortcuts.

---

## 🎯 The Problem

**Supply chains are hemorrhaging data.**

When a pharmaceutical batch hits a contamination issue, companies scramble through Excel spreadsheets and legacy databases trying to trace origin. By then, the product is already in hospitals.

When a food distributor needs FDA compliance proof, they dig through years of scanned documents. When a manufacturer detects a defect, they can't pinpoint which supplier component caused it.

**The root cause:** Centralized, mutable databases. No audit trail. No trust.

---

## ✅ The Solution

**LYNX Core is a protocol for immutable traceability.**

Instead of "update the batch record," LYNX records **every change as an event**—permanently. The ledger grows; it never mutates. This creates an unbreakable audit trail.

```
Your API calls → Domain validates → Event recorded → Immutable ledger
```

**The architecture is radical for 2026:** The domain logic is **completely independent** from MongoDB, HTTP, or any framework. Swap databases? Only the adapter changes. Need blockchain later? Same domain, different persistence layer. This is hexagonal architecture, done right.

---

## 🧬 Why This Matters

### For Enterprise Compliance
- **ISO 22005** (traceability) requires immutable records. LYNX gives you that out-of-the-box.
- **FDA audits** need event logs. LYNX's ledger is the audit trail.
- **Recalls** are hours, not days. Every batch's full history is queryable in milliseconds.

### For Architecture
- **Domain-driven design** without the bloat. Zero Spring annotations in domain logic.
- **Framework-agnostic** by design. Swap MongoDB for PostgreSQL, HTTP for gRPC—only adapters change.
- **Testable** at domain level without any infrastructure. 29 tests, all green, no mocks.

---

## 🛠️ Tech Stack

| Layer | Choice | Why |
|---|---|---|
| **Language** | Java 17 | Records + pattern matching for immutable domain models |
| **Framework** | Spring Boot 3.3 | Production-grade, but only in infrastructure layer |
| **Database** | MongoDB 7.0 | Flexible schema, JSON Schema validation for ledger integrity |
| **Testing** | JUnit 5 + Testcontainers | Integration tests against real DB, not mocks |
| **API** | Swagger/OpenAPI 3.0 | Self-documenting, testable in browser |
| **Container** | Docker Compose | One command: `up`. Database + app + networking included. |

---

## 📊 Status: May 2026 MVP

### ✅ Completed (Sprint 1)
- [x] Hexagonal project structure (domain/application/infrastructure)
- [x] Batch immutable model (Java Record with state machine)
- [x] LedgerEvent append-only (enforce immutability at language level)
- [x] MongoDB adapter (ACID transactions, JSON Schema)
- [x] 29 integration tests (100% critical paths)
- [x] Swagger UI (all endpoints documented)

### 🔄 In Progress (Sprints 2-4)
- [ ] REST CRUD API (POST/GET/PUT batches)
- [ ] State machine enforcement (DRAFT → ACTIVE → DISPATCHED)
- [ ] JWT authentication + RBAC
- [ ] Claude AI ingestion (text → batch)

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Docker + Docker Compose

### One Command

```bash
git clone https://github.com/nominal-studio/lynx-core.git
cd lynx-core
docker-compose up
```

Then:
- **Swagger:** http://localhost:8080/swagger-ui.html
- **API:** http://localhost:8080
- **MongoDB:** mongodb://localhost:27017/lynx_core

---

## 📖 Architecture 

LYNX follows **Hexagonal Architecture** (Ports & Adapters). The domain is **pure Java**, zero framework dependencies.

```
domain/                    ← Pure business logic
├── model/                 (Batch, LedgerEvent, BatchStatus)
├── port/                  (Interfaces: what we need from outside)
└── exception/             (Domain errors, no Spring)

application/               ← Business workflows
├── usecase/               (CreateBatch, TransitionStatus, etc.)

infrastructure/            ← Implementation details
├── input/rest/            (HTTP controllers—swap for gRPC later)
├── output/persistence/    (MongoDB—swap for PostgreSQL later)
└── config/                (Spring beans)
```

---

## 💡 Core Concepts

### Batch (Immutable Record)

```java
Batch batch = Batch.createDraft("SALICORNIA-001", 25.5, "kg");
batch = batch.withStatus(BatchStatus.ACTIVE);  // Returns NEW instance
```

Every mutation returns a new `Batch`. The old one is unchanged. This is **immutability by design**, not convention.

### LedgerEvent (Append-Only)

```java
LedgerEvent event = LedgerEvent.ofStatusChange(
    batchId, 
    BatchStatus.DRAFT, 
    BatchStatus.ACTIVE,
    EventSource.USER, 
    userId
);
ledger.append(event);  // Write-only, never deleted
```

The ledger is the source of truth. Every query about a batch's history reads from this immutable log.

### State Machine (Enforced)

```
DRAFT → ACTIVE ⇄ HOLD → COMPLETED → DISPATCHED (terminal)
```

Invalid transitions throw `InvalidStateTransitionException` at the domain level, not the database. This is **correctness by constraint**, not by hope.

---

## 🧪 Testing Philosophy

**No mocks. Real database. Real tests.**

```bash
mvn test
# 29 tests, all integration
# Testcontainers spins up real MongoDB for each test
# Tests are green or they're red—no "happy path"
```

---

## 🔮 Roadmap (Why You Should Pay Attention)

### Phase 1: Core (May 2026) ✅
Immutable traceability engine. Domain-first architecture.

### Phase 2: Integration (Summer 2026)
AI ingestion (Claude), React dashboard, mobile app (Kotlin Multiplatform).

### Phase 3: Decentralization (2027)
Blockchain audit layer, token governance, DAO.

**The Vision:** LYNX becomes the open-source standard for industrial traceability. 
---

## 💻 Development

### Build

```bash
mvn clean install
```

### Run Locally

```bash
mvn spring-boot:run
```

### Tests

```bash
mvn test
```

### Docker Build

```bash
docker build -t lynx-core:latest .
docker-compose up
```

---
