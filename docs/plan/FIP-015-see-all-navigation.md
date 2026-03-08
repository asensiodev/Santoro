# FIP — "See All" Navigation for Browse Sections

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-015                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | ✅ Done                                                            |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §9 F-18                                 |
| **Feature**            | "See All" Navigation for Browse Sections                           |
| **Date**               | 2026-03-08                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The Browse screen (SearchMoviesScreen) shows curated sections — Trending, Popular, Top Rated, Upcoming — as horizontal `LazyRow` carousels with ~5 visible movies each. Users who want to explore more of a specific section have no way to do so — they hit a dead-end and must resort to text search. Every major media app (Netflix, Letterboxd, Apple TV+, Prime Video) provides a "See All →" affordance on section headers that navigates to a full paginated grid for that category.

**Key insight:** The entire data + pagination infrastructure already exists. All TMDB endpoints accept `page: Int`. The search results grid (`MovieList` in `SearchMoviesScreen`) is a fully working paginated grid with infinite scroll. We only need a new screen to host it, a thin ViewModel, and the "See All" button wired into navigation.

---

## 2. Goals

- Add a "See All →" text button to each curated section header (Trending, Popular, Top Rated, Upcoming) in the Browse screen.
- Tapping "See All" navigates to a new full-screen paginated grid showing all movies for that section.
- The grid supports infinite scroll with the same pagination pattern used in search results.
- Back navigation returns to the Browse screen.
- Now Playing is excluded — it uses the hero carousel and has no "See All" equivalent (small, rotating list).

---

## 3. Non-Goals

- Genre sections "See All" — genre filtering already works via the chip selector + search. Could be a follow-up.
- Changing the existing Browse section layout or carousel design.
- Caching for the "See All" grid — first page may hit cache from Browse, but subsequent pages are network-only. Acceptable for MVP.
- Pull-to-refresh on the "See All" screen — not needed for MVP (the list starts fresh on each navigation).

---

## 4. User Stories

| ID    | As a…  | I want to…                                             | So that…                                                   | Acceptance Criteria |
|-------|--------|--------------------------------------------------------|------------------------------------------------------------|---------------------|
| US-01 | User   | Tap "See All" on the Trending section                  | I can browse a full paginated grid of trending movies      | Navigates to a grid showing trending movies, loads more on scroll |
| US-02 | User   | Tap "See All" on Popular, Top Rated, or Upcoming       | I can explore more movies in that category                 | Same grid behaviour, section title shown in top bar |
| US-03 | User   | Tap a movie card in the "See All" grid                 | I see the movie detail                                     | Navigates to MovieDetailRoute |
| US-04 | User   | Tap the back button on the "See All" screen            | I return to the Browse screen                              | Back navigation pops the "See All" screen |

---

## 5. UX / Flows

### Flow A — See All navigation

```
Browse screen
  → Section header: "Trending Now" + "See All →"
    → User taps "See All →"
      → Navigate to SeeAllMoviesRoute(sectionType = TRENDING)
        → SeeAllMoviesScreen shows:
            - Top bar with section title + back arrow
            - 2-column grid of movies (poster cards)
            - Infinite scroll pagination (same as search results)
        → User taps movie card
          → Navigate to MovieDetailRoute(movieId)
        → User taps back
          → Pop back to Browse screen
```

### Section mapping

| Browse section   | SectionType enum   | Title string resource                              | API use case              |
|------------------|--------------------|-----------------------------------------------------|---------------------------|
| Trending Now     | `TRENDING`         | `search_movies_trending_title`                      | `GetTrendingMoviesUseCase`   |
| Popular movies   | `POPULAR`          | `search_movies_popular_movies_title`                | `GetPopularMoviesUseCase`    |
| Top Rated        | `TOP_RATED`        | `search_movies_top_rated_title`                     | `GetTopRatedMoviesUseCase`   |
| Upcoming         | `UPCOMING`         | `search_movies_upcoming_title`                      | `GetUpcomingMoviesUseCase`   |

### Edge cases

| Case                           | Behaviour                                                |
|--------------------------------|----------------------------------------------------------|
| API error on first page        | Full-screen ErrorContent with Retry button               |
| API error on page N > 1        | Existing movies remain, pagination stops (endReached)    |
| Empty first page response      | Full-screen NoResultsContent                             |
| Process death / config change  | Page resets to 1 (acceptable — no SavedStateHandle for page) |

---

## 6. Architecture

```
SearchMoviesScreen (Browse)
       │ "See All →" tapped
       ▼
NavController.navigate(SeeAllMoviesRoute(sectionType))
       │
       ▼
SeeAllMoviesScreen (new)
       │ LaunchedEffect → ViewModel.process(LoadInitial)
       ▼
SeeAllMoviesViewModel (new)
       │ calls existing Use Case (GetTrendingMoviesUseCase, etc.)
       ▼
SearchMoviesRepository (existing — unchanged)
       │
       ▼
SearchMoviesApiService (existing — unchanged)
```

**Key architectural decisions:**

1. **New screen lives inside `feature:search-movies:impl`** — no new module needed. The "See All" screen is a detail of the browse/search feature, not a standalone feature. It reuses the same repository, use cases, API service, and UI models. Creating a separate module would add unnecessary module coupling for no encapsulation benefit.

2. **New ViewModel (`SeeAllMoviesViewModel`)** — thin ViewModel with a single responsibility: paginate one section type. Receives the section type as `SavedStateHandle` arg, resolves which use case to call, and manages pagination state.

3. **Reuse existing use cases** — `GetTrendingMoviesUseCase`, `GetPopularMoviesUseCase`, etc. are already internal to the search-movies module. No changes needed.

4. **Route with type-safe args** — `SeeAllMoviesRoute(sectionType: String)` using Kotlin Serialization, same pattern as `MovieDetailRoute`.

---

## 7. Data Model

No new data models. No schema changes. Reuses existing `MovieUi` and domain `Movie`.

**New enum:**

```kotlin
enum class SectionType {
    TRENDING, POPULAR, TOP_RATED, UPCOMING
}
```

---

## 8. Modules Affected

- `feature:search-movies:api` — new `SeeAllMoviesRoute` route class
- `feature:search-movies:impl` — new screen, ViewModel, navigation wiring, section header update
- `core:string-resources` — new "See All" string
- `app` — no changes (navigation is within the tab nav graph, not the main nav graph)

---

## 9. Phases & Tasks

### Phase 1 — Route & Navigation

- [x] Add `SeeAllMoviesRoute` to `feature/search-movies/api/.../navigation/`:
  ```kotlin
  @Serializable
  data class SeeAllMoviesRoute(val sectionType: String)
  ```
- [x] Add `navigateToSeeAllMovies` extension function on `NavController` in `feature/search-movies/impl/.../navigation/`
- [x] Register `seeAllMoviesRoute` composable in `feature/search-movies/impl/.../navigation/` with `onMovieClick` and `onBackClick` lambdas
- [x] Wire in `SantoroTabNavGraph` — add `seeAllMoviesRoute` destination. `onMovieClick` navigates to `MovieDetailRoute` via `mainNavController`. `onBackClick` pops the `tabNavController`.
- [x] Verify: `tabNavController` is used for navigation (within the tab graph), not `mainNavController`

### Phase 2 — SectionType enum & title resolution

- [x] Create `SectionType` enum in `feature/search-movies/impl/.../presentation/model/`:
  ```kotlin
  enum class SectionType { TRENDING, POPULAR, TOP_RATED, UPCOMING }
  ```
- [x] Add `@StringRes fun titleRes(): Int` extension or property that maps each type to the existing string resource

### Phase 3 — ViewModel

- [x] Create `SeeAllMoviesViewModel` in `feature/search-movies/impl/.../presentation/seeall/`:
  - Inject `SavedStateHandle`, all four section use cases (GetTrending, GetPopular, GetTopRated, GetUpcoming)
  - Parse `sectionType` from `SavedStateHandle` via `toRoute<SeeAllMoviesRoute>()`
  - Expose `uiState: StateFlow<SeeAllMoviesUiState>` and `effect: Flow<SeeAllMoviesEffect>`
  - `process(intent: SeeAllMoviesIntent)` with intents: `LoadInitial`, `LoadMore`, `MovieClicked`
  - Internal: resolve the correct use case from `SectionType`, manage page counter, accumulate results, detect end-reached
- [x] Create `SeeAllMoviesUiState`:
  ```kotlin
  data class SeeAllMoviesUiState(
      val sectionType: SectionType,
      val titleRes: Int,
      val movies: List<MovieUi>,
      val screenState: SeeAllScreenState,
      val isLoadingMore: Boolean,
      val isEndReached: Boolean,
  )
  ```
  With `SeeAllScreenState`: `Loading`, `Content`, `Error(message)`, `Empty`
- [x] Create `SeeAllMoviesIntent`: `LoadInitial`, `LoadMore`, `MovieClicked(movieId)`, `Retry`
- [x] Create `SeeAllMoviesEffect`: `NavigateToDetail(movieId)`

### Phase 4 — Screen UI

- [x] Create `SeeAllMoviesRoute` composable (stateful — connects ViewModel) in `feature/search-movies/impl/.../presentation/seeall/`
- [x] Create `SeeAllMoviesScreen` composable (stateless) with:
  - `TopAppBar` with section title + back arrow
  - Content: `Loading` → `LoadingIndicator`, `Error` → `ErrorContent` with retry, `Empty` → `NoResultsContent`, `Content` → paginated movie grid
  - Reuse the grid pattern from `MovieList` (2-column `LazyVerticalGrid` with `GridCells.Adaptive`, infinite scroll via `ObserveGridState` equivalent)
- [x] Add `@PreviewLightDark` with mock data

### Phase 5 — "See All" button in Browse section headers

- [x] Add string resource `browse_see_all` = "See All" (EN) / "Ver todo" (ES) in `core/string-resources`
- [x] Modify `MovieSection` composable to accept an optional `onSeeAllClick: (() -> Unit)?` parameter
- [x] When `onSeeAllClick` is non-null, render the section header as a `Row` with title + "See All →" clickable text (right-aligned)
- [x] Add the `SectionType` concept to `DashboardContent` — pass `onSeeAllClick` lambda for each section (Trending, Popular, Top Rated, Upcoming)
- [x] Add `SeeAllClicked(sectionType: SectionType)` intent to `SearchMoviesIntent`
- [x] Add `NavigateToSeeAll(sectionType: SectionType)` effect to `SearchMoviesEffect`
- [x] Handle in `SearchMoviesViewModel`: emit the effect
- [x] Handle in `SearchMoviesRoute`: collect the effect and call `navigateToSeeAllMovies`

### Phase 6 — Tests

- [x] Unit: `SeeAllMoviesViewModel` — `LoadInitial` with TRENDING → calls `GetTrendingMoviesUseCase` page 1
- [x] Unit: `SeeAllMoviesViewModel` — `LoadInitial` success → state = Content with movies
- [x] Unit: `SeeAllMoviesViewModel` — `LoadInitial` error → state = Error
- [x] Unit: `SeeAllMoviesViewModel` — `LoadInitial` empty → state = Empty
- [x] Unit: `SeeAllMoviesViewModel` — `LoadMore` → appends movies, increments page
- [x] Unit: `SeeAllMoviesViewModel` — `LoadMore` empty result → endReached = true
- [x] Unit: `SeeAllMoviesViewModel` — `MovieClicked` → emits `NavigateToDetail` effect
- [x] Unit: `SeeAllMoviesViewModel` — `Retry` → reloads from page 1
- [x] Unit: `SeeAllMoviesViewModel` — each `SectionType` resolves to the correct use case
- [x] Unit: `SearchMoviesViewModel` — `SeeAllClicked(POPULAR)` → emits `NavigateToSeeAll(POPULAR)` effect

---

## 10. Validation

| What                       | Result | Notes |
|----------------------------|--------|-------|
| Trending See All works     | ⏭️     |       |
| Popular See All works      | ⏭️     |       |
| Top Rated See All works    | ⏭️     |       |
| Upcoming See All works     | ⏭️     |       |
| Infinite scroll loads pages| ⏭️     |       |
| Movie tap → detail         | ⏭️     |       |
| Back → returns to browse   | ⏭️     |       |
| Error state + retry        | ⏭️     |       |
| Empty state                | ⏭️     |       |

---

## 11. Blockers

_None at planning time._

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should "Now Playing" have a "See All"? | **No.** Now Playing is the hero carousel — it's a small curated list, not a discovery section. Keeping it without "See All" reinforces its special visual treatment. |
| 2 | Should the "See All" screen use the cache from Browse? | **Partially.** Page 1 may be served from the existing caching repository if still fresh. Pages 2+ are direct network calls. This is the existing behaviour of the use cases — no changes needed. |
| 3 | Should the "See All" screen be a new feature module? | **No.** It lives inside `feature:search-movies:impl`. It reuses the same repository, use cases, and UI models. Extracting it would add module coupling overhead for zero encapsulation benefit. |
| 4 | Should the "See All" route go through `mainNavController` or `tabNavController`? | **`tabNavController`** for navigation TO the screen (it's within the Search tab). Movie detail click goes through `mainNavController` (same as other screens — movie detail is a main-level destination). |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Screen inside `feature:search-movies:impl` | New `feature:movie-list` module | Reuses all existing infra. No new module coupling. Same repository, use cases, mappers |
| 2 | New `SeeAllMoviesViewModel` (thin) | Reuse `SearchMoviesViewModel` with a mode flag | SearchMoviesVM is already complex (463 lines, 13 intents). Adding SeeAll logic would violate SRP. A dedicated thin VM (~80 lines) is cleaner |
| 3 | `SectionType` passed as `String` in route, parsed to enum | Pass enum directly | Kotlin Serialization in Nav Compose routes handles `String` natively. Enum serialization in routes requires custom serializer or name string. String → enum mapping is simple and testable |
| 4 | Navigation within tab graph (`tabNavController`) | Main nav graph | "See All" is part of the Search tab experience. Using the tab nav controller keeps the bottom bar visible and allows natural back navigation within the tab |
| 5 | Exclude "Now Playing" from "See All" | Include it | Hero carousel is a curated highlight, not a discovery section. "See All" doesn't match its UX purpose |

---

## 14. Out of Scope / Follow-ups

- **Genre sections "See All"** — genre discovery already works via chip filter + search. Could add per-genre "See All" later.
- **Pull-to-refresh on "See All" screen** — low value for a paginated list that starts fresh each time.
- **Cache for "See All" pages > 1** — could add `BrowseCacheLocalDataSource` support for paged sections. Not needed for MVP.
- **"See All" for Now Playing** — hero carousel is a special treatment. Would need a different grid layout.

---

## 15. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-08 | Initial draft |

