# App Flow — Job Application Tracker

## Screens

### 1. Application List (Home)
- Default screen on launch, loading instantly (0ms) from `LocalApplicationStore` cache.
- **Pipeline Dashboard Card**: Glanceable metric counters (`Applied`, `Screening`, `Interview`, `Offer`) with 1-tap interactive filtering and haptic feedback.
- **Application Cards**: Company monogram gradient, role, salary pill, source tag, status badge, overdue follow-up indicator, and relative update timestamp.
- **Controls**: Search bar (with instant "Clear Search" empty state), sort menu (Date Added, Last Updated, Company A-Z), and scrollable filter chips.
- **Actions**: FAB (+) → Add Application Wizard; Tap a card → Application Detail screen; Cloud icon → Server & Theme Settings dialog.

### 2. Add / Edit Application (3-Step Animated Wizard)
- **Step 1 (Role & Company)**: Company Name (`ImeAction.Next`, word capitalization), Role (`ImeAction.Next`), Target Compensation (currency prefix selector: $, ₹, €, £, AED + salary input).
- **Step 2 (Source & Stage)**: `FlowRow` chips for Source (`Wellfound`, `Upwork`, `Contra`, `Braintrust`, `Toptal`, `Referral`, `Other`) and Initial Status.
- **Step 3 (Timeline & Nudges)**: Date Applied (`MaterialDatePicker` modal), Job Posting Link (`KeyboardType.Uri`), and Follow-up Reminders (`FlowRow` chips: None, 3 Days, 7 Days, 14 Days, Custom numeric dialog).
- **Navigation Guard**: Back gesture / arrow button steps backward through the wizard (Step 3 → Step 2 → Step 1). Exiting Step 1 with unsaved edits prompts a **"Discard changes?"** confirmation dialog.

### 3. Application Detail (Cockpit)
- **Header Card**: Large company monogram, role, salary pill, application date, and 1-tap "Open Job Posting" browser button.
- **Pipeline Stepper**: Horizontal scrolling interactive stepper (`Applied` → `Screening` → `Interview` → `Offer` / `Rejected` / `Ghosted`) with smooth color animations and haptic feedback.
- **Quick Update Chips**: 1-tap suggestion pills (*"Passed screening"*, *"Scheduled round 1"*, *"Sent follow-up"*) with instant toast feedback.
- **Activity & Interview Timeline**: Reverse-chronological feed of free-text notes with timestamps.
- **Note Management**: Delete icon affordance on each note item with an `AlertDialog` confirmation step.
- **Application Actions**: Top bar Edit icon (opens wizard) and Delete icon (with deletion confirmation).

### 4. Settings & Server Config (Dialog)
- **Appearance Selector**: Toggle between `System Default`, `Light Mode`, and `Dark Mode` with persistent `SharedPreferences` storage via `ThemeConfig`.
- **Backend Connection**: Active Server URL and API Key inputs with 1-tap quick presets (Render Production, Android Emulator, USB Reverse Tether, Wi-Fi LAN).

---

## Status Pipeline (State Machine)
```
Applied → Screening → Interview → Offer
                    ↘ Rejected
                    ↘ Ghosted (auto-suggested after N days of inactivity)
```

---

## Navigation Structure
```
ApplicationListScreen (Home)
 ├── AddEditApplicationScreen (3-Step Wizard Modal)
 ├── ApplicationDetailScreen
 │     ├── AddEditApplicationScreen (Edit Mode)
 │     └── Note Delete Confirmation Dialog
 └── ServerConfigDialog (Theme & Backend Settings)
```

---

## Data Flow (Client ↔ Backend)
1. **App Launch**: Loads local cache immediately from `LocalApplicationStore` (0ms) → fetches `/applications` from Ktor backend in background → updates local cache.
2. **Status Progression / Quick Note**: Instant optimistic UI update with tactile haptic feedback → dispatched to backend via `PATCH /applications/{id}` or `POST /applications/{id}/notes`.
3. **Note Deletion**: `DELETE /applications/{id}/notes/{noteId}` deletes from backend and local cache.
4. **Follow-up Reminders**: Local `WorkManager` worker runs periodic scans and emits Android system notifications when `daysSinceLastUpdate >= reminderDays`.
5. **v2 Offline Sync Engine**: Client-generated UUIDs + WorkManager `SyncWorker` draining queued mutations (`CREATE_APP`, `UPDATE_APP`, `DELETE_APP`, `ADD_NOTE`, `DELETE_NOTE`) when network is restored.
