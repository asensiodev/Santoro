# Implementation Evidence

Gradle commands ran through the managed `gradle-run` wrapper with JDK 21 and `-PenableFirebase=false`. Instrumented tests ran on the `Pixel_9a` emulator (`emulator-5554`) only, by installing the test APK and running `am instrument`. Repeated "Daemon compilation failed" lines are the Kotlin daemon falling back to in-process compilation, not test or compiler failures.

## Phase 1 — Director data

### 1.1 Room 5 → 6 and domain directors

Scaffolding without behavior: `Director` domain model, nullable `Movie.directors`, nullable `MovieEntity.directors`, database version 6 with an empty `MIGRATION_5_6`.

- Red (JVM): `:core:database:testDebugUnitTest --tests '*MovieMapperTest*'` failed for resolved-empty and co-director round trips with `Expected: <[]> but was: <null>`; the unresolved case passed.
- Green (JVM): same command passed after mapping `directors` as JSON in `MovieMapper`.
- Red (instrumented): `SantoroRoomDatabaseMigrationTest#givenVersion5_whenMigratedTo6_thenDataPreservedAndDirectorsUnresolved` failed with `IllegalStateException: Migration didn't properly handle: movies`.
- Green (instrumented): after `ALTER TABLE movies ADD COLUMN directors TEXT`, the full core:database instrumented suite passed: `OK (26 tests)`.
- Schema `6.json` exported; `5.json` unchanged.

### 1.2 Store directors on save

Directors are resolved at the API boundary: with `credits`, a movie gets a resolved list (possibly empty); without `credits` (list/search responses), directors stay unresolved. Offline detail loads come from Room and keep their stored value, so saving them never marks a movie as resolved without a director.

- Red: `:core:data:test --tests '*MovieApiMapperTest*'` failed for co-directors, credits without a director and credits without crew; the no-credits case passed.
- Green: `:core:data:test` passed.
- Red: `:feature:movie-detail:impl:testDebugUnitTest --tests '*MovieUiMapperTest*'` failed for resolved and resolved-empty round trips through `MovieUi`; the unresolved case passed.
- Green: `:feature:movie-detail:impl:testDebugUnitTest` passed.

### 1.3 Firestore field

- Red: `:core:sync:testDebugUnitTest --tests '*FirestoreMovieDataSourceTest*' --tests '*DefaultSyncRepositoryTest*'` failed for upload/download with directors at the Firestore and repository layers; the absent/unresolved/malformed cases passed as regression coverage.
- Red: `:core:database:testDebugUnitTest --tests '*MovieMapperTest*'` failed for malformed stored directors (read as resolved-empty instead of unresolved) and for `toSyncData`/sync `toEntity` directors.
- Green: `:core:sync:testDebugUnitTest :core:database:testDebugUnitTest` passed.
- Existing Firestore download fixtures gained a `getString("directors")` stub returning `null` (absent field).

### 1.4 Older-client uploads

Verified by reading release 1.1.1 (`0bbe6eb`) `FirestoreMovieDataSource`: uploads use `transaction.set(document, map)` without merge, so an older client removes `directors`. Accepted limitation recorded in design.md.

### Phase 1 checks

- `ktlintFormat` on the five changed modules, then `ktlintCheck detekt :architecture-tests:test :app:compileDebugKotlin`: passed.
- `:core:data:test :core:database:testDebugUnitTest :core:sync:testDebugUnitTest :feature:movie-detail:impl:testDebugUnitTest`: passed.

## Phase 2 — Removable backfill

### 2.1 Credits request

`MovieCreditsApiService` (`movie/{movie_id}/credits`) and `TmdbMovieDirectorsDataSource` in `core/sync`, reusing `CreditsApiModel` and the now-public `CreditsApiModel.toDirectors()` from `core/data`.

- Red: `:core:sync:testDebugUnitTest --tests '*MovieDirectorsHttpTest*'` failed for co-directors (and request path) and for the 500 response against a stub returning an empty list; the no-director and 404 cases passed against the stub.
- Green: same command passed with MockWebServer and the real Retrofit/Gson stack.

### 2.2 Worker and conditional store

- Red: `:core:sync:testDebugUnitTest --tests '*DirectorBackfillWorkerTest*'` failed for all five cases (success with sync and event, nothing remaining, mid-run failure, read failure, cancellation) against an empty worker.
- Green: same command passed.
- Red (instrumented): `RoomDatabaseRepositoryIntegrationTest` failed for the unresolved-watched query (`Expected <[1, 4]> but was <[]>`) and for storing directors (`but was <null>`) against stubs; the keep-saved-directors case passed against the stub.
- Green (instrumented): core:database suite `OK (29 tests)`.
- Refactor under green: the conditional store moved from `SyncStore` to `MovieMutationRepository` and returns whether a row changed, so detail can reuse it; sync tests, the database suite (`OK (29 tests)`) and all affected instrumented sources compiled and passed.

### 2.3 Enqueue

- Red: `:core:sync:testDebugUnitTest` failed only for KEEP/network/backoff scheduling and for enqueueing after a successful download.
- Green: `:core:sync:testDebugUnitTest :app:kspDebugKotlin :app:compileDebugKotlin` passed, including the Hilt graph with the new worker and data source.

### 2.4 Detail fallback

- Red: `:feature:movie-detail:impl:testDebugUnitTest --tests '*MovieDetailViewModelTest*'` failed for storing and uploading, storing once per load, and storing remote directors after an offline emission; the negative cases passed as regression coverage.
- Green: `:feature:movie-detail:impl:testDebugUnitTest` passed.

### Phase 2 checks

- `ktlintFormat` on changed modules (two test names and long instrumented-test lines shortened by hand), then `ktlintCheck detekt :architecture-tests:test :app:compileDebugKotlin :app:compileJourneyTestAndroidTestKotlin :core:data:test :core:database:testDebugUnitTest :core:sync:testDebugUnitTest :feature:movie-detail:impl:testDebugUnitTest :app:testDebugUnitTest`: passed.
- core:database instrumented suite after formatting: `OK (29 tests)`.
- Not yet run: connected tests for movie-detail, watchlist and app journeys (their fakes changed only by a no-op method), Paparazzi and the aggregate gate; these are tracked in task 4.2.
