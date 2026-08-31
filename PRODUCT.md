# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
Rohit and active tech job seekers / freelance contractors applying across remote tech boards (Wellfound, Upwork, Contra, Toptal, Braintrust, Referrals). They need a swift, friction-free way to log applications on mobile, update pipeline status in 1 tap, write interview notes, and get nudged on stale applications without feeling overwhelmed.

## Product Purpose
Job Tracker eliminates the friction of managing tech job applications. It transforms messy spreadsheets into a high-speed (<15s application logging), tactile mobile cockpit with interactive pipeline tracking, interview history logs, and automatic follow-up reminders.

## Positioning
Mobile-first speed and developer ergonomics. Unlike bloated CRM tools or generic note apps, Job Tracker is laser-focused on the active tech job hunt: single-tap pipeline progression, dedicated interview note feeds, and intelligent stale-application reminder notifications.

## Operating Context
- **Mobile Android App**: Built with Kotlin Multiplatform, Jetpack Compose, Material 3, and Koin. Used while on the go or right after submitting an application or finishing an interview.
- **Backend API**: Ktor server + SQLite database running with local SQLite persistence and cloud-ready REST endpoints.

## Capabilities and Constraints
- **Fast Application Entry**: Record company, role, platform source, date applied, job link, and reminder threshold in under 15 seconds.
- **Single-Tap Pipeline Progression**: Instant status transitions across Applied -> Screening -> Interview -> Offer / Rejected / Ghosted.
- **Interview Notes Feed**: Reverse-chronological timeline of notes, interviewer questions, and follow-up points.
- **Follow-up Reminders**: WorkManager background checks alerting users when applications stay in the same state without updates beyond configured days.
- **Material 3 Design System**: Dynamic theming, custom status badges, dark mode first-class support, and edge-to-edge layout.

## Brand Commitments
- **Tone**: Focused, modern, high-craft, professional, tactile.
- **Palette**: Slate applied, Amber screening, Indigo interview, Emerald offer, Ruby rejected, Cool-gray ghosted.

## Product Principles
1. **Speed Over Ceremony**: Logging an application or dropping an interview note must take seconds, not minutes.
2. **Glanceable Pipeline**: The user should instantly understand their search health (active interviews, pending follow-ups) from the home screen.
3. **Tactile Delight**: Every status transition, note submission, and filter switch should feel responsive and fluid.
4. **Android Native Craft**: Strict adherence to Material Design 3 guidelines, 48dp touch targets, predictable navigation, and system-level dark mode.
