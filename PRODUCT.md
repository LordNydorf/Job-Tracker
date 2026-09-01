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
- **Mobile Android App**: Built with Kotlin Multiplatform, Jetpack Compose, Material 3, and Koin. Features 0ms offline startup cache via `LocalApplicationStore`, tactile haptic feedback, and configurable appearance themes (System, Light, Dark).
- **Backend API**: Ktor 3.0 server running on Render with Dual-Engine persistence: serverless Neon PostgreSQL in cloud production with local SQLite dev/test fallback.

## Capabilities and Constraints
- **Fast Application Entry**: Record company, role, target salary/rate, platform source, date applied, job link, and custom reminder threshold in under 15 seconds.
- **Single-Tap Pipeline Progression**: Instant status transitions across Applied ➔ Screening ➔ Interview ➔ Offer / Rejected / Ghosted with haptic feedback.
- **Interview Notes & Activity Feed**: Reverse-chronological timeline of notes, questions, and action items with 1-tap suggestions, toast feedback, and note deletion.
- **Interactive Dashboard Summary**: Metric cards dynamically filter the home application feed by stage on 1 tap.
- **Follow-up Reminders**: WorkManager background checks alerting users with notifications and card visual badges when applications exceed reminder days.
- **Appearance & Theming**: Persistent Light, Dark, and System Default theme modes.
- **v2 Offline Sync Engine (Upcoming)**: Client-side UUIDs, optimistic local writes, and WorkManager background synchronization queue.

## Brand Commitments
- **Tone**: Focused, modern, high-craft, professional, tactile.
- **Brand Palette**: Deep Cobalt Tech Blue (`#2563EB` light / `#60A5FA` dark).
- **Status Colors**: Sky applied, Amber screening, Violet interview, Emerald offer, Ruby rejected, Slate ghosted.

## Product Principles
1. **Speed Over Ceremony**: Logging an application or dropping an interview note must take seconds, not minutes.
2. **Glanceable Pipeline**: The user should instantly understand their search health (active interviews, pending follow-ups) from the home screen.
3. **Tactile Delight**: Every status transition, note submission, and filter switch should feel responsive and fluid with haptic feedback.
4. **Android Native Craft**: Strict adherence to Material Design 3 guidelines, 48dp touch targets, predictable navigation, and system-level dark/light mode parity.
