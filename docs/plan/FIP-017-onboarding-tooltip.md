# FIP — Onboarding Tooltip on First Detail Visit

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-017                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | ✅ Done                                                            |
| **PRD ref**            | MVP-FEATURE-SUGGESTIONS #10                                        |
| **Feature**            | Show tooltip on first Movie Detail visit highlighting Watchlist/Watched buttons |
| **Date**               | 2026-03-08                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

New users don't know they can add movies to Watchlist/Watched from the detail screen. The buttons exist but there's no guidance. Users who don't discover list management in the first session churn.

## 2. Goal

On the **first visit** to any Movie Detail screen, show a brief tooltip/coach mark highlighting the Watchlist and Watched buttons row: _"Tap to save movies to your lists"_. Dismisses on tap or after a timeout. Never shown again.

## 3. UX Spec

- **Trigger:** First time `MovieDetailScreen` reaches `Content` state with a movie loaded.
- **Appearance:** A small popup/tooltip anchored above the `WatchlistActionsRow`, with an arrow pointing down. Semi-transparent dark background with white text.
- **Content:** String resource: "Tap to save movies to your lists" / "Toca para guardar películas en tus listas"
- **Dismiss:** Tap anywhere on the tooltip, or auto-dismiss after 5 seconds.
- **Persistence:** Flag stored in DataStore (`has_seen_detail_tooltip = true`). Once dismissed, never shown again.
- **No overlay/scrim:** The rest of the screen stays interactive. The tooltip is non-blocking.

## 4. Architecture

```
DataStore (Preferences)
       │
OnboardingRepository (interface in :core:domain, impl in :core:data)
       │
HasSeenDetailTooltipUseCase (:feature:movie-detail:impl)
SetDetailTooltipSeenUseCase (:feature:movie-detail:impl)
       │
MovieDetailViewModel
       │
MovieDetailScreen (shows/hides TooltipBox)
```

**Key decisions:**

1. **OnboardingRepository in `:core:data`** — onboarding flags are cross-feature concerns. Other features can reuse it for future tooltips.
2. **DataStore (Preferences)** — lightweight key-value storage, already used in the project for recent searches. No Room needed.
3. **Tooltip composable** — use Material 3 `TooltipBox` / `RichTooltip` if available, or a custom `Popup` composable with arrow. Keep it simple — no external library.

## 5. Data Model

New DataStore preference key:

```
has_seen_detail_tooltip: Boolean (default: false)
```

## 6. Modules Affected

- `core:data` — new `OnboardingRepository` interface + DataStore impl
- `core:domain` — new `OnboardingRepository` interface (if following Clean Architecture domain boundary)
- `feature:movie-detail:impl` — ViewModel reads/writes the flag, Screen shows tooltip
- `core:string-resources` — new tooltip string (EN/ES)

## 7. Phases & Tasks

### Phase 1 — Onboarding Repository

- [x] Added `hasSeenDetailTooltip` + `setHasSeenDetailTooltip` to existing `UserPreferencesRepository` in `core:domain`
- [x] Implemented in `DefaultUserPreferencesRepository` in `core:data` using `SecureKeyValueStore`
- [x] No separate module needed — reuses existing Hilt binding

### Phase 2 — Use Cases

- [x] Created `ObserveHasSeenDetailTooltipUseCase` in `core:domain`
- [x] Created `SetDetailTooltipSeenUseCase` in `core:domain`

### Phase 3 — ViewModel Integration

- [x] Added `showTooltip: Boolean` to `MovieDetailUiState` (default `false`)
- [x] Injected both use cases into `MovieDetailViewModel`
- [x] On `Content` state reached: `checkTooltip()` checks `hasSeenDetailTooltip`. If false → set `showTooltip = true`
- [x] Added `DismissTooltip` intent → sets `showTooltip = false` + calls `setDetailTooltipSeen()`
- [x] Auto-dismiss via `LaunchedEffect` with 5s delay in the Screen

### Phase 4 — Tooltip UI

- [x] Added string resources EN/ES: `onboarding_detail_tooltip`
- [x] Created `OnboardingTooltip` composable — `inverseSurface` card with info icon, anchored above `WatchlistActionsRow`
- [x] Shows conditionally when `uiState.showTooltip == true`
- [x] Auto-dismiss after 5s via `LaunchedEffect`
- [x] Tap to dismiss via `clickable`

### Phase 5 — Tests

- [x] Unit: tooltip not seen + Content → `showTooltip = true`
- [x] Unit: tooltip already seen + Content → `showTooltip = false`
- [x] Unit: `DismissTooltip` → `showTooltip = false` + calls `setDetailTooltipSeen()`

---

## 8. Blockers

_None._

---

## 9. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-08 | Initial draft |

