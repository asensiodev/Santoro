# FIP — Pull-to-Refresh (Browse & Search)

| Field                  | Value                                                         |
|------------------------|---------------------------------------------------------------|
| **FIP ID**             | FIP-011                                                       |
| **Version**            | 1.0                                                           |
| **Status**             | ✅ Done                                                       |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-08                              |
| **Feature**            | Pull-to-refresh on SearchMoviesScreen (browse + search mode)  |
| **Date**               | 2026-02-28                                                    |
| **Author**             | @asensiodev                                                   |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.

---

## 1. Context & Motivation

The cache TTL (30 min curated / 5 min search) means fresh data appears automatically eventually, but the user has no way to force a refresh on demand. Pull-to-refresh gives explicit control.

`BrowseCacheLocalDataSource.clearSection()` already exists. `CachingSearchMoviesRepository.clearStaleEntries()` is already used on initial load. The missing piece is wiring a new intent through ViewModel → UI.

---

## 2. Goals

- Swipe down on browse or search results → invalidate cache for all current sections → re-fetch from TMDB.
- Show M3 `PullToRefreshBox` indicator while refreshing.
- Hide offline banner after a successful refresh.

## 3. Non-Goals

- Pull-to-refresh on Watchlist or Watched screens (local DB, no TTL concept).
- Per-section refresh (always refreshes all dashboard sections).

---

## 6. Architecture

```
UI swipe down
    ↓ SearchMoviesIntent.Refresh
ViewModel.refresh()
    ↓ cachingRepository.clearAllSections()   ← new method
    ↓ fetchDashboardData() / performSearch()
UI reflects isRefreshing = true → false
```

---

## 8. Modules Affected

- `feature/search-movies/impl`
  - `SearchMoviesIntent` — add `Refresh`
  - `SearchMoviesUiState` — add `isRefreshing: Boolean`
  - `SearchMoviesViewModel` — handle `Refresh`, add `clearAllSections()`
  - `CachingSearchMoviesRepository` — add `clearAllSections()`
  - `SearchMoviesScreen` — wrap content in `PullToRefreshBox`

---

## 9. Phases & Tasks

### Phase 1 — ViewModel & Data

- [x] Add `Refresh` to `SearchMoviesIntent`
- [x] Add `isRefreshing: Boolean = false` to `SearchMoviesUiState`
- [x] Add `clearAllSections()` to `CachingSearchMoviesRepository`
- [x] Handle `Refresh` in `SearchMoviesViewModel`

### Phase 2 — UI

- [x] Wrap `DashboardContent` and `SearchMoviesContent` in `PullToRefreshBox`
- [x] Pass `isRefreshing` and `onRefresh` through

### Phase 3 — String Resources

- [x] No new strings needed (indicator is visual only)

---

## 16. Changelog

| Version | Date       | Summary        |
|---------|------------|----------------|
| 1.0     | 2026-02-28 | Initial draft + implementation |
