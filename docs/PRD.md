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
1. As a user, I can add a new application (company, role, source, date applied, salary, link, custom reminder interval).
2. As a user, I can update an application's status in 1 tap (Applied → Screening → Interview → Offer / Rejected / Ghosted) with tactile haptic feedback.
3. As a user, I can see all applications in a list, filterable via interactive dashboard metrics, searchable with instant clear, and sortable by status, date, and company.
4. As a user, I can add and delete free-text notes/timeline items on an application with toast confirmations.
5. As a user, I can toggle between Light, Dark, and System Default appearance themes with settings persistence.
6. As a user, I get a local reminder notification if an application has had no status change in N days (with overdue visual indicators on cards).
7. As a user, my data persists in a serverless cloud database (Neon PostgreSQL) and loads instantly (0ms) from offline disk cache.

## 6. Success Criteria
- App installed on Rohit's own phone and used for real applications within a week of v1 ship.
- Backend deployed and reachable on Render + Neon PostgreSQL — usable as a live demo link.
- Codebase clean enough to link in job applications as a portfolio piece.

## 7. v2 Scope Roadmap
1. **Offline Write Queue**: Client-generated UUIDs, optimistic local writes, and WorkManager-backed background sync to survive cold starts and offline usage.
2. **Light Mode Visual Revamp**: Dedicated high-contrast color token pass for light appearance mode.

## 8. Out of Scope Questions to Revisit Later
- iOS app via Compose Multiplatform/SwiftUI (needs Mac & second UI layer).
- Multi-user auth & JWT (revisit only if a second user exists).
- Resume parsing / PDF extraction (distinct parsing subsystem).
- AI-powered job matching & scraping (multi-week scraper infrastructure effort).
