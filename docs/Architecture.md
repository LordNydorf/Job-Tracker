# Architecture — Job Application Tracker

## High-Level Shape
Kotlin Multiplatform monorepo, three modules:

```
job-tracker/
├── shared/          # Kotlin Multiplatform — models, API client interface, business logic
├── androidApp/       # Jetpack Compose UI, consumes `shared`
└── backend/          # Ktor server, consumes `shared` for models/DTOs
```

Same language, same data classes, no duplicate DTOs between client and server. This is the whole point of the exercise — one Kotlin codebase, three layers.

## `shared` Module
- **Models**: `Application`, `Note`, `Status` (sealed class/enum), `Source` (enum).
- **API contract**: interface `JobTrackerApi` describing operations (`getApplications()`, `createApplication()`, `updateStatus()`, `addNote()`, `deleteApplication()`), implemented differently on client (Ktor HTTP client) vs backend (direct DB access) — but same interface shape keeps both sides honest.
- **Serialization**: `kotlinx.serialization` for all models — works identically on JVM (backend) and Android.
- No platform-specific code here in v1 (no `expect`/`actual` needed yet — that's more relevant once/if iOS is added).

## `androidApp` Module
- **UI**: Jetpack Compose, single-activity, Navigation Compose for screen routing.
- **State**: ViewModel per screen (`ApplicationListViewModel`, `ApplicationDetailViewModel`), exposing `StateFlow`.
- **Networking**: Ktor client (CIO or OkHttp engine) implementing `JobTrackerApi`, injected into ViewModels.
- **DI**: Koin (lightweight, Kotlin-idiomatic, less ceremony than Hilt for a solo project).
- **Local reminders**: WorkManager, scheduled based on `Application.lastUpdated` + reminder interval.
- **Persistence (client-side cache, optional v1.1)**: Room, for offline-first later — not required for v1 since backend is source of truth.

## `backend` Module
- **Framework**: Ktor server, Netty engine.
- **Routing**: REST, resource-oriented (`/applications`, `/applications/{id}`, `/applications/{id}/notes`).
- **Database**: Start with SQLite via Exposed (simple, file-based, zero infra). Swap to Postgres later if deploying seriously.
- **Serialization**: `kotlinx.serialization` content negotiation plugin.
- **No auth in v1** — single user, deployed with an obscure URL / basic API key header is enough. Do not over-engineer auth before there's a second user.

## Data Flow
```
Compose UI → ViewModel → Ktor Client (shared interface) → HTTP → Ktor Server → Exposed → SQLite
```

## Why This Stack
- **Koin over Hilt**: less annotation processing overhead, easier to reason about in a solo/learning project.
- **SQLite/Exposed over Postgres initially**: zero infra to stand up locally; migrate later if the deployed backend needs concurrent writes at scale (it won't, for a single user).
- **KMP shared module even without iOS yet**: forces clean separation between UI and business logic from day one, and leaves the door open for iOS/desktop later without a rewrite.

## Deployment
- Backend: containerized (Dockerfile using Ktor's fat-jar), deployed to Railway or Render free tier.
- Android: signed debug build for now; release signing config deferred until this becomes a "real" App Store-bound app (not v1 scope).
