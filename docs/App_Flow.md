# App Flow — Job Application Tracker

## Screens

### 1. Application List (Home)
- Default screen on launch.
- Shows all applications as cards: company, role, status badge, days-since-update.
- Sort control: by date added, by status, by last updated.
- Filter chips: All / Applied / Interviewing / Offer / Closed (rejected+ghosted).
- FAB (+) → Add Application screen.
- Tap a card → Application Detail screen.

### 2. Add / Edit Application
- Fields: Company (text), Role (text), Source (dropdown: Wellfound, Upwork, Contra, Braintrust, Toptal, Referral, Other), Date Applied (date picker), Job Link (text, optional), Initial Status (defaults to "Applied").
- Save → returns to List, new/updated card visible.
- Cancel → discard changes, return to previous screen.

### 3. Application Detail
- Shows all fields from Add/Edit, editable inline.
- Status stepper/dropdown to move through pipeline stages.
- Notes section: chronological list of free-text notes with timestamps, add-note input at bottom.
- "Delete application" action (confirm dialog).
- Follow-up reminder toggle + days-until-reminder field.

### 4. Settings (minimal, v1)
- Backend URL (for dev/testing against local vs deployed server).
- Notification permission status/toggle.

## Status Pipeline (state machine)
```
Applied → Screening → Interview → Offer
                    ↘ Rejected
                    ↘ Ghosted (auto-suggested after N days no response)
```
- Transitions are user-driven except the "Ghosted" suggestion, which is a passive nudge (notification), not an automatic state change.

## Navigation Structure
```
List (home)
 ├── Add Application (modal/sheet)
 ├── Detail (per application)
 │     └── Edit (inline, same screen)
 └── Settings (top bar icon)
```

## Data Flow (client ↔ backend)
1. App launches → fetch `/applications` from Ktor backend → populate list.
2. Add/Edit → `POST` or `PATCH /applications/{id}` → optimistic UI update, rollback on failure.
3. Delete → `DELETE /applications/{id}` → remove from local state on success.
4. Notes → nested under application, `POST /applications/{id}/notes`.
5. Local reminders are scheduled client-side (WorkManager), not backend-driven, in v1.
