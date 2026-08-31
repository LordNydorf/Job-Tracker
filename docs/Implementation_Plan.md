# Implementation Plan — Job Application Tracker

## Phase 0 — Scaffold (Day 1)
- [ ] Generate KMP project via kmp.jetbrains.com wizard (shared + androidApp + backend modules).
- [ ] Confirm project builds and runs (empty Android app, empty Ktor "hello world" route).
- [ ] Set up Git repo, initial commit.

## Phase 1 — Shared Models (Day 1–2)
- [ ] Define `Application`, `Note`, `Status`, `Source` in `shared`, with `kotlinx.serialization`.
- [ ] Define `JobTrackerApi` interface (method signatures only, no implementation yet).
- [ ] Unit test serialization round-trips.

## Phase 2 — Backend (Day 2–3)
- [ ] Set up Exposed + SQLite, define table schema matching `shared` models.
- [ ] Implement CRUD routes: `GET/POST /applications`, `GET/PATCH/DELETE /applications/{id}`, `POST /applications/{id}/notes`.
- [ ] Manual test via curl/Postman/Bruno before touching the Android app.
- [ ] Basic error handling: 404 for missing IDs, 400 for malformed bodies.

## Phase 3 — Android UI (Day 3–5)
- [ ] Set up Koin DI, Navigation Compose skeleton.
- [ ] Build List screen with hardcoded/mock data first (unblock UI work from backend readiness).
- [ ] Build Add/Edit screen.
- [ ] Build Detail screen with notes feed.
- [ ] Wire Ktor client implementation of `JobTrackerApi`, swap mock data for real network calls.

## Phase 4 — Integration (Day 5–6)
- [ ] End-to-end test: add application on device → confirm it persists in backend DB → kill and reopen app → confirm data loads from server, not just local state.
- [ ] Handle loading/error states in UI (empty list, network failure, retry).

## Phase 5 — Polish Feature (Day 6–7)
- [ ] WorkManager reminder: schedule local notification if `lastUpdated` exceeds threshold.
- [ ] Status pipeline visual polish (colors from Design Brief).

## Phase 6 — Deploy (Day 7)
- [ ] Dockerize backend, deploy to Railway/Render.
- [ ] Point Android app's base URL at deployed backend (config-driven, not hardcoded).
- [ ] Final smoke test against production backend.

## Definition of Done for v1
- Rohit can add, view, update, and delete real job applications from his phone.
- Backend is deployed and reachable outside localhost.
- Repo is clean enough to link from a portfolio/resume.

## Explicit Anti-Scope-Creep Note
Do not start iOS/KMP-UI work, auth, or multi-user support until v1 above is fully working end-to-end. Any new idea that comes up mid-build goes in a "later" list at the bottom of this file, not into the current sprint.

### Later / Parking Lot
-
