# FIP — Snackbar Feedback with Undo on State Changes

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-018                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | 🟡 Draft                                                          |
| **PRD ref**            | MVP-FEATURE-SUGGESTIONS #7                                         |
| **Feature**            | Snackbar confirmation with Undo on Watchlist/Watched toggle        |
| **Date**               | 2026-03-08                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

When the user marks a movie as Watched or adds to Watchlist from the detail screen, there's no confirmation beyond the icon animation. If the user wasn't looking at the button, they miss the feedback. Accidental toggles have no recovery path.

## 2. Goal

Show a brief `Snackbar` with an "Undo" action after toggling Watchlist/Watched states in Movie Detail:

- "Added to Watchlist" / "Removed from Watchlist"
- "Marked as Watched" / "Unmarked as Watched"

Undo reverts the action within the Snackbar duration (~5s).

## 3. UX Spec

- **Trigger:** After successful `ToggleWatchlist` or `ToggleWatched` in Movie Detail.
- **Snackbar content:** Action-specific message + "Undo" action button.
- **Undo:** Reverts the toggle (calls the same toggle intent again).
- **Duration:** `SnackbarDuration.Short` (~4s) — standard Material 3.
- **Position:** Bottom of screen, above any system navigation.
- **Only on MovieDetail** — not on Watchlist remove (already has dialog confirmation).

## 4. Architecture

```
MovieDetailViewModel
    │ emits ShowSnackbar(message, actionLabel) effect
    ▼
MovieDetailRoute
    │ collects effect → shows Snackbar via SnackbarHostState
    │ if result == ActionPerformed → process(UndoToggle) intent
    ▼
MovieDetailViewModel
    │ UndoToggle → re-toggles the last action
```

**Key decisions:**

1. **Effect-based** — Snackbar is a one-shot side effect, not UI state. Use `MovieDetailEffect.ShowSnackbar`.
2. **Undo via re-toggle** — simplest approach. The toggle is already idempotent.
3. **SnackbarHostState** — standard Compose M3 pattern. No `Scaffold` needed — can use a standalone `SnackbarHost`.
4. **String resources for messages** — no hardcoded strings.

## 5. Modules Affected

- `feature:movie-detail:impl` — ViewModel (emit effect), Screen (show Snackbar)
- `core:string-resources` — new snackbar message strings (EN/ES)

## 6. Phases & Tasks

### Phase 1 — String Resources

- [ ] Add strings (EN):
  - `snackbar_added_to_watchlist` = "Added to Watchlist"
  - `snackbar_removed_from_watchlist` = "Removed from Watchlist"
  - `snackbar_marked_as_watched` = "Marked as Watched"
  - `snackbar_unmarked_as_watched` = "Unmarked as Watched"
  - `snackbar_undo` = "Undo"
- [ ] Add strings (ES):
  - `snackbar_added_to_watchlist` = "Añadida a Watchlist"
  - `snackbar_removed_from_watchlist` = "Eliminada de Watchlist"
  - `snackbar_marked_as_watched` = "Marcada como vista"
  - `snackbar_unmarked_as_watched` = "Desmarcada como vista"
  - `snackbar_undo` = "Deshacer"

### Phase 2 — ViewModel Changes

- [ ] Add `ShowSnackbar(messageRes: Int)` to `MovieDetailEffect`
- [ ] After successful `ToggleWatchlist`: emit `ShowSnackbar` with the appropriate added/removed message
- [ ] After successful `ToggleWatched`: emit `ShowSnackbar` with the appropriate marked/unmarked message
- [ ] Add `UndoToggleWatchlist` and `UndoToggleWatched` intents — they just call the existing toggle methods

### Phase 3 — Screen Integration

- [ ] Add `SnackbarHostState` to `MovieDetailScreen`
- [ ] Add `SnackbarHost` positioned at bottom of the `Box`
- [ ] In `MovieDetailRoute`, handle `ShowSnackbar` effect:
  - Show snackbar with message + "Undo" action
  - If `SnackbarResult.ActionPerformed` → process the appropriate undo intent
- [ ] Ensure snackbar doesn't overlap with `CollapsibleTopAppBar`

### Phase 4 — Tests

- [ ] Unit: `MovieDetailViewModel` — `ToggleWatchlist` success → emits `ShowSnackbar` with added message
- [ ] Unit: `MovieDetailViewModel` — `ToggleWatchlist` on already-in-watchlist → emits `ShowSnackbar` with removed message
- [ ] Unit: `MovieDetailViewModel` — `ToggleWatched` success → emits `ShowSnackbar` with marked message
- [ ] Unit: `MovieDetailViewModel` — `UndoToggleWatchlist` → calls toggle again

---

## 7. Blockers

_None._

---

## 8. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-08 | Initial draft |

