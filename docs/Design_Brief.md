# Design Brief — Job Application Tracker

## Design Principles
1. **Fast to log, faster to scan.** Adding an application should take under 15 seconds. The list view is the cockpit — optimize for glanceable status and tactile feedback.
2. **Status is the hero.** Color and iconography make pipeline stages instantly readable without reading text.
3. **No dead ends.** Every screen has an obvious next action (add, edit, back, filter) — no screens that just display data with nothing to do.
4. **Speed Over Ceremony.** 1-tap pipeline transitions, quick note chips with toast feedback, and clean 3-step wizard ergonomics.

## Visual Language & Design Tokens
- **Brand Identity**:
  - **Light Mode**: Royal Cobalt Blue (`BrandPrimary = #2563EB`, container `#DBEAFE`, text `#1E3A8A`).
  - **Dark Mode**: Electric Sky Cobalt (`BrandDarkPrimary = #60A5FA`, container `#1E3A8A`, text `#DBEAFE`).
- **High-Contrast Status Colors** (Isolated constants in `Color.kt`):
  - **Applied**: Sky Blue (`#0284C7` / `#38BDF8`)
  - **Screening**: Amber Gold (`#D97706` / `#FBBF24`)
  - **Interview**: Electric Violet (`#4F46E5` / `#818CF8`)
  - **Offer**: Emerald Green (`#059669` / `#34D399`)
  - **Rejected**: Ruby Red (`#E11D48` / `#FB7185`)
  - **Ghosted**: Cool Slate with dashed border (`#475569` / `#94A3B8`)
- **Theme Modes**: First-class support for `System Default`, `Light Mode`, and `Dark Mode` with persistent state via `ThemeConfig`.
- **Typography**: Material 3 default type scale with clean letter spacing and bold hierarchical weights.

## Layout Patterns
- **List Screen**: Dashboard metric summary card (tap to filter), card-based list with company avatar gradient monogram, status badge, source tag, target salary pill, overdue follow-up nudge, and relative time.
- **Detail Screen**: Company header card with 1-tap job link launcher, animated status stepper, quick update suggestion chips, and reverse-chronological notes timeline with delete affordances.
- **Add/Edit Wizard**: 3-step animated wizard with sticky action bar, currency prefix selector, inline date picker, URI keyboard options, and back-navigation unsaved changes guard.

## Interaction & Tactile Feel
- Single-tap status changes on detail stepper and list cards with `LocalHapticFeedback` tactile pulses.
- Suggestion chips trigger rapid 1-tap note logging with instant toast feedback.
- Custom reminder dialog with numeric keyboard input.
- Contextual search empty state with a 1-tap "Clear Search" button.

## v2 Visual Roadmap
- **Light Mode Visual Revamp**: Dedicated design pass to refine surface elevations, tonal contrast, and border definition on pure light surfaces (`#F8FAFC`).
