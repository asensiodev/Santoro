## Context

See proposal.md for motivation and scope. Relevant observed code:

- `MovieEntity` (Room v5) has no director or crew; `Movie.director` and `Movie.crew` exist only on detail-loaded domain movies.
- `MovieDetailViewModel` is the only caller of `MovieMutationRepository.updateMovieState`, so every saved movie passes through a fully loaded detail.
- `FirestoreMovieDataSource` and `MovieSyncEntity` sync poster, genres and watched timestamps, but no director.
- `WatchedMoviesViewModel` groups by `watchedDate` month; `WatchedMoviesUiState.movies` is `Map<String, List<MovieUi>>`.
- `feature/search-movies` already uses DataStore Preferences through a data source; `core/sync` owns WorkManager workers and Firestore.
- F-29 exposes `PersonDetailRoute(personId)` in `feature/person-detail/api`.

## Goals / Non-Goals

**Goals:** Two view modes and four groupings over local data; persistent per-device preferences; director data captured at save time and resolved for existing movies by a job that can be deleted later without data loss.

**Non-Goals:** Director filmography and completion (F-32), custom folders, cross-device preference sync, grouping by other attributes.

## Decisions

### Director data model

Add a nullable `directors` value to the domain `Movie`: `null` means unresolved, an empty list means resolved without a director. Each entry holds TMDB person ID and name. Room migration 5 → 6 adds a nullable `directors` TEXT column (JSON, following the existing `genres` JSON convention). The detail mapper fills `directors` from crew entries with `CrewRole.DIRECTOR`, so every new save stores them.

Alternative: a separate `movie_directors` table with a join. It allows indexed director queries, but collection grouping happens in memory over at most hundreds of movies; the JSON column matches existing conventions and keeps the migration additive.

### Firestore

Upload `directors` as a JSON string of `{id, name}` objects, following the existing `genres` field convention; on download, an absent field or malformed JSON maps to `null` (unresolved), never to a resolved empty list, so resolution is retried. Older app versions ignore the field.

Task 1.4 result: release 1.1.1 uploads with `transaction.set(document, map)` without `SetOptions.merge()`, which replaces the whole document. An older client that uploads a movie therefore removes its `directors` field. Accepted limitation: it requires the same account on two devices running different app versions, and the newer device keeps its local directors because the download merge only updates list flags for existing rows. Only a later fresh restore would download the movie as unresolved, and the backfill or the detail fallback resolves it again; the user sees the movie under Unknown director for at most one backfill run.

The download merge does not fill a local unresolved row from remote directors. That case needs another device to have resolved the movie first, and the local backfill resolves it with one request; filling it would add a DAO path for no visible benefit.

### Removable backfill

`DirectorBackfillWorker` in `core/sync`: unique one-time work (`santoro_director_backfill`) with `ExistingWorkPolicy.KEEP`, network constraint and exponential backoff. `SyncWorker` enqueues it after every successful download and merge; that sync already runs on each authenticated session start (`scheduleSessionSync`), after a restore and every six hours, and waits for network when offline. A single hook covers app start, restore and offline starts, and KEEP makes a run with nothing left cost one Room query.

The worker reads watched movie IDs with `directors IS NULL` (`SyncStore`), requests TMDB `GET /3/movie/{id}/credits` sequentially (`TmdbMovieDirectorsDataSource`, reusing `CreditsApiModel` and the director rule from `core/data`; 404 resolves empty), and stores each result through `MovieMutationRepository.storeResolvedDirectors`. The first failure stops the run and returns retry, keeping movies already stored; `CancellationException` propagates. If anything was stored, it schedules one immediate sync to upload the local snapshot instead of one upload request per movie. The resulting sync re-enqueues the backfill, which finds nothing and does not sync again, so the chain terminates. It logs `director_backfill_finished` with `resolved` and `remaining` counts and records `director_backfill_failed` errors.

`storeResolvedDirectors` is a conditional write (`UPDATE … WHERE directors IS NULL`) that bumps `updatedAt` so the upload is not skipped by the remote-newer check, never overwrites directors saved meanwhile by detail, and returns whether a row changed. It lives on `MovieMutationRepository` because both the worker and the detail fallback use it.

The worker is removable because it is not the source of truth: new saves store directors, Firestore carries them across devices, and the movie-detail fallback resolves any remaining unresolved watched movie on open. Removal criterion: once `remaining` stays at zero across active installs for several weeks, delete the worker, its enqueue calls and its credits request; keep the column, sync field and fallback.

Alternative: resolving directors only on demand when Director grouping is opened. It ties a network fan-out to a screen's lifecycle and shows a partially filled grouping on every first visit.

### Detail fallback

After a remote detail load the ViewModel sees resolved directors and cannot tell whether Room still holds them as unresolved. When `MovieDetailViewModel` loads a watched movie with resolved directors, it calls the conditional `storeResolvedDirectors` once per load; Room only writes if the stored value is unresolved, and the ViewModel enqueues the existing per-movie upload only when a row changed. Offline loads come from Room with unresolved directors and do nothing. List flags are never changed, and a storage failure is silent because the screen content does not depend on it.

### Collection presentation

The current Watched screen is already a poster grid (`LazyVerticalGrid`, adaptive ~100dp cells, non-sticky month headings). Decision confirmed by the user on 2026-10-28: that grid becomes Shelf with three fixed columns and is the default, so existing users see no change; List is the new mode, rows with a small poster, title and "year · director". Headings stay non-sticky as today, because `LazyVerticalGrid` has no sticky headers and rebuilding the screen on a list for that alone adds risk without credible user benefit. Date watched keeps today's order inside a month (most recently watched first).

Add `WatchedViewMode` (Shelf, List) and `WatchedGrouping` (DateWatched, Director, Genre, Decade) to `core/domain`, persisted through the existing `UserPreferencesRepository` (secure key-value store, which sign-out does not clear), following `hasSeenDetailTooltip`, instead of a new DataStore data source. A pure grouping function in the presentation mapper produces ordered `WatchedGroupUi(key, label, count, directorId?, movies)` from domain movies; lazy item keys combine group key and movie ID so multi-membership stays unique. Labels for Unknown director, No genre and Undated use string resources; decade labels use a localized resource. The screen keeps one `LazyVerticalGrid`: Shelf uses three columns per row, List uses full-span rows; headings are full-span items; keys and content types stay stable. Mode and grouping are durable state in `WatchedMoviesUiState`; change intents update state immediately, persist through `UserPreferencesRepository`, and send analytics (`watched_view_mode_changed`, `watched_grouping_changed`).

Preferences live in the secure key-value store, which sign-out does not clear (F-28 clears only movie rows), so a choice survives app restarts and sign out/in on the same device. A different account on the same device inherits the view preference; this is a display setting, not account data, so it is accepted. Change events alone would under-count returning users whose choice is already saved, so the ViewModel also records `watched_collection_viewed` with `mode` and `grouping` once per screen entry with populated content. This gives the real distribution of views in use through the existing `ObservabilityTracker.trackAction`, without a new user-property API.

Saved preferences are read once before the movies collector starts, so the first content frame already uses them, and then combined in memory with the single long-lived movies collector, keeping latest-wins behavior for queries. A read failure falls back to defaults; a write failure keeps the in-memory choice for the session.

Director heading clicks call an `onDirectorClick(personId)` callback, like the existing `onMovieClick`, navigating to `PersonDetailRoute(personId)` through the app graph; no ViewModel logic is involved, so no effect is needed.

### Screen revision (2026-11-01)

After testing the first implementation on the emulator, the user asked for a layout closer to the earlier design exploration, constrained to the app's real data and design system. The user approved the revised mockups (canvas "Santoro · Colección", row "Propuesta v2") on 2026-11-01.

- **Header.** A `headlineMedium` "Watched" title and a summary line with the total watched movie count, plus a search `IconButton` and the existing `SingleChoiceSegmentedButtonRow` toggle on the right. The total is the number of distinct movie IDs in the unfiltered collection, so multi-membership does not inflate it. It is computed when the query is blank and kept while searching; the header is hidden during search anyway. The summary shows only the movie count; detailed figures belong to the future statistics screen. During Loading, Error and Empty, only the title is shown.
- **Search on demand.** `WatchedMoviesUiState` gains `isSearchOpen` as durable state. `OpenSearch` sets it; `CloseSearch` clears it and the query in one update, so the latest-wins query collector re-emits the full collection. The open header is a back `IconButton` and the existing `QueryTextField`, which already has a clear action; a `FocusRequester` focuses it when `isSearchOpen` becomes true, and a `BackHandler` enabled only while search is open sends `CloseSearch`. `CloseSearch` also regroups the last full collection immediately instead of waiting for the debounced query, and the screen scrolls the grid to the top when search closes (revision 2026-11-01, after manual checks showed the closed screen kept the filtered list's scroll position; regrouping first avoids the lazy grid re-anchoring to a filtered key). When the movies collector reports no watched movies, the same update closes search, so the Empty state never shows a search field. The view toggle is hidden while searching; the chips stay, so the user can regroup matches. Search stays title-only (`searchWatchedMoviesByTitle`). No analytics event is added for search.
- **Chips.** The grouping chips reuse the Search screen genre chip shape: `FilterChip` with `RoundedCornerShape(Size.size16)`. Selected colors were first `primaryContainer` / `onPrimaryContainer` like the Search chips; manual checks showed the selected chip looked dimmer than unselected ones in dark theme, so on 2026-11-01 they changed to `secondaryContainer` / `onSecondaryContainer`, matching the view toggle, with the default 48dp minimum touch target instead of today's forced 48dp visual height. They keep a leading check icon on the selected chip: `primaryContainerDark` is close to the dark background, so without the check, the selected chip would differ from the others only by a faint fill.
- **Headings.** Resolved director headings become a full-width tonal row (`surfaceContainer`, `shapes.medium`, minimum height 56dp) with name, count and chevron, in both modes. The Unknown director heading and all other groupings keep plain 48dp headings without a container. Headings stay non-sticky (see above). Completion progress in headings is out of scope: F-32 explicitly excludes it because it needs one filmography request per director on screen.
- **Stats removal.** Remove `WatchedStatsDashboard`, its screenshot test and golden, the `LoadStats` intent, `stats` in UI state, the stats collector in the ViewModel and its use-case dependency. Keep `GetWatchedStatsUseCase`, `WatchedStats`, their unit test, the Hilt provider and the `watched_stat_*` strings, so the statistics screen can reuse them. Until then the use case has no consumer, which is accepted.

Rejected alternatives: a permanently visible search field (it takes the most valuable row on a browsing screen, and search is a secondary task here); a search action inside the chip row (it would scroll off with the chips on narrow screens and large fonts).

Accepted limitations:
- Open search is not restored after process death, because the ViewModel does not use `SavedStateHandle`. The user returns to the closed header with the full collection, so no data is lost.
- Users used to the always-visible field must tap the search icon first. The icon sits in the header in the standard position, so the cost is one tap.

### Release date restoration (2026-11-01)

Found during manual checks: Firestore documents never carried the release date, so `MovieSyncData.toEntity()` restored every movie with `releaseDate = null`. Any user who changes device, reinstalls, or signs out and in (F-28 clears local movie rows) saw most of the collection under Undated in Decade grouping and List rows without a year. The original device keeps complete rows. The user chose to fix it within F-31 on 2026-11-01, including movies already restored.

- **Tri-state release date.** `null` means unresolved; an empty string means TMDB has no release date; otherwise the TMDB date. Presentation already treats blank as undated. No Room migration: the column is already nullable, and the only rows with `null` are restored rows and movies saved without a TMDB date, which one request resolves.
- **Firestore.** Upload `releaseDate` as a string field, following `directors`. On download, an absent or null field maps to `null` (unresolved). Older clients overwrite documents without the field, the same accepted limitation as `directors` (task 1.4): a later restore resolves it again through the backfill.
- **Backfill.** `DirectorBackfillWorker` keeps its class, unique work name and analytics events, so the removal criterion and dashboards stay continuous, but now selects watched movies whose directors or release date are unresolved. The credits request becomes `GET /3/movie/{id}?append_to_response=credits` (the movie detail request, `MovieApiModel`), so one request resolves both values; `MovieDirectorsRemoteDataSource` becomes `MovieMetadataRemoteDataSource`, returning directors (empty when credits are missing) and the release date (empty when absent). A 404 resolves both as empty.
- **Conditional store.** `MovieMutationRepository.storeResolvedDirectors` becomes `storeResolvedMetadata(movieId, directors?, releaseDate?)`. The update fills only columns that are `null` and receive a non-null value, bumps `updatedAt` only when something changes, and returns whether a row changed; the existing upload-on-change behaviour is unchanged. Because the worker uploads its local snapshot afterwards, the restored device writes the resolved dates back to Firestore and later restores include them.
- **Detail fallback.** For a watched movie, `MovieDetailViewModel` passes the loaded directors and release date as they are. A Room load passes values that are either already stored or `null`, which the conditional update ignores; a remote load fills whatever is missing. A remote detail with a `null` release date leaves it unresolved for the backfill.

Rejected: filling unresolved local rows from remote values during the download merge (needs another DAO path, and the backfill resolves them with one request), and a separate release-date worker (a second request per movie for the same endpoint).

### Offline posters and detail (2026-11-01)

Found in manual checks with airplane mode and an empty image cache. The user asked to fix both within F-31 on 2026-11-01 ("mete los dos ahora mejor").

- **Watched posters.** Shelf cells showed a blank card when the poster failed to load, so the movie could not be identified, which contradicts the existing "Missing poster" scenario. `MovieCard` and the List thumbnail now draw the existing placeholder (icon, and title in Shelf) underneath the image, following the cast portrait pattern in movie detail: the image covers it once it loads, and failure or loading leaves it visible. The title text is always in the semantics tree, so the image itself carries no content description.
- **Movie detail without remote data.** The hero backdrop stretched the placeholder icon across the header and the poster cropped it. The backdrop now shows no error image over a neutral `surfaceVariant` base under the existing gradient, and the poster draws the centered placeholder icon on `surfaceVariant` underneath the image. The Cast heading is hidden when the movie has no cast, as the crew grid already is.
- Out of scope, agreed: filling overview, rating, countries and tagline for restored movies; offline detail shows "No description available" and an unknown country for them until opened online.

### Data sources and side effects

- Allowed: Room reads/writes for directors and release dates, `UserPreferencesRepository` preferences, TMDB `movie/{id}?append_to_response=credits`, the existing upload scheduling, analytics events listed above, navigation to person detail.
- Forbidden: changing watched/watchlist flags or timestamps during backfill or fallback, new Firestore collections, person persistence, requests on every screen visit.

### Failure UX

Grouping never blocks on network. Unresolved movies appear under Unknown director. Backfill failures are invisible to the user and retried by WorkManager. Existing Watched loading/error/empty states are unchanged.

## Test Boundaries

Confirmed by the user on 2026-10-27 ("1 ok … 2 ok … empieza con la fase 1"), together with List as default, multi-genre membership, accepted short-film limitation (F-32) and the deferred quick add.

- Room migration 5 → 6 instrumented test with an existing v5 row, and DAO query for unresolved watched movies.
- Sync mapping unit tests for `directors` upload/download, including absent field → unresolved.
- Worker unit tests (WorkManager test utilities, controlled credits responses): success, empty credits, 404, transient failure → retry, cancellation, nothing remaining.
- Credits HTTP integration test with MockWebServer and the real Retrofit/Gson stack.
- Pure grouping function unit tests for each grouping, ordering, multi-membership and fallback groups.
- `WatchedMoviesViewModel` tests through `process(intent)`: mode/grouping changes, persistence calls, defaults on read failure, search in groupings, navigation effect.
- `MovieDetailViewModel` test for the unresolved-watched fallback, and no write when resolved or not watched.
- Plain Compose tests for the toggle, chips, Shelf poster click, director heading click and hidden controls when empty.
- Paparazzi light/dark for List and Shelf in Director and Decade groupings, and Spanish large font for chips. Existing content captures change because of the new controls and fixed columns; review each changed image.
- App journey: Watched → Director heading → person profile → Back with mode and grouping preserved.

Screen revision boundaries (confirmed by the user on 2026-11-01: "si, dale con la fase 5"):

- `WatchedMoviesViewModel` tests through `process(intent)`: no stats dependency or state; total counts distinct movies with multi-membership; total unchanged while searching; `OpenSearch` / `CloseSearch` (close clears the query and restores the full collection); grouping change keeps the query; search closes when the collection becomes empty.
- Plain Compose tests: header shows title and total; search button opens a focused field and hides the toggle; the back action and system Back close search; the clear action keeps search open; only the title shows in Loading, Error and Empty; the director heading is a button and Unknown director is not; no stats content.
- `WatchedMoviesFeatureTest` (instrumented) no longer asserts stats.
- Paparazzi light/dark: Shelf Director, List Date watched, search open with results, no results while searching, Empty, and Spanish large font. Remove the stats dashboard capture.

Release date restoration boundaries (approved with option 1 on 2026-11-01: "vale, opción 1, actualiza la spec y dale"):

- Sync mapping unit tests: `releaseDate` upload and download at the Firestore and repository layers (absent or null → unresolved, empty → resolved without date), and `MovieSyncData` ↔ entity mapping.
- Room instrumented tests: the unresolved query includes watched movies with resolved directors and a `null` release date; the conditional store fills only missing values, never overwrites, and reports no change when nothing is filled.
- HTTP integration test (MockWebServer, real Retrofit/Gson): `movie/{id}?append_to_response=credits` yields directors and release date; missing date → empty; 404 → both empty.
- Worker unit tests: a date-only unresolved movie is resolved and uploaded; existing cases keep passing.
- `MovieDetailViewModel` tests: a remote load of a watched movie stores a missing release date; a Room load triggers no upload.
- Manual: on the emulator, restored movies move from Undated to their decades after the backfill.

## Risks / Trade-offs

- A user with several hundred watched movies triggers the same number of sequential credits requests once; TMDB rate limits are well above this and the worker backs off on 429.
- Genre grouping shows a movie in several groups; group counts therefore do not sum to the total. This matches how people browse by genre.
- Shelf, the existing grid, stays the default so the upgrade changes nothing for current users; List adoption is measured through analytics.

## Migration Plan

Room 5 → 6 is additive (nullable column, no data rewrite). Rollback: a downgrade is not supported by Room; a later version can ignore the column. The backfill is self-terminating and removable as described above.
