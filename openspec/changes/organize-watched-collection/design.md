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

Add `WatchedViewMode` (List, Shelf) and `WatchedGrouping` (DateWatched, Director, Genre, Decade) to the watched feature. A pure grouping function in the presentation mapper produces ordered `WatchedGroupUi(key, label, count, directorId?, movies)` from domain movies; lazy item keys combine group key and movie ID so multi-membership stays unique. Labels for Unknown director, No genre and Undated use string resources; decade labels use a localized resource. The screen uses `LazyVerticalGrid` with full-span sticky headers in Shelf and the existing `LazyColumn` with sticky headers in List; group headers use stable keys and content types. Mode and grouping are durable state in `WatchedMoviesUiState`; change intents update state immediately, write DataStore through a `WatchedPreferencesDataSource`, and send analytics (`watched_view_mode_changed`, `watched_grouping_changed`).

Preferences live in DataStore, which sign-out does not clear (F-28 clears only movie rows), so a choice survives app restarts and sign out/in on the same device. A different account on the same device inherits the view preference; this is a display setting, not account data, so it is accepted. Change events alone would under-count returning users whose choice is already saved, so the ViewModel also records `watched_collection_viewed` with `mode` and `grouping` once per screen entry with populated content. This gives the real distribution of views in use through the existing `ObservabilityTracker.trackAction`, without a new user-property API.

Preferences are collected once with the movies flow (combine), keeping a single long-lived collector and latest-wins behavior for queries. A DataStore read failure falls back to defaults; a write failure keeps the in-memory choice for the session.

Director heading clicks emit a navigation effect collected in `RESUMED`, navigating to `PersonDetailRoute(personId)` through the app graph.

### Data sources and side effects

- Allowed: Room reads/writes for directors, DataStore preferences, TMDB `movie/{id}/credits`, the existing upload scheduling, analytics events listed above, navigation to person detail.
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
- Paparazzi light/dark for List and Shelf in Director and Decade groupings, and Spanish large font for chips.
- App journey: Watched → Director heading → person profile → Back with mode and grouping preserved.

## Risks / Trade-offs

- A user with several hundred watched movies triggers the same number of sequential credits requests once; TMDB rate limits are well above this and the worker backs off on 429.
- Genre grouping shows a movie in several groups; group counts therefore do not sum to the total. This matches how people browse by genre.
- Changing the default view to Shelf could improve discovery but would surprise existing users; List stays the default and the choice is measured through analytics.

## Migration Plan

Room 5 → 6 is additive (nullable column, no data rewrite). Rollback: a downgrade is not supported by Room; a later version can ignore the column. The backfill is self-terminating and removable as described above.
