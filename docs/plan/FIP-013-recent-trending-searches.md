# FIP — Search: Recent & Trending Searches

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-013                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | ✅ Done                                                             |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-22                                   |
| **Feature**            | Search: Recent & Trending Searches                                 |
| **Date**               | 2026-02-28                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The current `SearchMoviesScreen` shows the browse grid immediately when the search field is empty. There is no search history or quick-access suggestions.

Top-tier apps (YouTube, Spotify, Letterboxd) surface **recent searches** and **trending content** when the user focuses the search field without typing, dramatically reducing re-search friction and giving the screen a polished, purposeful feel even before the user types anything.

---

## 2. Goals

- When the search field is **focused and empty**, replace the browse grid with a `SearchSuggestionsScreen` composable showing:
  1. **Recent Searches** — last 5 queries stored locally in DataStore, each tappable to re-run.
  2. **Trending Searches** — titles from the existing `GetTrendingMoviesUseCase` shown as `SuggestionChip` chips.
- Persist recent searches across app restarts using DataStore (not Room — ephemeral, no sync).
- Provide a **Clear all** action for recent searches.
- Clearing the field returns the user to this suggestions state.

---

## 3. Non-Goals

- Autocomplete / query completion as the user types.
- Server-side trending search queries (uses `/trending` movie titles as proxy).
- Syncing search history to Firebase / cloud.
- Infinite scroll or pagination on the suggestions screen.

---

## 4. User Stories

| ID    | As a…         | I want to…                                           | So that…                                       | Acceptance Criteria |
|-------|---------------|------------------------------------------------------|------------------------------------------------|---------------------|
| US-01 | Search user   | See my last searches when I focus the search field   | I can quickly re-run a previous query          | Up to 5 recent chips shown; oldest removed when limit exceeded |
| US-02 | Search user   | Tap a recent search chip to re-run it                | I don't have to retype the same query          | Query filled in field + search triggered immediately |
| US-03 | Search user   | Clear my search history                              | I can start fresh without old suggestions      | "Clear all" removes all entries from DataStore and UI |
| US-04 | Search user   | See trending movies as quick-access chips            | I can discover popular content with one tap    | Trending title chips shown below recent section |
| US-05 | Search user   | Return to suggestions when I clear the search field  | The screen feels consistent and purposeful     | Clearing field (X button) restores suggestions view |

---

## 5. UX / Flows

### Screen: SearchMoviesScreen

| State              | What the user sees                                                  | User actions available                         |
|--------------------|---------------------------------------------------------------------|------------------------------------------------|
| Field empty + focused | `SearchSuggestionsContent` below search bar — Recent + Trending sections | Tap chip → fills query + searches; Clear all → wipes history |
| Field non-empty    | Existing search results grid (unchanged)                            | Type, clear, paginate                          |
| Trending loading   | Recent section shown; trending section shows `LoadingIndicator`     | —                                              |
| Trending error     | Recent section shown; trending section hidden silently              | —                                              |
| No recents         | "Recent searches" section hidden; only trending shown               | —                                              |

#### Suggestions layout (top → bottom)

```
[ Recent Searches ]  — header + "Clear all" TextButton (hidden when empty)
  chip chip chip      — FlowRow of SuggestionChip, max 5

[ Trending Now ]     — header
  chip chip chip      — LazyRow of SuggestionChip (movie titles from /trending)
```

---

## 6. Architecture

```
SearchMoviesScreen
       │ observes query + isFieldFocused
SearchMoviesViewModel
       │ new: SaveRecentSearchUseCase / GetRecentSearchesUseCase / ClearRecentSearchesUseCase
       │ existing: GetTrendingMoviesUseCase (reused)
RecentSearchesRepository (new interface in domain)
       │
DataStoreRecentSearchesDataSource (new impl in data)
       │
DataStore<Preferences>
```

---

## 7. Data Model

### New: DataStore key

- Key: `recent_searches` — stores a JSON array of up to 5 query strings.
- Serialized as a `Set<String>` with insertion-order preserved via `List` + JSON encoding.

### Modified: `SearchMoviesUiState`

Add:
```kotlin
val recentSearches: List<String> = emptyList()
val trendingSuggestions: List<String> = emptyList()
val isFieldFocused: Boolean = false
```

### New intents in `SearchMoviesIntent`

```kotlin
data object FieldFocused : SearchMoviesIntent
data object FieldCleared : SearchMoviesIntent
data class SuggestionTapped(val query: String) : SearchMoviesIntent
data object ClearRecentSearches : SearchMoviesIntent
```

---

## 8. Modules Affected

- `feature/search-movies/impl` — new use cases, new data source, new composable, updated ViewModel + UiState + Intent
- `core/string-resources` — new string keys (EN + ES)
- `gradle/libs.versions.toml` — add DataStore dependency
- `feature/search-movies/impl/build.gradle.kts` — add DataStore dependency

---

## 9. Phases & Tasks

### Phase 1 — Dependencies

- [x] Add `androidx-datastore-preferences` to `libs.versions.toml` (version `1.1.4`)
- [x] Add `datastore-preferences` to `feature/search-movies/impl/build.gradle.kts`

### Phase 2 — Domain

- [x] Create `RecentSearchesRepository` interface in `feature/search-movies/impl/.../domain/repository/`
  - `fun getRecentSearches(): Flow<List<String>>`
  - `suspend fun saveSearch(query: String)`
  - `suspend fun clearAll()`
- [x] Create `GetRecentSearchesUseCase` — collects `RecentSearchesRepository.getRecentSearches()`
- [x] Create `SaveRecentSearchUseCase` — calls `repo.saveSearch(query)` (trims + deduplicates + enforces max 5)
- [x] Create `ClearRecentSearchesUseCase` — calls `repo.clearAll()`

### Phase 3 — Data

- [x] Create `DataStoreRecentSearchesDataSource` implementing `RecentSearchesRepository`
  - Inject `DataStore<Preferences>` via Hilt
  - Serialize/deserialize using `Gson` (already in dependencies) as JSON array of strings
  - `saveSearch`: prepend new query, deduplicate, keep max 5, persist
  - `clearAll`: write empty list
- [x] Provide `DataStore<Preferences>` in `SearchMoviesModule` via `@Provides @Singleton`
  - Use `preferencesDataStore(name = "recent_searches")` extension (top-level property on Application context)
- [x] Bind `DataStoreRecentSearchesDataSource` to `RecentSearchesRepository` in `SearchMoviesModule`

### Phase 4 — Presentation

- [x] Add `recentSearches`, `trendingSuggestions`, `isFieldFocused` to `SearchMoviesUiState`
- [x] Add `FieldFocused`, `FieldCleared`, `SuggestionTapped`, `ClearRecentSearches` to `SearchMoviesIntent`
- [x] Inject `GetRecentSearchesUseCase`, `SaveRecentSearchUseCase`, `ClearRecentSearchesUseCase` into `SearchMoviesViewModel`
- [x] In `process(LoadInitialData)`: launch parallel coroutine collecting `getRecentSearchesUseCase()` into `uiState.recentSearches`
- [x] In `process(UpdateQuery)`: when query becomes non-blank, call `saveRecentSearchUseCase(query)` (debounced — save only when search is actually triggered, not on every keystroke)
- [x] In `process(SuggestionTapped)`: fill query + trigger search immediately
- [x] In `process(ClearRecentSearches)`: call `clearRecentSearchesUseCase()`
- [x] In `process(FieldFocused)` / `process(FieldCleared)`: toggle `uiState.isFieldFocused`
- [x] In `SearchMoviesScreen`: pass `onFocusChanged` to the search bar; when `isFieldFocused && query.isBlank()`, show `SearchSuggestionsContent` instead of browse/results grid

### Phase 5 — UI

- [x] Create `SearchSuggestionsContent` composable in `.../presentation/component/`
  - Parameters: `recentSearches: List<String>`, `trendingSuggestions: List<String>`, `onSuggestionTap: (String) -> Unit`, `onClearRecents: () -> Unit`, `modifier: Modifier = Modifier`
  - Layout: `LazyColumn` with two sticky-header-style sections
  - Recent section: header Row with label + `TextButton("Clear all")` (hidden when `recentSearches.isEmpty()`); `FlowRow` of `SuggestionChip`
  - Trending section: header Text; `LazyRow` of `SuggestionChip`
  - All strings via `stringResource`
- [x] Wire `SearchSuggestionsContent` into `SearchMoviesScreen` — shown when `uiState.isFieldFocused && uiState.query.isBlank()`

### Phase 6 — String resources

- [x] Add to `core/string-resources/src/main/res/values/strings.xml` (EN):
  ```xml
  <string name="search_suggestions_recent_title">Recent searches</string>
  <string name="search_suggestions_clear_all">Clear all</string>
  <string name="search_suggestions_trending_title">Trending now</string>
  ```
- [x] Add equivalents to `core/string-resources/src/main/res/values-es/strings.xml` (ES):
  ```xml
  <string name="search_suggestions_recent_title">Búsquedas recientes</string>
  <string name="search_suggestions_clear_all">Borrar todo</string>
  <string name="search_suggestions_trending_title">Tendencias</string>
  ```

### Phase 7 — Tests

- [x] `SaveRecentSearchUseCaseTest`
  - GIVEN empty history WHEN save "inception" THEN history is ["inception"]
  - GIVEN 5 entries WHEN save new query THEN oldest removed, new query at front
  - GIVEN duplicate "inception" WHEN save "inception" THEN no duplicate, moved to front
- [x] `GetRecentSearchesUseCaseTest`
  - GIVEN history ["inception", "avatar"] WHEN invoke THEN emits same list
- [x] `ClearRecentSearchesUseCaseTest`
  - GIVEN non-empty history WHEN clearAll THEN history emits empty list
- [x] `SearchMoviesViewModelTest` — add cases:
  - GIVEN field focused with empty query WHEN FieldFocused intent THEN `isFieldFocused = true`
  - GIVEN suggestion tapped WHEN SuggestionTapped("avatar") THEN query = "avatar" and search triggered
  - GIVEN recent searches present WHEN ClearRecentSearches THEN `recentSearches` is empty

---

## 10. Validation

| What                        | Result | Notes |
|-----------------------------|--------|-------|
| Manual test on real device  | ⏭️     | Post-implementation |
| Manual test on emulator     | ⏭️     | Post-implementation |
| Edge cases verified         | ⏭️     | Post-implementation |
| Accessibility check         | ⏭️     | Post-implementation |

---

## 11. Blockers

_None._

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Save search on every keystroke or only when search is triggered? | Only when search is triggered (debounced) to avoid noisy history |
| 2 | Trending: full movie title or truncated? | Full title, chips will wrap via FlowRow |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Use DataStore for recent searches | Room, SharedPreferences | DataStore is modern, coroutine-native, and ephemeral storage fits perfectly |
| 2 | Reuse `GetTrendingMoviesUseCase` for trending suggestions | New dedicated endpoint | Zero new network calls; trending movies already cached |
| 3 | Store as JSON array in a single Preferences key | Multiple keys | Simpler to manage order and deduplication in one serialization step |

---

## 14. Out of Scope / Follow-ups

- Autocomplete as user types (future feature)
- Sync search history to cloud (out of scope for v1)
- F-18 "See All" navigation (separate FIP)

---

## 16. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-28 | Initial draft |
