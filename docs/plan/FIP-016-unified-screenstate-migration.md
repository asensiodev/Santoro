# FIP — Unified ScreenState Pattern Migration

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-016                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | ✅ Done                                                            |
| **PRD ref**            | Internal refactor — no PRD feature                                 |
| **Feature**            | Migrate all UiState models to sealed ScreenState pattern           |
| **Date**               | 2026-03-08                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The project currently has two patterns for representing screen visual state:

### Pattern A — Boolean flags (majority of features)
```kotlin
data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movie: MovieUi? = null,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
)
```
**Problems:**
- States are NOT mutually exclusive — `isLoading = true` and `errorMessage != null` can coexist, creating undefined UI behaviour.
- The Screen composable must use `when` with priority-based `if/else` chains instead of exhaustive `when`.
- Adding a new state (e.g., `Empty`) requires adding another boolean and updating every conditional.

### Pattern B — Sealed ScreenState (Browse + SeeAll)
```kotlin
sealed interface ScreenState {
    data object Loading : ScreenState
    data object Content : ScreenState
    data class Error(val message: String) : ScreenState
    data object Empty : ScreenState
}

data class UiState(
    val screenState: ScreenState = ScreenState.Loading,
    val movies: List<MovieUi> = emptyList(),
    // ... persistent data
)
```
**Advantages:**
- States are mutually exclusive at compile-time.
- Exhaustive `when` in the Screen — compiler enforces handling all states.
- Data that persists across state transitions lives in the data class wrapper.
- Adding a new state = add a sealed variant → compiler shows all places that need updating.

**Goal:** Migrate all features from Pattern A to Pattern B for consistency and safety.

---

## 2. Scope

| Feature module       | Current pattern | Needs migration |
|----------------------|-----------------|-----------------|
| `search-movies`     | B (sealed)      | ❌ Already done |
| `search-movies/seeall` | B (sealed)   | ❌ Already done |
| `movie-detail`      | A (booleans)    | ✅              |
| `watchlist`          | A (booleans)    | ✅              |
| `watched-movies`     | A (booleans)    | ✅              |
| `login`              | A (booleans)    | ⏭️ Skipped — overlay loading, not screen-level |
| `settings`           | A (booleans)    | ⏭️ Skipped — static config screen |
| `profile`            | A (booleans)    | ⏭️ Skipped — static config screen |

---

## 3. Migration Strategy

- One phase per feature module.
- Each phase: create `XxxScreenState` sealed interface → refactor `XxxUiState` to embed it → update ViewModel state transitions → update Screen `when` → update tests.
- No behaviour changes — purely structural refactor.
- Start with the simplest (movie-detail), end with the most complex (login).

---

## 4. Phases & Tasks

### Phase 1 — Movie Detail

- [x] Create `MovieDetailScreenState`: `Loading`, `Content`, `Error(message)`
- [x] Refactor `MovieDetailUiState`: replace `isLoading`/`errorMessage`/`hasResults` with `screenState: MovieDetailScreenState`
- [x] Update `MovieDetailViewModel` state transitions
- [x] Update `MovieDetailScreen` composable — exhaustive `when` on `screenState`
- [x] Update tests
- [x] Toggle errors (watchlist/watched) moved to `ShowError` effect (not a screen-level state change)

### Phase 2 — Watchlist

- [x] Create `WatchlistScreenState`: `Loading`, `Content`, `Error(message)`, `Empty`
- [x] Refactor `WatchlistMoviesUiState`: replace `isLoading`/`errorMessage`/`hasResults` with `screenState`
- [x] Keep `query`, `movies`, `movieToRemove` as persistent fields in the data class
- [x] Update ViewModel, Screen, and tests

### Phase 3 — Watched Movies

- [x] Create `WatchedScreenState`: `Loading`, `Content`, `Error(message)`, `Empty`
- [x] Refactor `WatchedMoviesUiState`: replace `isLoading`/`errorMessage` with `screenState`
- [x] Keep `movies`, `query`, `stats` as persistent fields
- [x] Update ViewModel, Screen, and tests

### Phase 4 — Login — SKIPPED

Login uses `isLoading` as an overlay (semi-transparent over buttons), not a screen-level state transition. Does not fit the `ScreenState` pattern.

### Phase 5 — Settings & Profile — SKIPPED

Static config screens. `isLoading` is used for individual async operations (delete account, link Google), not page-level Loading/Content/Error cycles.

---

## 5. Blockers

_None at planning time._

---

## 6. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-08 | Initial draft |
| 1.1     | 2026-03-08 | Completed — Phases 1-3 done, 4-5 skipped |

