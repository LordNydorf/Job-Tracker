# TRD — Job Application Tracker

## 1. Tech Stack
| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin (1.9+/2.0) | Shared across all layers |
| Shared logic | Kotlin Multiplatform | `shared` module, no expect/actual needed in v1 |
| Serialization | kotlinx.serialization | JSON, shared between client/server |
| Android UI | Jetpack Compose | Material 3 |
| Android state | ViewModel + StateFlow | No LiveData |
| Android DI | Koin | Lightweight vs Hilt |
| Android networking | Ktor Client (OkHttp engine) | Implements shared `JobTrackerApi` |
| Android background work | WorkManager | Local follow-up reminders |
| Backend framework | Ktor Server (Netty) | REST |
| Backend DB | SQLite via Exposed | Swap to Postgres only if needed later |
| Deployment | Docker → Railway/Render | Free tier acceptable for v1 |

## 2. Data Model

```kotlin
enum class Status { APPLIED, SCREENING, INTERVIEW, OFFER, REJECTED, GHOSTED }
enum class Source { WELLFOUND, UPWORK, CONTRA, BRAINTRUST, TOPTAL, REFERRAL, OTHER }

data class Application(
    val id: String,
    val company: String,
    val role: String,
    val source: Source,
    val dateApplied: LocalDate,
    val jobLink: String?,
    val status: Status,
    val lastUpdated: Instant,
    val reminderDays: Int?
)

data class Note(
    val id: String,
    val applicationId: String,
    val text: String,
    val createdAt: Instant
)
```

## 3. API Contract

| Method | Route | Purpose |
|---|---|---|
| GET | `/applications` | List all applications |
| POST | `/applications` | Create new application |
| GET | `/applications/{id}` | Get single application |
| PATCH | `/applications/{id}` | Update fields (incl. status) |
| DELETE | `/applications/{id}` | Delete application |
| POST | `/applications/{id}/notes` | Add note |
| GET | `/applications/{id}/notes` | List notes for an application |

All requests/responses: `application/json`, using shared `Application`/`Note` models.
Auth: `X-API-Key` header, single static key for v1 (see `security_consideration.md`).

## 4. Non-Functional Requirements
- **Performance**: trivial at single-user scale — no specific latency targets, just "feels instant" on a list of <200 applications.
- **Availability**: best-effort. Free-tier hosting may cold-start/sleep; acceptable for v1, not a production SLA.
- **Offline behavior**: v1 requires network to load/save. Local caching (Room) is a v1.1 stretch goal, not a hard requirement.
- **Minimum Android version**: API 26+ (covers virtually all real devices, keeps Compose/Koin compatibility simple).

## 5. Build & Environments
- **Local dev**: Android app points to `http://10.0.2.2:PORT` (emulator) or LAN IP (physical device) for the locally running Ktor server.
- **Production**: Android app points to deployed Railway/Render URL, set via build config field, not hardcoded in source.

## 6. Testing Strategy
- `shared`: unit tests for serialization, any pure business logic.
- `backend`: route-level tests using Ktor's test application against an in-memory/test DB.
- `androidApp`: ViewModel unit tests with fake `JobTrackerApi`; UI tests optional for v1 (not worth the setup time for a solo utility app).
