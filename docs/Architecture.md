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
- **Models**: `Application`, `Note`, `Status` (enum), `Source` (enum).
- **API contract**: interface `JobTrackerApi` describing operations (`getApplications()`, `createApplication()`, `updateStatus()`, `deleteApplication()`, `getNotes()`, `addNote()`, `deleteNote()`), implemented differently on client (`KtorJobTrackerApi`) vs backend (`JobTrackerRepositoryImpl`) — but identical interface keeps both sides contractually aligned.
- **Serialization**: `kotlinx.serialization` for all models — works identically on JVM (backend) and Android.
- No platform-specific code here in v1 (no `expect`/`actual` needed yet — that's more relevant once/if iOS is added).

## `androidApp` Module
- **UI**: Jetpack Compose, single-activity (`MainActivity`), Navigation Compose for screen routing.
- **Theming & Color**: Material 3 with dedicated Deep Cobalt Tech Blue palette (`#2563EB` light / `#60A5FA` dark), persistent `ThemeConfig` (System, Light, Dark), and independent status color tokens.
- **State**: ViewModel per screen (`ApplicationListViewModel`, `ApplicationDetailViewModel`, `AddEditViewModel`), exposing `StateFlow`.
- **Networking**: Ktor client (OkHttp engine) implementing `JobTrackerApi`, injected via Koin into ViewModels.
- **DI**: Koin (lightweight, Kotlin-idiomatic, less ceremony than Hilt for a solo project).
- **Local Reminders**: WorkManager, scheduled based on `Application.lastUpdated` + reminder interval (`daysSinceLastUpdate > reminderDays`).
- **Persistence & Offline Cache**: `LocalApplicationStore` provides 0ms offline startup cache from disk JSON files. 
- **v2 Offline Engine**: WorkManager `SyncWorker` + local pending mutation queue (`CREATE_APP`, `UPDATE_APP`, `DELETE_APP`, `ADD_NOTE`, `DELETE_NOTE`) for optimistic offline writes and automatic background synchronization.

## `backend` Module
- **Framework**: Ktor server, Netty engine.
- **Routing**: REST, resource-oriented (`/applications`, `/applications/{id}`, `/applications/{id}/notes`, `/applications/{id}/notes/{noteId}`).
- **Database**: Dual-Engine via Exposed SQL ORM + HikariCP connection pooling:
  - *Production Cloud*: Serverless PostgreSQL on Neon (`DATABASE_URL`).
  - *Local Dev / Tests*: In-memory or local file SQLite (`data/jobtracker.db`).
- **Serialization**: `kotlinx.serialization` content negotiation plugin.
- **Auth**: Static API key header (`X-API-Key`) verified via custom Ktor authentication plugin.

## Data Flow
```
Compose UI → ViewModel → LocalApplicationStore (0ms cache & v2 offline queue)
                       ↳ Ktor Client (shared interface) → HTTP (TLS) → Ktor Server → Exposed + HikariCP → Neon PostgreSQL / SQLite
```

## Why This Stack
- **Koin over Hilt**: less annotation processing overhead, easier to reason about in a solo/learning project.
- **Neon PostgreSQL + SQLite fallback**: zero infra local development with reliable, persistent cloud storage in production that survives container restarts.
- **KMP shared module even without iOS yet**: forces clean separation between UI and business logic from day one, and leaves the door open for iOS/desktop later without a rewrite.

## Deployment
- Backend: containerized (multi-stage JRE 21 Alpine `Dockerfile`), deployed to Render or Railway with Neon serverless PostgreSQL.
- Android: signed release APK (`:androidApp:assembleRelease`) installable directly on physical devices or sideloaded.
