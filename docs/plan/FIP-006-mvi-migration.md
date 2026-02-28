# FIP — MVI Architecture Migration

| Field           | Value                                                        |
|-----------------|--------------------------------------------------------------|
| **FIP ID**      | FIP-006                                                      |
| **Status**      | ✅ Done                                               |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-09                             |
| **Feature**     | Migrate all feature ViewModels to MVI (Intent → State → Effect) |
| **Date**        | 2026-02-26                                                   |
| **Author**      | @asensiodev                                                  |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or contradicts the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The current pattern is MVVM with direct function calls from UI to ViewModel:
```
UI → viewModel.updateQuery(query)
UI → viewModel.loadInitialData()
ViewModel → StateFlow<UiState>
```

Issues as the app grows:
- Side effects (navigation, toasts) are implicit or missing
- No single entry point — UI can call any ViewModel function in any order
- State transitions are scattered across multiple functions

MVI enforces:
```
UI → Intent → ViewModel.process(intent)
ViewModel → StateFlow<UiState>   (state)
ViewModel → Channel<UiEffect>    (one-time side effects)
```

---

## 6. Architecture

Every migrated feature follows this contract — no external library, pure Kotlin:

```kotlin
// One sealed interface per feature
sealed interface SearchMoviesIntent {
    data object LoadInitialData : SearchMoviesIntent
    data class UpdateQuery(val query: String) : SearchMoviesIntent
    // ...
}

// One-time effects
sealed interface SearchMoviesEffect {
    data class NavigateToDetail(val movieId: Int) : SearchMoviesEffect
    data object ShowErrorToast : SearchMoviesEffect
}

// ViewModel
class SearchMoviesViewModel : ViewModel() {
    val uiState: StateFlow<SearchMoviesUiState>
    val effect: Flow<SearchMoviesEffect>  // Channel(BUFFERED).receiveAsFlow()

    fun process(intent: SearchMoviesIntent)
}

// UI
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) { ... }
    }
}
```

### Migration Order

Feature-by-feature. Order by complexity (simplest first to validate pattern):

| Phase | Feature | ViewModel | Complexity |
|-------|---------|-----------|------------|
| 1 | `watchlist` | `WatchlistMoviesViewModel` | Low — 3 actions |
| 2 | `watched-movies` | `WatchedMoviesViewModel` | Low — 3 actions |
| 3 | `movie-detail` | `MovieDetailViewModel` | Medium — 4 actions + sync |
| 4 | `settings` | `SettingsViewModel` + `ProfileViewModel` | Medium — auth side effects |
| 5 | `search-movies` | `SearchMoviesViewModel` | High — most complex, pagination + cache |

### Files touched per feature (template)

For each feature:
- Add `<Feature>Intent.kt`
- Add `<Feature>Effect.kt` (only if there are side effects beyond state)
- Update `<Feature>ViewModel.kt` — add `process()`, emit via `Channel`
- Update `<Feature>Screen.kt` — replace direct calls with `process(Intent)`
- Update `<Feature>ViewModelTest.kt` — test via `process(intent)`

---

## 9. Phases & Tasks

### Phase 1 — Watchlist

- [x] Create `WatchlistIntent` sealed interface
- [x] Create `WatchlistEffect` sealed interface
- [x] Refactor `WatchlistMoviesViewModel` to expose `process(intent)` and `effect: Flow<WatchlistEffect>`
- [x] Update `WatchlistScreen` to call `viewModel.process(...)` and collect effects
- [x] Update `WatchlistMoviesViewModelTest`

### Phase 2 — Watched Movies

- [x] Create `WatchedMoviesIntent` sealed interface
- [x] Create `WatchedMoviesEffect` sealed interface
- [x] Refactor `WatchedMoviesViewModel`
- [x] Update `WatchedMoviesScreen`
- [x] Create `WatchedMoviesViewModelTest`

### Phase 3 — Movie Detail

- [x] Create `MovieDetailIntent` sealed interface:
  - `FetchDetails(movieId)`, `ToggleWatched`, `ToggleWatchlist`, `ShareMovie`
- [x] Create `MovieDetailEffect` sealed interface:
  - `ShareMovie(text)`, `NavigateBack`
- [x] Refactor `MovieDetailViewModel`
- [x] Update `MovieDetailScreen`
- [x] Update `MovieDetailViewModelTest`

### Phase 4 — Settings & Profile

- [x] Create `SettingsIntent`:
  - `ObserveAuth`, `OnAppearanceClicked`, `SetTheme(option)`, `DismissThemePicker`, `OnLogoutClicked`
- [x] Create `SettingsEffect`:
  - `NavigateBack` (post logout)
- [x] Refactor `SettingsViewModel`
- [x] Update `SettingsScreen`
- [x] Create `ProfileIntent`:
  - `LoadProfile`, `OnLinkGoogleClicked`, `OnSettingsClicked`, `DismissBottomSheet`, `OnAccountCollision`
- [x] Create `ProfileEffect`:
  - `NavigateToSettings`, `LaunchGoogleSignIn`
- [x] Refactor `ProfileViewModel`
- [x] Update `ProfileScreen`
- [x] Update `SettingsViewModelTest` + `ProfileViewModelTest`

### Phase 5 — Search Movies

- [x] Create `SearchMoviesIntent` sealed interface
- [x] Create `SearchMoviesEffect` sealed interface
- [x] Refactor `SearchMoviesViewModel`
- [x] Update `SearchMoviesScreen`
- [x] Update `SearchMoviesViewModelTest`
