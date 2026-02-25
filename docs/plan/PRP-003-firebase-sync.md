# PRP — Firebase Sync (Offline-First)

| Field           | Value                                                              |
|-----------------|--------------------------------------------------------------------|
| **PRP ID**      | PRP-003                                                            |
| **Version**     | 1.0                                                                |
| **Status**      | ✅ Completed                                                        |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §G-05                                   |
| **Feature**     | Cloud sync of Watched / Watchlist via Firestore (Offline-First)   |
| **Date**        | 2026-02-25                                                         |
| **Author**      | @asensiodev                                                        |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 0. Prerequisites (manual — not code)

Before any phase can be executed, the Firebase Console setup must be complete.

- [x] Complete **all steps** in [`docs/guides/GUIDE-firestore-setup.md`](../guides/GUIDE-firestore-setup.md)
- [x] Firestore database is created and in **Production mode**
- [x] Security rules are published
- [x] `firebase-firestore` dependency added to `libs.versions.toml`

---

## 1. Context & Motivation

Santoro stores Watched and Watchlist data exclusively in Room. If the user installs the app on a second device or reinstalls, all data is lost. Firestore as a sync backend fixes this: Room remains the single source of truth for reads (zero latency, works offline), and Firestore is the persistence and sync layer that replicates data across devices and survives reinstalls.

**Strategy: Room-as-source-of-truth + async Firestore sync**
- UI always reads from Room → instant, no network dependency.
- Every user action (toggle watched, add/remove watchlist) writes to Room first, then enqueues a background upload job.
- A sync worker downloads Firestore state on app start and periodically, merges into Room.
- Conflict resolution: `updatedAt` timestamp — last write wins.

---

## 2. Goals

- User's Watched and Watchlist lists survive reinstall and appear on a second device.
- App is fully usable offline — all actions work without a network connection.
- Sync happens silently in the background — no loading spinners, no blocking UI.
- Anonymous users are synced to their anonymous `uid`. When they link a Google account, their data migrates to the new `uid`.

### 2.1 Non-Goals

- Syncing TMDB browse data (popular, trending, search results) — always fetched from API.
- Real-time multi-device sync (Firestore listeners) — WorkManager polling is sufficient for v1.
- Sync conflict UI — last-write-wins is the resolution strategy, no user-facing conflict dialogs.
- Syncing movie metadata (title, poster, etc.) from Firestore — only user state (`isWatched`, `isInWatchlist`, `watchedAt`) is synced.

---

## 3. User Stories

| ID    | As a…    | I want to…                                              | So that…                                              | Acceptance Criteria |
|-------|----------|---------------------------------------------------------|-------------------------------------------------------|---------------------|
| US-01 | app user | have my Watchlist and Watched list on a new device      | I don't lose my data on reinstall                     | After signing in on a new device, lists are populated from Firestore within seconds. |
| US-02 | app user | use the app while offline                               | I can still see and modify my lists without internet  | All toggle/add/remove actions work offline. When network returns, changes sync automatically. |
| US-03 | app user | have my data when I link my Google account              | my anonymous history is not lost                      | After linking Google, all previous watched/watchlist data is preserved. |

---

## 4. Architecture

```
Presentation (ViewModel)
    │ observes Flow<>
    ▼
Room (source of truth)          ← always read from here
    ▲           ▲
    │ writes     │ writes
    │           │
User Action    SyncWorker (WorkManager)
    │               │
    │ enqueues       │ reads/writes
    ▼               ▼
UploadWorker    Firestore
(OneTime)       users/{uid}/movies/{movieId}
```

---

## 5. Data model — Firestore document

Each interacted movie is a document at `users/{uid}/movies/{movieId}`:

```
{
  movieId:       Number,
  title:         String,
  posterPath:    String | null,
  isWatched:     Boolean,
  isInWatchlist: Boolean,
  watchedAt:     Timestamp | null,
  updatedAt:     Timestamp          ← conflict resolution key
}
```

Only fields needed for sync are stored. Full movie metadata (cast, crew, genres) is always fetched from TMDB.

---

## 6. Modules affected / created

| Module | Change |
|---|---|
| `core/sync` (new) | `SyncRepository`, `FirestoreMovieDataSource`, `SyncWorker`, `UploadWorker`, DI module |
| `core/database` | Add `updatedAt: Long` field to `MovieEntity`, new DAO queries for sync |
| `feature/movie-detail/impl` | Enqueue `UploadWorker` after `UpdateMovieStateUseCase` |
| `feature/watchlist/impl` | Enqueue `UploadWorker` after `RemoveFromWatchlistUseCase` |
| `app` | Initialize `SyncWorker` on app start / after login |

---

## 7. Phases & Tasks

### Phase 1 — Database layer: add `updatedAt`

- [x] Add `updatedAt: Long` column to `MovieEntity` (default `System.currentTimeMillis()`)
- [x] Create Room migration: version N → N+1 (`ALTER TABLE movies ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0`)
- [x] Update `MovieDao`: add `getMoviesForSync(): List<MovieEntity>` query
- [x] Update `MovieDao`: add `upsertMovieSyncState(movieId, isWatched, isInWatchlist, watchedAt, updatedAt)` query
- [x] Update `RoomDatabaseRepository` to expose `getMoviesForSync` and `upsertMovieSyncState`
- [x] Update `MovieMapper` to propagate `updatedAt` (set to `System.currentTimeMillis()` on every write via `toEntity()`)

### Phase 2 — `core/sync` module: Firestore data source

- [x] Create `core/sync` module with `build.gradle.kts` (depends on `core/database`, `core/auth`, firebase-firestore)
- [x] Create `MovieSyncEntity` data class — mirrors the Firestore document fields
- [x] Create `FirestoreMovieDataSource` interface:
  - `uploadMovie(uid: String, entity: MovieSyncEntity): Result<Unit>`
  - `downloadUserMovies(uid: String): Result<List<MovieSyncEntity>>`
- [x] Implement `FirestoreMovieDataSourceImpl` using `firebase-firestore-ktx` coroutine extensions (`.await()`)
- [x] Create `SyncRepository` interface:
  - `uploadPendingChanges(uid: String): Result<Unit>`
  - `downloadAndMerge(uid: String): Result<Unit>`
- [x] Implement `DefaultSyncRepository`:
  - `uploadPendingChanges` → reads Room movies with `isWatched || isInWatchlist`, uploads each to Firestore
  - `downloadAndMerge` → downloads all Firestore docs for uid, for each: if Firestore `updatedAt` > Room `updatedAt` → upsert into Room; else keep Room value
- [x] Create `SyncModule` (Hilt) — provides `FirebaseFirestore` instance, binds interfaces to impls

### Phase 3 — WorkManager workers

- [x] Add WorkManager dependency to `libs.versions.toml` and `core/sync/build.gradle.kts`
- [x] Create `UploadWorker` (`CoroutineWorker`):
  - Input: `movieId: Int` (via `Data`)
  - Gets current user `uid` from `AuthRepository`
  - Calls `SyncRepository.uploadPendingChanges(uid)`
  - Returns `Result.success()` on ok, `Result.retry()` on network error
  - Retry policy: exponential backoff, requires network
- [x] Create `SyncWorker` (`CoroutineWorker`):
  - No input data needed
  - Gets `uid` from `AuthRepository`; if null (not logged in) → `Result.success()` (no-op)
  - Calls `SyncRepository.downloadAndMerge(uid)`
  - Returns `Result.success()` on ok, `Result.retry()` on network error
- [x] Create `WorkManagerSyncScheduler`:
  - `schedulePeriodicSync()` → `PeriodicWorkRequest` every 6 hours, requires network, existing periodic work policy: `KEEP`
  - `scheduleImmediateSync()` → `OneTimeWorkRequest`, requires network
  - `enqueueUpload(movieId: Int)` → `OneTimeWorkRequest`, requires network, `APPEND_OR_REPLACE` policy per movieId tag

### Phase 4 — Wire upload on user action

- [x] Inject `WorkManagerSyncScheduler` into `MovieDetailViewModel`
- [x] After `UpdateMovieStateUseCase` succeeds → call `scheduler.enqueueUpload(movieId)` (both toggleWatched and toggleWatchlist)
- [x] Inject `WorkManagerSyncScheduler` into `WatchlistMoviesViewModel`
- [x] After `RemoveFromWatchlistUseCase` → call `scheduler.enqueueUpload(movieId)`
- [x] Update tests for both ViewModels to pass mock scheduler

### Phase 5 — Wire sync on app start / login

- [x] Add `core:sync` to `app/build.gradle.kts`
- [x] Configure `SantoroApplication` to implement `Configuration.Provider` with `HiltWorkerFactory` (disables auto WorkManager init)
- [x] Disable `WorkManagerInitializer` in `AndroidManifest.xml` via `tools:node="remove"`
- [x] Inject `WorkManagerSyncScheduler` into `MainActivityViewModel`
- [x] On auth state transitioning to `Authenticated` → call `schedulePeriodicSync()` + `scheduleImmediateSync()`
- [x] `distinctUntilChangedBy` ensures sync is only triggered once per auth transition, not on every re-emission
- [x] Add `MainActivityViewModelTest` covering: auth→sync triggered, no-auth→no sync, duplicate auth emission→sync once

### Phase 6 — Tests

- [x] Unit: `DefaultSyncRepository.downloadAndMerge` — Firestore newer → Room updated; Room newer → Room unchanged; movie not in Room → upserted
- [x] Unit: `DefaultSyncRepository.uploadPendingChanges` — uploads all, empty list → no-ops, mid-batch failure stops early, correct movieId sent
- [x] Unit: `FirestoreMovieDataSourceImpl` — success path, Firestore exception → failure, missing movieId skipped, empty collection → empty list
- [x] Unit: `UploadWorker` — no uid → success no-op; upload ok → success; network error → retry
- [x] Unit: `SyncWorker` — no uid → success no-op; download+merge ok → success; network error → retry
- [x] Unit: `MovieDetailViewModel` — toggleWatched/toggleWatchlist success → enqueueUpload; failure → no enqueue
- [x] Unit: `WatchlistMoviesViewModel` — onRemoveConfirmed → enqueueUpload; no movie to remove → no enqueue
- [x] Unit: `MainActivityViewModel` — auth→sync triggered once; no auth→no sync; duplicate auth emission→sync once

### Phase 7 — Bug fixes (post-implementation)

- [x] Fix `core/sync/build.gradle.kts`: `libs.hilt.compiler` moved from `implementation` → `ksp` so `@HiltWorker` annotation processing runs and `HiltWorkerFactory` can instantiate workers (was causing `NoSuchMethodException` / FAILED workers in WorkManager)
- [x] Fix `downloadAndMerge` in `DefaultSyncRepository`: movies that exist in Firestore but **not** in Room were silently skipped (`?: return@forEach`). Root cause: `updateMovieSyncState` is a SQL `UPDATE` — it's a no-op if the row doesn't exist.
  - Added `upsertMovieFromSync` query (`INSERT OR REPLACE`) to `MovieDao` with default values for TMDB-only fields (`overview`, `popularity`, etc.)
  - Exposed `upsertMovieFromSync` in `DatabaseRepository` interface and implemented in `RoomDatabaseRepository`
  - `downloadAndMerge` now branches: `localMovie == null` → `upsertMovieFromSync`; `remote.updatedAt > local.updatedAt` → `updateMovieSyncState`

---

## 8. Technical Notes

### Conflict resolution (last-write-wins)
Every Room write sets `updatedAt = System.currentTimeMillis()`. During `downloadAndMerge`:
```
if (firestoreDoc.updatedAt > roomMovie.updatedAt) → upsert Firestore values into Room
else → keep Room values (Room is more recent, upload will handle it)
```

### Anonymous → Google account migration
Firebase Auth handles `uid` continuity when linking: the `uid` does **not** change after linking a Google account to an anonymous session. The Firestore path `users/{uid}/movies/` remains valid. No data migration needed — this is one of the core benefits of Firebase's anonymous account linking.

### WorkManager network constraint
All workers use `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()`. WorkManager will hold the job and execute it automatically when network becomes available.

### Firestore coroutines
Use `firebase-firestore-ktx` `.await()` extensions inside `withContext(Dispatchers.IO)`. No manual callbacks.

### Preventing duplicate uploads
`enqueueUpload` uses a unique work name per `movieId` with `ExistingWorkPolicy.APPEND_OR_REPLACE`. If the user toggles a movie quickly multiple times, only the latest state is uploaded.

---

## 9. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should `SyncWorker` also run on first install before any login? | No — `SyncWorker` is a no-op if `uid` is null. It's triggered post-login. |
| 2 | What if the user has 10,000 movies? Batch Firestore writes needed? | Out of scope for v1. Firestore limit is 500 writes/batch. Flag for follow-up if needed. |
| 3 | Real-time listeners instead of WorkManager polling? | Out of scope for v1. Last-write-wins + periodic sync is sufficient for a personal tracker. |

---

## 10. Out of Scope / Follow-ups

- Real-time Firestore listeners (live multi-device sync without polling)
- Batch write optimization for large lists
- Sync status indicator in UI ("Last synced: 2 min ago")
- Selective sync (user can disable cloud sync from Settings)
- **PRP-004 — Browse cache (G-06):** Room as short-lived cache (TTL ~30 min) for popular/trending/search results, so the user doesn't re-hit the API on every navigation and the app degrades gracefully on slow networks. Independent of Firestore sync.

---

## 11. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-25 | Initial draft |
