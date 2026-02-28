# FIP — Watched Screen: Richer Stats Dashboard

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-012                                                            |
| **Version**            | 1.1                                                                |
| **Status**             | ✅ Done                                                            |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-20                                   |
| **Feature**            | Watched Screen: Richer Stats Dashboard                             |
| **Date**               | 2026-02-28                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The current `WatchedMoviesScreen` shows a single summary `Card` with just the total count of watched movies. This is functional but provides no meaningful insight into the user's viewing habits.

Stats dashboards are proven engagement drivers in tracking apps (Spotify Wrapped, Letterboxd, Trakt). Expanding the summary section into a rich stats dashboard — total movies, total runtime, favourite genre, and longest streak — adds delight and personalisation without requiring any new data sources. All data is derivable from the existing Room `movies` table via the domain layer.

---

## 2. Goals

- Replace `GamificationHeader` (single stat card) with a `WatchedStatsDashboard` composable showing four stat cards in a horizontal scroll row.
- Compute stats from the existing `getWatchedMovies()` flow — no new DAO queries, no schema migrations.
- Introduce a `WatchedStats` domain model and `GetWatchedStatsUseCase`.
- Wire the stats into `WatchedMoviesViewModel` alongside the existing `LoadMovies` flow.
- Full unit test coverage for the use case and updated ViewModel.

---

## 3. Non-Goals

- Sorting/filtering by rating — deferred (F-19 discarded).
- Backend/cloud sync of stats — stats are local-only.
- Charts, graphs, or animated "year in review" views — future feature.
- Adding `runtime` to `MovieEntity` / Room schema — stats fall back to `0 h` when runtime is unavailable for a movie (see §12).

---

## 4. User Stories

| ID    | As a…       | I want to…                                           | So that…                                          | Acceptance Criteria |
|-------|-------------|------------------------------------------------------|---------------------------------------------------|---------------------|
| US-01 | Watched user | See how many hours of movies I've watched            | I can appreciate the time I've invested           | "X h" shown in stats card, "— h" when all runtimes are null |
| US-02 | Watched user | Know my favourite genre                              | I can discover my taste patterns                  | Most frequent genre name shown; "—" if no genres available |
| US-03 | Watched user | See my longest weekly watching streak                | I can feel proud of consistent movie habits       | Consecutive ISO weeks with ≥1 movie shown as "X weeks" |
| US-04 | Watched user | See all four stats at a glance without scrolling far | I want a quick summary at the top of the screen   | Horizontal scroll row of 4 `ElevatedCard`s above the movie grid |

---

## 5. UX / Flows

### Screen: WatchedMoviesScreen

| State   | What the user sees                                                                 | User actions available         |
|---------|------------------------------------------------------------------------------------|--------------------------------|
| Loading | Existing spinner (unchanged)                                                       | —                              |
| Content | `WatchedStatsDashboard` above the grouped grid — 4 `ElevatedCard`s in a `LazyRow` | Horizontal scroll between cards |
| Empty   | Existing empty state (unchanged) — no dashboard shown                              | —                              |
| Error   | Existing error + retry (unchanged)                                                 | Retry → reloads                |

#### Stats cards layout (in order)

| # | Label                        | Value format        | Icon                   | Fallback  |
|---|------------------------------|---------------------|------------------------|-----------|
| 1 | `watched_stat_total_label`   | `%d movies`         | `Icons.Rounded.Movie`  | `0 movies`|
| 2 | `watched_stat_runtime_label` | `%d h`              | `Icons.Rounded.Schedule`| `— h`    |
| 3 | `watched_stat_genre_label`   | genre name string   | `Icons.Rounded.Favorite`| `—`      |
| 4 | `watched_stat_streak_label`  | `%d weeks`          | `Icons.Rounded.LocalFire`| `0 weeks`|

Each card: `ElevatedCard`, fixed `width = size120`, internal `Column` with icon + label + value.

---

## 6. Architecture

```
WatchedMoviesScreen
       │
WatchedMoviesViewModel
       │ collects (parallel to LoadMovies)
GetWatchedStatsUseCase
       │ maps
DatabaseRepository.getWatchedMovies() → List<Movie>
       │ in-memory computation (no new DAO query)
WatchedStats (domain model)
```

---

## 7. Data Model

### New: `WatchedStats` (domain model)

Location: `feature/watched-movies/impl/.../domain/model/WatchedStats.kt`

```
data class WatchedStats(
    val totalWatched: Int,
    val totalRuntimeHours: Int,
    val favouriteGenre: String?,
    val longestStreakWeeks: Int,
)
```

### Modified: `WatchedMoviesUiState`

Add `stats: WatchedStats? = null`.

### No Room schema changes.

---

## 8. Modules Affected

- `feature/watched-movies/impl` — new use case, domain model, ViewModel update, new composable, string keys
- `core/string-resources` — 8 new string keys (EN + ES)

---

## 9. Phases & Tasks

### Phase 1 — Domain

- [x] Create `WatchedStats` data class in `feature/watched-movies/impl/.../domain/model/`
- [x] Create `GetWatchedStatsUseCase` in `feature/watched-movies/impl/.../domain/usecase/`
  - Consumes `DatabaseRepository.getWatchedMovies()` via `flowOn(dispatchers.io)`
  - Maps `List<Movie>` to `WatchedStats`:
    - `totalWatched` = list size
    - `totalRuntimeHours` = `movies.sumOf { it.runtime ?: 0 } / 60`
    - `favouriteGenre` = most frequent genre name across all `movie.genres` (null if empty)
    - `longestStreakWeeks` = longest run of consecutive ISO weeks (Mon–Sun) that each contain ≥1 `watchedAt` timestamp

### Phase 2 — DI

- [x] Register `GetWatchedStatsUseCase` in `WatchedMoviesModule` (Hilt `@Provides` or `@Binds` as appropriate — match existing pattern)

### Phase 3 — Presentation?

- [x] Add `stats: WatchedStats? = null` to `WatchedMoviesUiState`
- [x] Add `LoadStats` intent to `WatchedMoviesIntent`
- [x] Inject `GetWatchedStatsUseCase` into `WatchedMoviesViewModel`
- [x] In `process(LoadMovies)`, launch a parallel coroutine collecting `getWatchedStatsUseCase()` and updating `uiState.stats`
- [x] Remove `totalWatchedMovies` derived property from `WatchedMoviesUiState` (now covered by `stats.totalWatched`)

### Phase 4 — UI

- [x] Create `WatchedStatsDashboard` composable in `feature/watched-movies/impl/.../presentation/component/`
  - Parameter: `stats: WatchedStats, modifier: Modifier = Modifier`
  - Layout: `LazyRow` with `horizontalArrangement = spacedBy(Spacings.spacing8)`
  - Four `StatCard` sub-composables (private), each: `ElevatedCard(width = Size.size120)` → `Column(padding = Spacings.spacing12)` → Icon + label `Text` + value `Text`
  - All strings via `stringResource`
- [x] Replace `GamificationHeader` call in `WatchedMoviesScreen.kt` / `WatchedMovieList` with `WatchedStatsDashboard`
  - Only render dashboard when `stats != null`
- [x] Update `WatchedMovieList` signature: replace `totalCount: Int` with `stats: WatchedStats?`
- [x] Update `GamificationHeader` → delete (or rename if still needed for a preview)

### Phase 5 — String resources

- [x] Add to `core/string-resources/src/main/res/values/strings.xml` (EN):
  ```xml
  <string name="watched_stat_total_label">Movies Watched</string>
  <string name="watched_stat_total_value">%d movies</string>
  <string name="watched_stat_runtime_label">Total Runtime</string>
  <string name="watched_stat_runtime_value">%d h</string>
  <string name="watched_stat_runtime_unavailable">— h</string>
  <string name="watched_stat_genre_label">Top Genre</string>
  <string name="watched_stat_genre_unavailable">—</string>
  <string name="watched_stat_streak_label">Best Streak</string>
  <string name="watched_stat_streak_value">%d weeks</string>
  ```
- [x] Add equivalents to `core/string-resources/src/main/res/values-es/strings.xml` (ES):
  ```xml
  <string name="watched_stat_total_label">Películas vistas</string>
  <string name="watched_stat_total_value">%d películas</string>
  <string name="watched_stat_runtime_label">Tiempo total</string>
  <string name="watched_stat_runtime_value">%d h</string>
  <string name="watched_stat_runtime_unavailable">— h</string>
  <string name="watched_stat_genre_label">Género favorito</string>
  <string name="watched_stat_genre_unavailable">—</string>
  <string name="watched_stat_streak_label">Mejor racha</string>
  <string name="watched_stat_streak_value">%d semanas</string>
  ```

### Phase 6 — Tests

- [x] `GetWatchedStatsUseCaseTest` — test all stat computations:
  - GIVEN empty list → all stats are 0 / null
  - GIVEN movies with runtimes → `totalRuntimeHours` is correct sum / 60
  - GIVEN movies with null runtimes → `totalRuntimeHours = 0`
  - GIVEN movies with genres → `favouriteGenre` is most frequent
  - GIVEN genre tie → returns one of the tied genres (stable)
  - GIVEN no genres → `favouriteGenre = null`
  - GIVEN consecutive ISO weeks → `longestStreakWeeks` is correct
  - GIVEN non-consecutive weeks → streak resets correctly
- [x] `WatchedMoviesViewModelTest` — extend:
  - GIVEN `LoadMovies` intent → `uiState.stats` is populated with correct `WatchedStats`
  - GIVEN stats use case error → `uiState.stats` remains null, error not surfaced separately

---

## 10. Validation

| What                       | Result | Notes |
|----------------------------|--------|-------|
| Manual test on real device | ⏭️     |       |
| Manual test on emulator    | ⏭️     |       |
| Edge cases verified        | ⏭️     |       |
| Accessibility check        | ⏭️     |       |

---

## 11. Blockers

_None at planning time._

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | `runtime` is on the `Movie` domain model but not persisted in `MovieEntity` — only populated when the user visits Movie Detail. Should we show `— h` for movies without runtime, or add `runtime` to the entity? | **Resolved:** Added `runtime` to `MovieEntity` (MIGRATION_4_5). Runtime is now persisted when a movie is saved/updated. Total hours are accurate. |
| 2 | Should the streak use calendar weeks (Mon–Sun ISO) or rolling 7-day windows? | ISO calendar weeks for simplicity and predictability. |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Compute stats in-memory from domain `List<Movie>` — no new DAO query | Add SQL aggregate query to DAO | `runtime` is absent from `MovieEntity`; computing at domain layer avoids schema migration and keeps the use case fully unit-testable without Room |
| 2 | `GetWatchedStatsUseCase` wraps `DatabaseRepository.getWatchedMovies()` independently | Reuse the same flow from `GetWatchedMoviesUseCase` | Cleaner separation; each use case has a single responsibility; both are collected in parallel in the ViewModel |
| 3 | Replace `GamificationHeader` entirely | Keep as fallback | No UX value in keeping the old single-stat card once the dashboard is in place |
| 4 | 2×2 fixed grid (`Column` of two `Row`s with `weight(1f)`) instead of `LazyRow` | `LazyRow` horizontal scroll | All 4 cards always visible without scroll; mirrors Letterboxd/Trakt summary pattern; no affordance problem |
| 5 | Add `runtime` to `MovieEntity` (MIGRATION_4_5) | Keep fallback `— h` | Runtime is persisted on every detail visit; total hours become accurate progressively as the user browses movies |

---

## 14. Out of Scope / Follow-ups

- Adding `runtime` to `MovieEntity` (enables accurate runtime totals without visiting Detail first)
- "Year in review" animated summary screen
- Sorting/filtering watched list by stat values

---

## 16. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-28 | Initial draft |
| 1.1     | 2026-02-28 | LazyRow → 2×2 fixed grid · `runtime` added to `MovieEntity` (MIGRATION_4_5) · hours stat now accurate |
