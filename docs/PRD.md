# PRD — Job Application Tracker

## 1. Problem
Rohit is actively job hunting across multiple channels (Wellfound, freelance platforms) and has no structured way to track applications, statuses, and follow-ups. Spreadsheets get abandoned. Needs a lightweight tool he actually uses daily — and it doubles as a portfolio piece proving full-stack Kotlin ability (KMP + Compose + Ktor), not just Flutter.

## 2. Goals
- Track every job/freelance application in one place: company, role, status, dates, notes.
- Get reminded to follow up so nothing goes stale.
- Ship something demoable: Android app talking to a real deployed backend.
- Prove Kotlin competency end-to-end (client + server + shared logic) as a portfolio artifact.

## 3. Non-Goals (v1)
- No iOS app in v1 (KMP makes it *possible* later, not required now).
- No team/multi-user support — single user (Rohit) only.
- No browser extension / auto-import from job boards.
- No resume/cover-letter generation features.

## 4. Target User
Just Rohit, for now. Design decisions optimize for his actual workflow, not generic personas.

## 5. Core User Stories
1. As a user, I can add a new application (company, role, source, date applied, link).
2. As a user, I can update an application's status (Applied → Screening → Interview → Offer / Rejected / Ghosted).
3. As a user, I can see all applications in a list, filterable/sortable by status and date.
4. As a user, I can add free-text notes to an application (e.g. interviewer name, questions asked).
5. As a user, I get a local reminder if an application has had no status change in N days.
6. As a user, my data persists on a backend so it's not lost if I reinstall the app.

## 6. Success Criteria
- App installed on Rohit's own phone and used for real applications within a week of v1 ship.
- Backend deployed and reachable (not just localhost) — usable as a live demo link.
- Codebase clean enough to link in job applications as a portfolio piece.

## 7. Out of Scope Questions to Revisit Later
- Multi-device sync conflict handling (v1 is single-device-at-a-time, last-write-wins is fine).
- Auth (v1 can hardcode a single user or skip auth entirely, given it's single-user).
