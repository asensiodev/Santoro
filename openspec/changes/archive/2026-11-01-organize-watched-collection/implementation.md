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

## Phase 3 — Collection presentation

Decision confirmed by the user on 2026-10-28: the existing poster grid becomes Shelf (default, three fixed columns) and List is the new mode; spec, design, proposal and PRD were updated before implementation.

### 3.1 Grouping

- Red: `:feature:watched-movies:impl:testDebugUnitTest --tests '*WatchedGroupMapperTest*'` failed for every grouping, ordering, multi-membership, unique key and release-year/directors case against an empty stub.
- Green: all watched unit tests passed after implementing `WatchedGroupMapper` and the new `MovieUi` fields; three existing `MovieUiMapperTest` expectations gained the intended `releaseYear`.

### 3.2 Preferences and ViewModel

- Red: `:core:data:test --tests '*DefaultUserPreferencesRepositoryTest*'` failed for saved values and updates against a stub; the defaults case passed as regression coverage. Green: `:core:data:test`.
- Structural refactor under green: `movies: Map` became `groups: List<WatchedGroupUi>` with `viewMode`/`grouping`; watched unit tests passed and `verifyPaparazziDebug` for the module stayed identical. One existing test that never loaded movies was made to load them, matching its name.
- Red: `--tests '*WatchedMoviesViewModelTest*'` failed for saved preferences on first content, regrouping with persistence and analytics, mode change, write failure, search grouping and the once-only collection view event; read failure and empty collection passed as regression coverage.
- Green: `:feature:watched-movies:impl:testDebugUnitTest`.

### 3.3 UI

- Red (instrumented, `emulator-5554`): `WatchedCollectionScreenTest` failed 4 of 6 (toggle, chips, List row, director heading); unknown-director heading and hidden controls passed as regression coverage.
- Green: watched instrumented suite `OK (7 tests)`. `WatchedMoviesFeatureTest` now expects the total text twice (stats and the single group count).

### 3.4 Screenshots

- Recorded `:feature:watched-movies:impl:recordPaparazziDebug --tests '*WatchedMoviesScreenScreenshotTest*'`: changed `content` (controls, count) and new `shelfDirector`, `listDirector`, `listDecade`, `spanishLargeDecade` in both themes. Inspected content dark, Shelf director light, List director dark, List decade light and Spanish 200% light.
- Observation for review: when the selected chip is Decade, the chip row starts scrolled to the beginning, so the selected chip can be off screen.
- Resolved after review: the selected grouping chip is brought into view on entry and on change. Red (`emulator-5554`): `givenDecadeGroupingOnNarrowWidth_whenShown_thenSelectedChipIsVisible` failed with `The component is not displayed!`. A first attempt that requested bring-into-view directly from `LaunchedEffect` still failed because the chip had no layout yet; waiting one frame before the request turned it green. Watched instrumented suite `OK (8 tests)`; `:feature:watched-movies:impl:verifyPaparazziDebug` passed unchanged because screenshots capture the first frame; module `ktlintCheck detekt testDebugUnitTest` passed.

### Phase 3 checks

- `ktlintFormat` on changed modules, then `ktlintCheck detekt :architecture-tests:test verifyPaparazziDebug :app:compileDebugKotlin :app:compileJourneyTestAndroidTestKotlin :core:data:test :feature:watched-movies:impl:testDebugUnitTest :feature:movie-detail:impl:testDebugUnitTest :app:testDebugUnitTest`: passed (all-module screenshots verified).
- Watched instrumented suite after formatting: `OK (7 tests)`.

## Phase 4 — Navigation and validation

### 4.1 Director navigation journey

- The journey Hilt graph replaces `DatabaseModule` and `SyncModule`, so it lacked `SyncStore` and `MovieDirectorsRemoteDataSource` required by `DirectorBackfillWorker` since phase 2 (phase 2 compiled the journey Kotlin sources but not their Hilt aggregation). Added `FakeSyncStore` and an empty directors source to `AppJourneyTestModule`; `:app:hiltJavaCompileDebug` confirmed the production graph was already complete.
- `AppJourneyTestData.watchedMovie` now carries the existing journey director.
- Red: `MainActivityWatchedCollectionJourneyTest` failed (`The component is not displayed!`) because the director heading did nothing.
- Green: after wiring `onDirectorClick` to `navigateToPersonDetail` with the RESUMED guard in `SantoroTabNavGraph` (and removing the empty default), the full journey suite passed on `emulator-5554`: `OK (18 tests)`. Back restores the Watched screen with List and Director still selected.

### 4.2 Validation

- Connected (emulator only): app journeys `OK (18 tests)`, watched `OK (7 tests)`, movie-detail `OK (9 tests)`, watchlist `OK (2 tests)`, core:database `OK (29 tests)`.
- `:architecture-tests:test verifyPaparazziDebug`: passed.
- Aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease` with `-PenableFirebase=false`: passed after shortening one journey test name for ktlint.
- Not run: CI on API 35, the release startup check (needs the signed minified Release APK) and the manual checks in 4.3.

## Phase 5 — Screen revision

The user confirmed the revision test boundaries on 2026-11-01 ("si, dale con la fase 5"). Instrumented tests ran on `emulator-5554` only.

### 5.1 ViewModel

- Stats removal was a refactor without a behavioral red: `WatchedMoviesViewModel` no longer takes `GetWatchedStatsUseCase`, `LoadStats` and `stats` were removed, and the two stats tests were deleted. The existing suite stayed green with the scaffolding (`isSearchOpen`, `totalMovies`, no-op `OpenSearch` / `CloseSearch`).
- Fixture correction: two search tests returned matches that were not in the watched collection (empty collection flow). They now include the matched movie in the collection, because the ViewModel derives `hasMovies` and the total from the collection while searching.
- Red: `:feature:watched-movies:impl:testDebugUnitTest --tests '*WatchedMoviesViewModelTest*'` failed 5 new tests on assertions: total with a co-directed movie (`Expected: <2> but was: <0>`), total while searching (`Expected: <2> but was: <0>`), `OpenSearch` (`Expected: <true> but was: <false>`), `CloseSearch` (`Expected: <> but was: <dune>`) and collection emptied while searching (`Expected: <> but was: <dune>`). Query kept across a grouping change passed immediately (regression coverage).
- Green: same command passed after combining the collection flow with the search flow while a query is active, deriving `hasMovies`, `totalMovies` and the empty-collection close from the collection, and clearing query and search state in `CloseSearch`.

### 5.2 and 5.3 Compose and feature tests

- Red (`WatchedCollectionScreenTest`, `WatchedMoviesFeatureTest`): 8 of 15 failed: header title/total, title-only Loading and Empty, search button, focused field with hidden toggle, close action, system Back (`Expected: <[CloseSearch]> but was: <[]>`), and the feature test's header plus group count. The clear action keeping search open passed immediately (regression coverage through `QueryTextField`).
- Green: watched instrumented suite `OK (15 tests)` after adding `WatchedCollectionHeader` (title, total, search button, view toggle; search mode with back action, focused `QueryTextField` and `BackHandler`), splitting the controls into `WatchedGroupingChips` (Search genre chip style plus selected check) and `WatchedViewModeToggle`, the tonal director heading, and deleting `WatchedStatsDashboard`.

### 5.4 Screenshots

- Deleted `WatchedStatsDashboardScreenshotTest` and its golden.
- Recorded `:feature:watched-movies:impl:recordPaparazziDebug --tests '*WatchedMoviesScreenScreenshotTest*'`: 18 changed and 4 new captures (`listDate`, `searchOpen`) in both themes; `noResults` now shows the search-open state. Inspected all 22.
- Observations for review: in dark theme the selected chip (`primaryContainerDark` on the background) is distinguishable mainly by its check icon, as anticipated in design.md; in the Spanish 200% capture, the selected Decade chip is off screen in the first frame, which the instrumented narrow-width test covers.

### 5.5 PRD

- Version 2.21: Watched screen description, F-20 status, F-31 behaviour and a new planned F-33 Watched statistics screen.

### 5.6 Validation

- `ktlintFormat` on `:feature:watched-movies:impl` and `:core:design-system`, then `ktlintCheck detekt :feature:watched-movies:impl:testDebugUnitTest :feature:watched-movies:impl:verifyPaparazziDebug :architecture-tests:test`: passed.
- Aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` with `-PenableFirebase=false`: passed.
- Connected (emulator only): app journeys `OK (18 tests)`, watched `OK (15 tests)`, movie-detail `OK (9 tests)`, watchlist `OK (2 tests)`, core:database `OK (29 tests)`. The first install of three test APKs failed with `INSTALL_FAILED_INSUFFICIENT_STORAGE`; after uninstalling the old test packages, the fresh APKs installed and passed.
- Not run: CI on API 35, the release startup check and the manual checks in 4.3.

### 4.3 finding: bottom spacing

- Manual check (user, emulator): at the end of a scrolled collection, the last poster or row touched the bottom navigation bar in both modes.
- Fix: `WatchedMovieList` adds `contentPadding = PaddingValues(bottom = Spacings.spacing16)`, matching See all movies. No automated test: Paparazzi captures only the first frame, and the scroll-end gap is a layout detail with low regression risk. Verified on `emulator-5554` by scrolling to the end in Shelf and List with Director grouping. `:app:assembleDebug :feature:watched-movies:impl:verifyPaparazziDebug ktlintCheck detekt` (module): passed, screenshots unchanged.

## Phase 6 — Release date restoration

Found in manual checks: restored movies had `releaseDate = null` because Firestore never carried it. The user chose to fix it within F-31 on 2026-11-01 ("vale, opción 1, actualiza la spec y dale").

### 6.1 Sync field

- Scaffolding: nullable `releaseDate` on `MovieSyncEntity` and `MovieSyncData` with no mapping.
- Red: `:core:sync:testDebugUnitTest` (Firestore upload field, download tri-state, repository upload and merge) and `:core:database:testDebugUnitTest --tests '*MovieMapperTest*'` (entity ↔ sync data) failed 6 new tests on assertions, for example `Expected: <[2021-04-07, , null]> but was: <[null, null, null]>`.
- Green: after the `releaseDate` Firestore field and mappings, both modules passed. One existing full-mapping expectation in `MovieMapperTest` gained `releaseDate = "2026-01-01"`, the intended behaviour change.

### 6.2 Room

- Refactor without behaviour change: `storeResolvedDirectors` became `storeResolvedMetadata(movieId, directors?, releaseDate?)` and `getWatchedMovieIdsWithUnresolvedDirectors` became `getWatchedMovieIdsWithUnresolvedMetadata` across domain, Room, worker, detail ViewModel, test fakes and tests; all affected modules compiled and their unit tests passed.
- Red (`emulator-5554`, `RoomDatabaseRepositoryIntegrationTest`): 2 of 11 failed: date-only unresolved query (`Expected: <[1]> but was: <[]>`) and filling a missing date (`Expected: <true> but was: <false>`). "Nothing missing reports no change" passed (regression coverage).
- Green: after the `(directors IS NULL OR releaseDate IS NULL)` query and the `COALESCE` conditional update, core:database suite `OK (32 tests)`.

### 6.3 HTTP

- Scaffolding: `MovieMetadataRemoteDataSource` / `TmdbMovieMetadataDataSource` / `MovieMetadataApiService` renames, still on the credits endpoint, with `RemoteMovieMetadata`.
- Red: `MovieMetadataHttpTest` failed 3 of 5 on assertions (co-directors and date, empty values, null date); 404 and 500 passed (regression coverage).
- Green: `GET movie/{id}?append_to_response=credits` returning `MovieApiModel`; path asserted as `/3/movie/275?append_to_response=credits`. `:core:sync:testDebugUnitTest` passed.

### 6.4 Worker

- Red: `DirectorBackfillWorkerTest` date-only movie failed verifying `storeResolvedMetadata(275, [Joel Coen, Ethan Coen], 1996-03-08)`.
- Green: the worker passes the fetched release date; `:core:sync:testDebugUnitTest` passed.

### 6.5 Detail fallback

- Red: `MovieDetailViewModelTest` failed 2 new tests verifying the stored release date (remote load; Room emission without date followed by the remote one).
- Green: the fallback passes `movie.releaseDate` and records the request as handled only when a row changed, so a no-op Room emission no longer blocks the remote one. `:feature:movie-detail:impl:testDebugUnitTest` passed.

### 6.6 Validation

- `ktlintFormat` on changed modules, then the aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` with `-PenableFirebase=false`: passed.
- Manual (`emulator-5554`, restored account): before, Decade showed 3 of 5 movies under Undated; after installing this build, the backfill resolved them, a sync uploaded them, a second backfill found nothing, and Decade showed all five in the 2020s with years in List rows.
- Connected (emulator only, installed one suite at a time because of emulator storage): app journeys `OK (18 tests)`, watched `OK (15 tests)`, movie-detail `OK (9 tests)`, watchlist `OK (2 tests)`, core:database `OK (32 tests)`.
- Not run: CI on API 35, the release startup check, and Firestore inspection of the uploaded `releaseDate` field (inferred from the sync run and unit tests).

## Phase 7 — Offline posters and detail

Requested by the user on 2026-11-01 ("mete los dos ahora mejor") after manual offline checks.

### 7.1 Watched posters

- Red (`emulator-5554`): `WatchedPosterFallbackTest`, with a singleton `ImageLoader` whose interceptor returns an error, failed with `The component is not displayed!` for the Shelf title after the load failed.
- Green: `MovieCard` and the List thumbnail draw the placeholder underneath the image (cast portrait pattern); watched instrumented suite `OK (16 tests)`. Watched Paparazzi captures were unchanged (they use null poster paths).

### 7.2 Movie detail without remote data

- Red: `MovieDetailWithoutRemoteDataTest` failed `assertDoesNotExist` for the Cast heading with an empty cast.
- Green: Cast hidden when empty, backdrop without error image over a `surfaceVariant` base, poster placeholder icon centered under the image; movie-detail instrumented suite `OK (10 tests)`.
- Recorded `:feature:movie-detail:impl:recordPaparazziDebug --tests '*MovieDetailScreenScreenshotTest*'`: only `content` light/dark changed (neutral header, centered poster placeholder, no Cast heading); inspected both.

### 7.3 Validation

- `ktlintFormat` on watched and movie-detail, then the aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` with `-PenableFirebase=false`: passed.
- Connected (emulator only): app journeys `OK (18 tests)`, watched `OK (16 tests)`, movie-detail `OK (10 tests)`.
- Manual (`emulator-5554`, app started online, then airplane mode, image cache previously trimmed): Shelf cells show the placeholder with each title.
- Found while testing, outside F-31 and present in release 1.1.1: a cold start without network crashes the app. `ApiKeyStartupInitializer` launches `ApiKeyRefresher.ensureKeyUpToDate()` without a handler, and `FirebaseRemoteConfigProvider.initialize()` rethrows `FirebaseRemoteConfigClientException` (`UnknownHostException`) when the cached config is older than the one-hour fetch interval. Reported to the user; not fixed in this change.
- Real-world mixed-version check: the user marked five movies as watched on a phone running release 1.1.1 (no directors or release dates in Firestore). After the emulator synced them, they appeared with directors and years in Director and Decade groupings, resolved by the backfill without user action.

## Phase 8 — Offline cold start crash

Outside the watched collection capability; included in this release at the user's request.

- Red: `ApiKeyRefresherTest` "remote config fetch fails offline" failed with the thrown `IOException` propagating out of `ensureKeyUpToDate`, reproducing the crash path; the cancellation test passed (regression coverage).
- Green: `ensureKeyUpToDate` returns without changing the local key when `RemoteConfigProvider.initialize()` fails and rethrows `CancellationException`. `:core:network:testDebugUnitTest :core:network:ktlintCheck :core:network:detekt :app:assembleDebug`: passed.
- Manual (`emulator-5554`): with the Remote Config fetch metadata removed to force a fetch, a cold start in airplane mode showed Search with saved results and no entry in the crash buffer; the same scenario crashed before the fix with `FirebaseRemoteConfigClientException`. Movie detail opened offline showed the phase 7 header, poster placeholder and no Cast heading.
- Aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` with `-PenableFirebase=false`: passed.

## 4.3 Manual checks (agent-run on `emulator-5554`, 2026-11-01)

- Sign out and sign in with the same account: all 10 watched movies returned with directors; Shelf mode and Director grouping were kept; Decade showed only decade groups and no Undated group; no TMDB detail request was made after sign-in, so release dates came from Firestore.
- Spanish (per-app locale) and dark theme: header, total, chips, decade labels, search placeholder and bottom bar translated. Search filtered "padrino" within Decade grouping; the first Back closed the keyboard, the second closed search and stayed on Watched.
- Font scale 2.0: header and toggle fit, selected chip visible, List rows wrap, the search placeholder ellipsizes.
- Accessibility tree (`uiautomator`, what TalkBack reads): view toggle exposed as radio buttons with labels and checked state; grouping chips checkable with checked state; List rows merge title and "year · director"; director headings are clickable with name and count; Shelf posters expose the title.
- Findings for review: (1) in dark theme the selected chip (`primaryContainer`) looks dimmer than unselected chips; (2) closing search keeps the scroll position of the filtered list instead of returning to the top.
- Not covered: spoken TalkBack output (semantics verified instead) and the upgrade from Play 1.1.1 (accepted gap).

## Phase 9 — Manual-check adjustments

Requested by the user on 2026-11-01 ("si, haz los dos ajustes").

- 9.1 Red: `WatchedMoviesViewModelTest` "CloseSearch shows the full collection before the query debounce" failed with `Expected: <[1, 2]> but was: <[1]>`. Green after `CloseSearch` regroups the last full collection; the ViewModel suite passed.
- 9.2 Red (`emulator-5554`): `WatchedCollectionScreenTest#givenScrolledSearchResults_whenSearchCloses_thenGridReturnsToTheTop` failed with `The component is not displayed!`. Green after the screen hoists the grid state and scrolls to the top only on the open → closed transition; watched instrumented suite `OK (17 tests)`.
- 9.3 Selected grouping chips use `secondaryContainer` / `onSecondaryContainer`. Recorded `:feature:watched-movies:impl:recordPaparazziDebug --tests '*WatchedMoviesScreenScreenshotTest*'` and inspected all 16 captures with chips (both themes): the selected chip is distinct and matches the view toggle; loading, empty and error captures are unchanged.
- Validation: aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` with `-PenableFirebase=false`: passed; app journeys `OK (18 tests)` (director navigation keeps the scroll position with the hoisted grid state).
- Manual (`emulator-5554`, dark theme): searching "padrino" in Decade and closing search returned to the top of the full collection; the selected Decade chip is clearly distinguishable.

## Post-archive PR audit fixes (2026-11-01)

A high-effort review of PR #4 reported ten findings; two were fixed before release at the user's request, the rest are recorded below.

- Release obfuscation of synced directors (confirmed): R8 renamed `Director` fields to `a`/`b` in the release mapping, so `DefaultSyncRepository` would have uploaded `[{"a":…,"b":…}]`. Directors now serialize through `SyncDirector` (`@SerializedName("id")`, `@SerializedName("name")`) in the already-kept `core.sync.data.model` package; the release mapping keeps `SyncDirector` and its fields unrenamed. A JVM test pinning `[{"id":1223,"name":"Joel Coen"}]` passed before and after the change (regression coverage; the JVM does not reproduce R8). No released build had uploaded directors, so no data migration is needed.
- Stale search emission after closing search: red `WatchedMoviesViewModelTest` "closed search … old search emits before the debounce" (`Expected: <[1, 2]> but was: <[1]>`); green after ignoring results whose query is no longer the active one.
- Validation: aggregate gate `test detekt ktlintCheck koverVerify assembleDebug verifyPaparazziDebug :architecture-tests:test` plus `:app:bundleRelease :app:assembleRelease :app:lintVitalRelease` with real release Firebase: passed. Release startup check on `emulator-5554`: PASS. AAB 1.2.0 (45) SHA-256 `640f23f41540d038f48ddb829f4223e22761f4778e4e84e22f9fae709676f48f`.
- Recorded, not fixed: metadata fills bump `updatedAt` and can win last-write-wins over a newer change from another device (narrow window; a field-level Firestore merge is the proper fix); the backfill stops at the first per-movie failure; the backfill schedules a full snapshot sync; the download merge does not fill unresolved local metadata; duplicated movie detail Retrofit service between sync and movie-detail; preference error handling catches only `IOException`; fixed three Shelf columns on wide windows (product decision).
- Found during the audit, pre-existing in production: synced genres are serialized from the domain `Genre` class, whose fields R8 also renames to `a`/`b`, so Firestore documents uploaded by Play builds hold obfuscated genre JSON. Fixing it requires reading both formats; tracked as a separate change.
