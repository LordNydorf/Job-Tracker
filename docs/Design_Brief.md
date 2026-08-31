# Design Brief — Job Application Tracker

## Design Principles
1. **Fast to log, faster to scan.** Adding an application should take under 15 seconds. The list view is the app — optimize for glanceable status.
2. **Status is the hero.** Color and iconography should make pipeline stage instantly readable without reading text.
3. **No dead ends.** Every screen has an obvious next action (add, edit, back) — no screens that just display data with nothing to do.
4. **Boring is fine.** This is a personal utility, not a portfolio piece for visual design. Material 3 defaults are acceptable; don't burn time on custom theming in v1.

## Visual Language
- **System**: Material 3 (Material You), Jetpack Compose defaults — dynamic color if available, sensible static fallback otherwise.
- **Status colors**:
  - Applied — neutral gray/blue
  - Screening — amber
  - Interview — blue/purple (in-progress feel)
  - Offer — green
  - Rejected — muted red
  - Ghosted — dashed/outlined treatment (distinct from Rejected — it's ambiguous, not final)
- **Typography**: Material 3 default type scale (no custom font in v1 — revisit if this becomes a portfolio-facing app later).

## Layout Patterns
- **List screen**: card-based list, one application per card, status badge top-right, company+role as primary text, "3 days since update" as secondary text.
- **Detail screen**: vertical scroll, status stepper at top, editable fields below, notes as a reverse-chronological feed at the bottom.
- **Add/Edit**: bottom sheet or full-screen modal, form fields stacked, single primary "Save" action.

## Interaction Notes
- Status changes should be a single tap/dropdown, not buried in an edit form — this is the action Rohit will do most often.
- Swipe-to-archive or swipe-to-delete on list cards is a nice-to-have, not required for v1.
- Empty state on first launch: brief illustration/text + prominent "Add your first application" CTA.

## What This Is NOT
- Not a branding exercise. No custom logo/icon polish required for v1 — a placeholder launcher icon is fine.
- Not optimized for anyone but Rohit. Don't design for hypothetical other users yet.
