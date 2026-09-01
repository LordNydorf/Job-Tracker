# Implementation Plan — Job Application Tracker

## Phase 0 — Scaffold (Day 1)
- [x] Generate KMP project via kmp.jetbrains.com wizard (shared + androidApp + backend modules).
- [x] Confirm project builds and runs (empty Android app, empty Ktor "hello world" route).
- [x] Set up Git repo, initial commit.

## Phase 1 — Shared Models (Day 1–2)
- [x] Define `Application`, `Note`, `Status`, `Source` in `shared`, with `kotlinx.serialization`.
- [x] Define `JobTrackerApi` interface (method signatures only, no implementation yet).
- [x] Unit test serialization round-trips.

## Phase 2 — Backend (Day 2–3)
- [x] Set up Exposed + SQLite, define table schema matching `shared` models.
- [x] Implement CRUD routes: `GET/POST /applications`, `GET/PATCH/DELETE /applications/{id}`, `POST /applications/{id}/notes`.
- [x] Manual test via curl/Postman/Bruno before touching the Android app.
- [x] Basic error handling: 404 for missing IDs, 400 for malformed bodies.

## Phase 3 — Android UI (Day 3–5)
- [x] Set up Koin DI, Navigation Compose skeleton.
- [x] Build List screen with hardcoded/mock data first (unblock UI work from backend readiness).
- [x] Build Add/Edit screen (Multi-step animated wizard form).
- [x] Build Detail screen with notes feed.
- [x] Wire Ktor client implementation of `JobTrackerApi`, swap mock data for real network calls.

## Phase 4 — Integration (Day 5–6)
- [x] End-to-end test: add application on device → confirm it persists in backend DB → kill and reopen app → confirm data loads from server, not just local state.
- [x] Test error states: simulate server unreachable (kill Ktor server) → verify user-facing error message with retry button.

## Phase 5 — Polish & Feature (Day 6–7)
- [x] Implement WorkManager reminder notification worker for stale applications (`daysSinceLastUpdate > reminderDays`).
- [x] Apply visual styling rules from [Design_Brief.md](file:///c:/Users/notth/Projects/New%20folder/docs/Design_Brief.md) — status colors, dark mode, card typography, touch feedback.
- [x] Clean up code, remove all TODOs, ensure zero compiler warnings.
- [x] Custom Adaptive App Icon (Foreground, Background, Monochrome Material You layers).
- [x] Android 12+ Splash Screen integration (`androidx.core:core-splashscreen`).

## Phase 6 — Deploy & Release Readiness (Day 7)
- [x] Dockerize backend with multi-stage JRE 21 Alpine container (`Dockerfile` & `.dockerignore`).
- [x] Cloud deployment manifests for Railway (`railway.json`) and Render (`render.yaml`).
- [x] Point Android app's base URL at dynamic server config (Wi-Fi, USB, Emulator, Cloud production).
- [x] Release APK compilation verification (`:androidApp:assembleRelease`).
- [x] Comprehensive showcase `README.md` with architecture diagram, API reference, and setup guide.

## Definition of Done for v1
- [x] Rohit can add, view, update, and delete real job applications from his phone.
- [x] Backend is containerized, cloud-ready, and reachable outside localhost.
- [x] Repo is clean enough to link from a portfolio/resume.

## v2 Scope
- Offline write queue: client-generated UUIDs, optimistic local writes via LocalApplicationStore, WorkManager-backed background sync to survive app backgrounding/kill, pending/synced status indicator in the UI. Fixes Render free-tier cold-start blocking writes.
- Light mode visual revamp: the existing light theme variant needs its own design pass (colors/contrast), separate from the dark-mode-first design work already done.

### Later / Parking Lot
- iOS app via Compose Multiplatform/SwiftUI — needs a Mac, real second UI layer, project-sized on its own
- Multi-user auth — no second user exists yet; revisit only if that changes
- Resume parsing (auto-fill fields from pasted job descriptions/resume) — real value, but a distinct parsing subsystem, own project
- AI-powered daily job matching/scraping based on resume — depends on resume parsing being done first; scraping job sites is genuinely hard (many block it); treat as multi-week effort, not a feature

