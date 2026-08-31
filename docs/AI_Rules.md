# AI Rules — Job Application Tracker

Rules for AI coding agents (Claude Code or similar) working in this repo. Keep this file living — add rules based on actual mistakes the agent makes, not speculative ones.

## Project Context
- Kotlin Multiplatform monorepo: `shared/` (models + API contract), `androidApp/` (Jetpack Compose), `backend/` (Ktor).
- Solo developer project, learning Kotlin — prefer explicit, readable code over clever abstractions.
- Reference docs in this repo: `PRD.md`, `Architecture.md`, `App_Flow.md` — read these before making structural decisions.

## General Rules
- Do not introduce new dependencies without flagging them first — this is a learning project, minimize surface area.
- Prefer Kotlin idioms (data classes, sealed classes, extension functions) over Java-style verbosity.
- Match existing code style in a file before imposing a different pattern.
- When a model/DTO exists in `shared`, use it — never redefine an equivalent class in `androidApp` or `backend`.

## Android (`androidApp`)
- Compose-only UI — no XML layouts.
- State hoisting: composables should be stateless where possible; state lives in ViewModels.
- Use `StateFlow`, not `LiveData`.
- DI via Koin — don't introduce Hilt/Dagger.

## Backend (`backend`)
- Ktor + Exposed + SQLite for v1 — do not swap to Postgres or add auth unless explicitly asked.
- Keep routes RESTful and resource-oriented; match the endpoints listed in `Architecture.md`.
- Validate request bodies; return proper HTTP status codes (400/404/500), don't silently swallow errors.

## Testing
- Shared module: unit test serialization and any business logic in `JobTrackerApi` implementations.
- Backend: test routes against an in-memory/test SQLite DB, not the dev DB.

## What NOT to do
- Don't add features beyond what's in `Implementation_Plan.md`'s current phase without being asked.
- Don't refactor working code "for style" without being asked — flag suggestions instead of unilaterally changing things.
- Don't add authentication, multi-user support, or iOS targets — explicitly out of scope for v1 per `PRD.md`.

## Build/Mistake Log
(Add entries here as real issues come up during development — this section should grow from experience, not upfront guessing.)

-
