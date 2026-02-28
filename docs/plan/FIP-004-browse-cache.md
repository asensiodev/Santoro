# FIP — Browse Cache (Offline-First Browse)

| Field           | Value                                          |
|-----------------|------------------------------------------------|
| **FIP ID**      | FIP-004                                        |
| **Version**     | 1.0                                            |
| **Status**      | ✅ Done                                        |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-14               |
| **Feature**     | Room cache for TMDB browse results (TTL-based) |
| **Date**        | 2026-02-26                                     |
| **Author**      | @asensiodev                                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

Every time the user navigates to the Search/Browse screen, `SearchMoviesViewModel.loadInitialData()` fires parallel network calls (now playing, popular, top rated, upcoming, trending) via `fetchDashboardData()`. The current `RemoteSearchMoviesRepository` goes straight to the TMDB API — there is no caching layer.

This means:
- **Slow re-entry:** Re-opening the Search tab always shows Loading and re-hits the network, even seconds after the last visit.
- **No offline support:** A network outage means a blank error screen — even for data the user just saw.
- **Unnecessary API quota consumption:** Curated sections (now playing, trending) change infrequently; re-fetching them on every navigation is wasteful.

**Strategy: Cache-first with TTL, graceful stale fallback**
- A new Room table (`browse_cache`) stores paginated movie lists keyed by `(section, page)` + a `cachedAt` timestamp.
- On every repository call: check cache → if entry exists and `now - cachedAt < TTL` → return cached data. Otherwise, fetch from TMDB, write to cache, return fresh data.
- On network failure: if stale cache exists (any age), return it with a stale signal so the UI can show a subtle "Offline — showing cached results" banner.
- Search results use a composite cache key: `"search:$query"` + `page`. TTL for search is shorter (5 min) since results change with user intent.

---

## 2. Goals

- Eliminate redundant network calls when the user re-navigates to the browse screen within the TTL window.
- Show curated browse sections instantly from cache (zero-latency re-entry).
- Serve stale results gracefully when offline — no blank error screens for previously seen data.
- Cache paginated popular results so infinite scroll pages already loaded survive re-entry.
- Cache search results per query string to avoid re-fetching on config changes or brief navigations away.
- Keep the domain and presentation layers unaware of caching — the repository hides the strategy.

---

## 3. Non-Goals

- Caching movie detail data (cast, crew, full metadata) — fetched from a different endpoint in `feature/movie-detail`; out of scope.
- Caching by-genre browse results — lower priority; can be added later as the cache design supports it trivially.
- Persistent cache across app installs or devices — this is local-only Room cache, not Firestore.
- Invalidating cache when TMDB data actually changes — TTL expiry is the only invalidation mechanism in v1.
- Exposing a manual "Refresh" button to the user — the cache is transparent; stale-banner retry is sufficient.

---

## 4. User Stories

| ID    | As a…    | I want to…                                                   | So that…                                              | Acceptance Criteria |
|-------|----------|--------------------------------------------------------------|-------------------------------------------------------|---------------------|
| US-01 | app user | return to the Search tab and see content instantly           | I don't wait for a network round-trip every time      | Re-entering the tab within 30 min shows content immediately (no Loading state). |
| US-02 | app user | browse movies while offline                                  | the app remains useful without a connection           | With no internet, the screen shows the last cached data and a subtle "Offline" banner instead of an error screen. |
| US-03 | app user | search for a movie and return to the same search quickly     | I don't wait for re-fetching results I just saw       | Re-issuing the same query within 5 min returns instantly from cache. |
| US-04 | app user | scroll through popular movies that survive a tab switch      | I can navigate away and return without re-fetching    | All previously loaded popular pages are served from cache; only new pages hit the network. |

---

## 6. Architecture

```
Presentation (SearchMoviesViewModel)
       │ calls use cases (unchanged)
       ▼
Domain Use Cases (unchanged interface)
       │ calls SearchMoviesRepository
       ▼
CachingSearchMoviesRepository          ← NEW (wraps both data sources)
       │                   │
       ▼                   ▼
BrowseCacheLocalDataSource    RemoteSearchMoviesDatasource (existing)
(Room — browse_cache table)   (TMDB Retrofit — existing)
       │
       ▼
BrowseCacheDao (NEW)
BrowseCacheEntity (NEW)

Cache-first flow per request:
┌─────────────────────────────────────────────────────┐
│ 1. Query Room for (section, page)                   │
│ 2a. Cache HIT + fresh (within TTL) → emit Success   │
│ 2b. Cache HIT + stale OR MISS → fetch TMDB          │
│     → on success: write to Room, emit Success        │
│     → on failure + stale cache: emit Stale(data)    │
│     → on failure + no cache: emit Error              │
└─────────────────────────────────────────────────────┘
```

The stale signal is surfaced via `StaleDataException` — an internal typed exception emitted after the stale `Success`. The ViewModel catches it by type to set `isShowingStaleData = true` while keeping the content visible. The domain interface (`SearchMoviesRepository`) remains completely unchanged.

---

## 7. Data Model — Room Table

### `BrowseCacheEntity` — new Room entity, table `browse_cache`

| Column       | Type   | Notes                                                                                          |
|--------------|--------|------------------------------------------------------------------------------------------------|
| `section`    | String | Section identifier: `popular`, `now_playing`, `top_rated`, `upcoming`, `trending`, `search:$query` |
| `page`       | Int    | TMDB page number (1-based)                                                                     |
| `moviesJson` | String | Gson-serialised `List<MovieApiModel>`                                                          |
| `cachedAt`   | Long   | `System.currentTimeMillis()` at time of write                                                  |

**Primary key:** composite `(section, page)`.
**Conflict strategy:** `INSERT OR REPLACE` — refreshing a page overwrites the old entry.

---

## 8. Modules Affected

| Module | Change |
|---|---|
| `core/database` | Add `BrowseCacheEntity`, `BrowseCacheDao`; register in `SantoroRoomDatabase` (version 2→3 + migration); expose DAO from `DatabaseModule` |
| `feature/search-movies/impl` | Add `BrowseCacheLocalDataSource` (interface + Room impl), `CachingSearchMoviesRepository` (new impl of `SearchMoviesRepository`), `BrowseSectionKeys`, `BrowseCacheTtl`, `StaleDataException`; update `SearchMoviesModule`; update `SearchMoviesUiState` + `SearchMoviesScreen` for offline banner |
| `core/string-resources` | Add `browse_offline_cache_banner` string |

---

## 9. Phases & Tasks

### Phase 1 — Room: `BrowseCacheEntity` + `BrowseCacheDao`

- [x] Create `BrowseCacheEntity` data class in `core/database/`:
  - `@Entity(tableName = "browse_cache", primaryKeys = ["section", "page"])`
  - Fields: `section: String`, `page: Int`, `moviesJson: String`, `cachedAt: Long`
- [x] Create `BrowseCacheDao` interface:
  - `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPage(entry: BrowseCacheEntity)`
  - `@Query("SELECT * FROM browse_cache WHERE section = :section AND page = :page LIMIT 1") suspend fun getPage(section: String, page: Int): BrowseCacheEntity?`
  - `@Query("DELETE FROM browse_cache WHERE section = :section") suspend fun clearSection(section: String)`
  - `@Query("DELETE FROM browse_cache WHERE cachedAt < :cutoff") suspend fun clearEntriesOlderThan(cutoff: Long)`
  - `@Query("DELETE FROM browse_cache") suspend fun clearAll()`
- [x] Register `BrowseCacheEntity` in `SantoroRoomDatabase`; bump version 2→3
- [x] Write `MIGRATION_2_3`: `CREATE TABLE IF NOT EXISTS browse_cache (section TEXT NOT NULL, page INTEGER NOT NULL, moviesJson TEXT NOT NULL, cachedAt INTEGER NOT NULL, PRIMARY KEY(section, page))`
- [x] Add `MIGRATION_2_3` to `Room.databaseBuilder` in `DatabaseModule`
- [x] Expose `fun browseCacheDao(): BrowseCacheDao` in `SantoroRoomDatabase`
- [x] Add `@Provides fun provideBrowseCacheDao(db: SantoroRoomDatabase): BrowseCacheDao` in `DatabaseModule`

### Phase 2 — `BrowseCacheLocalDataSource` in `feature/search-movies/impl`

- [x] Create `BrowseCacheEntry` data class: `section: String, page: Int, movies: List<Movie>, cachedAt: Long`
- [x] Create `BrowseCacheLocalDataSource` interface:
  - `suspend fun getCachedPage(section: String, page: Int): BrowseCacheEntry?`
  - `suspend fun savePage(section: String, page: Int, movies: List<Movie>, cachedAt: Long)`
  - `suspend fun clearSection(section: String)`
  - `suspend fun clearStaleEntries(cutoffMs: Long)`
- [x] Create `RoomBrowseCacheDataSource` implementing `BrowseCacheLocalDataSource`:
  - Inject `BrowseCacheDao` and `Gson`
  - `getCachedPage`: query DAO → deserialise `moviesJson` via Gson → map `MovieApiModel` → `toDomain()` → return `BrowseCacheEntry`; return `null` if DAO returns `null`
  - `savePage`: map `List<Movie>` → `List<MovieApiModel>` via reverse mapper → Gson → `upsertPage(BrowseCacheEntity(...))`
  - `clearSection` / `clearStaleEntries`: delegate to DAO
- [x] Add `toApiModel()` reverse mapper extension (`Movie` → `MovieApiModel`) covering fields available in list responses: `id`, `title`, `overview`, `posterPath`, `backdropPath`, `releaseDate`, `popularity`, `voteAverage`, `voteCount`, `genreIds`
- [x] Add `projects.core.database` dependency to `feature/search-movies/impl/build.gradle.kts`

### Phase 3 — `CachingSearchMoviesRepository`

- [x] Create `BrowseCacheTtl` object:
  - `CURATED_MS = 30 * 60 * 1000L`
  - `SEARCH_MS = 5 * 60 * 1000L`
- [x] Create `BrowseSectionKeys` object:
  - `const val NOW_PLAYING`, `POPULAR`, `TOP_RATED`, `UPCOMING`, `TRENDING`
  - `fun searchKey(query: String): String = "search:${query.lowercase().trim()}"`
- [x] Create `StaleDataException : Exception()` in `feature/search-movies/impl/data/`
- [x] Create `CachingSearchMoviesRepository` implementing `SearchMoviesRepository`, injecting `BrowseCacheLocalDataSource`, `SearchMoviesDatasource` (remote), `DispatcherProvider`:
  - Private `cachedFlow(section, page, ttlMs, remoteFetch)` helper with full cache-first + stale fallback logic
  - Wire all methods: `searchMovies`, `getNowPlayingMovies`, `getPopularMovies`, `getTopRatedMovies`, `getUpcomingMovies`, `getTrendingMovies` → `cachedFlow` with appropriate section key and TTL
  - `getMoviesByGenre(genreId, page)` → **no cache** in v1; delegate directly to remote
- [x] Call `localDataSource.clearStaleEntries(...)` via `clearStaleEntries()` public method called from ViewModel's `fetchDashboardData`

### Phase 4 — Wire `CachingSearchMoviesRepository` into DI

- [x] Update `SearchMoviesModule`:
  - Add `@Provides` for `BrowseCacheLocalDataSource` → `RoomBrowseCacheDataSource`
  - Provide `CachingSearchMoviesRepository` as concrete type + as `SearchMoviesRepository` interface
  - Keep `RemoteSearchMoviesDatasource` provision unchanged
- [x] Add `provideGson()` to `NetworkProvidesModule` and wire into `GsonConverterFactory`
- [x] Delete `RemoteSearchMoviesRepository` and `RemoteSearchMoviesRepositoryTest`

### Phase 5 — UI: offline banner in `SearchMoviesScreen`

- [x] Add `isShowingStaleData: Boolean = false` field to `SearchMoviesUiState`
- [x] In `SearchMoviesViewModel`, handle `StaleDataException` → `isShowingStaleData = true`, keep content visible
- [x] In `SearchMoviesViewModel`, fresh `Result.Success` → `isShowingStaleData = false`
- [x] In `SearchMoviesScreen`, add `AnimatedVisibility` `OfflineBanner` composable below genre chips
- [x] Add `browse_offline_cache_banner` and `browse_offline_retry` to `core/string-resources`

### Phase 6 — Tests

- [x] Unit — `BrowseCacheDao` (Room in-memory): upsert + query returns correct entry; upsert same key overwrites; `clearSection` removes only that section; `clearEntriesOlderThan` removes only old entries; `clearAll` empties table
- [x] Unit — `RoomBrowseCacheDataSource`: `getCachedPage` → null when DAO returns null; correct deserialization; `savePage` → correct serialization passed to DAO; `clearStaleEntries` → correct cutoff passed
- [x] Unit — `CachingSearchMoviesRepository.getPopularMovies`:
  - GIVEN fresh cache WHEN invoked THEN emits cached movies without calling remote
  - GIVEN expired cache WHEN invoked THEN calls remote, saves result, emits fresh movies
  - GIVEN no cache + network success THEN saves and emits
  - GIVEN no cache + `IOException` THEN emits `Result.Error`
  - GIVEN stale cache + `IOException` THEN emits `Result.Success(staleMovies)` followed by `Result.Error(StaleDataException)`
- [x] Unit — `CachingSearchMoviesRepository.searchMovies`:
  - GIVEN fresh cache for `"inception"` page 1 THEN cached result returned
  - GIVEN expired/missing cache THEN remote called and result cached
  - GIVEN different query THEN treated as different cache key
- [x] Unit — `SearchMoviesViewModel`:
  - GIVEN `StaleDataException` from use case THEN `isShowingStaleData = true`, `screenState = Content`
  - GIVEN subsequent fresh result THEN `isShowingStaleData = false`
  - GIVEN `Result.Error(IOException)` with no cache THEN `screenState = Error`, `isShowingStaleData = false`

---

### Technical Notes

#### TTL Strategy

| Section | TTL | Rationale |
|---|---|---|
| `now_playing`, `trending`, `popular`, `top_rated`, `upcoming` | 30 min | Changes daily on TMDB; 30 min is a safe refresh cadence |
| `search:$query` | 5 min | Query results may evolve; users expect fresher data when searching |
| By genre | Not cached v1 | Lower traffic path; trivial to add later with same pattern |

#### Pagination & Cache Interaction

Popular is the only section with user-driven infinite scroll. Pages already loaded (page 1, 2, 3…) are each cached independently under key `("popular", N)`. On re-entry to the tab, `fetchDashboardData()` re-fetches page 1 of each section — within TTL this is instant. Subsequent pages from `loadMorePopularMovies()` also benefit from per-page caching transparently.

#### Search Cache Key Strategy

Key = `"search:${query.lowercase().trim()}"` — normalised to avoid duplicate entries for `" Inception "` vs `"inception"`. Each page is cached independently. Minimum query length for caching: anything that passes `query.isNotBlank()`.

#### Reverse Mapper (`Movie` → `MovieApiModel`)

Only a subset of fields are needed: `id`, `title`, `overview`, `posterPath`, `backdropPath`, `releaseDate`, `popularity`, `voteAverage`, `voteCount`, `genreIds`. The `genres` field (full name objects) is null in list responses — TMDB returns `genre_ids` in list endpoints. This reuses the existing `MovieApiModel` Gson serialisation already in `core/data`.

#### Offline Degradation UX Contract

| State | UI |
|---|---|
| Fresh data available | Content screen, no banner |
| Network error + stale cache | Content screen + `isShowingStaleData = true` → banner with Retry |
| Network error + no cache at all | `SearchScreenState.Error` with Retry |

#### `StaleDataException` Design

Using a typed exception rather than a new `CacheResult<T>` sealed class keeps the domain interface (`SearchMoviesRepository`, all use cases) completely unchanged. The exception is internal to `feature/search-movies/impl` — only `CachingSearchMoviesRepository` emits it and only `SearchMoviesViewModel` handles it.

---

## 10. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should genre-browse results be cached in v1? | **No** — deferred. Lower traffic path; design supports it trivially when needed. |
| 2 | Should there be a pull-to-refresh to force-bypass the cache? | **No** for v1. Cache expires naturally via TTL. |
| 3 | Where should `clearStaleEntries` be called? | ✅ **Inside `CachingSearchMoviesRepository`** at the start of initial dashboard load. Cleanup only happens during active browse — not on every app start. |
| 4 | Should cached movies reflect `isWatched`/`isInWatchlist`? | **No.** Cache stores raw TMDB browse results. Watch state is merged from `DatabaseRepository` by the ViewModel — same as today. |
| 5 | Should `RemoteSearchMoviesRepository` be deleted? | ✅ **Yes — delete both `RemoteSearchMoviesRepository` and `RemoteSearchMoviesRepositoryTest`.** The test instantiates the concrete class directly (not the interface), so it must be replaced by `CachingSearchMoviesRepositoryTest`. The datasource (`SearchMoviesDatasource`) is injected directly into `CachingSearchMoviesRepository`. |
| 6 | Stale signal approach? | ✅ **`StaleDataException`** — domain interface stays untouched. Internal to `feature/search-movies/impl`. |

---

## 11. Out of Scope / Follow-ups

- Genre-browse cache (`"genre:$genreId"` key)
- Pull-to-refresh to force cache invalidation
- Cache size cap (max N entries per section) — TTL + `clearStaleEntries` is sufficient for v1
- Movie detail cache (cast, crew, full metadata)
- Exposing cache age to the user ("Last updated 10 min ago")

---

## 12. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-26 | Initial draft |
