# Implementation Evidence

Gradle commands ran through the managed `gradle-run` wrapper with JDK 21 and `-PenableFirebase=false`. Repeated "Daemon compilation failed" lines are the Kotlin daemon falling back to in-process compilation, not test or compiler failures.

## Phase 1 — Data and filter

### 1.1 Person credit fields

- Scaffolding without behaviour: `PersonMovieCredit.isVideo` (default `false`) and `genreIds` (default empty), and nullable `video` / `genre_ids` on `PersonCreditApiModel`.
- Red: `:feature:person-detail:impl:testDebugUnitTest` failed only the new `PersonRepositoryHttpTest` case, `Expected: <[(43, false, [18, 80]), (42, true, [10402, 99])]> but was: <[(43, false, []), (42, false, [])]>`.
- Green: `PersonApiMapper` maps `isVideo` (any grouped credit flagged) and the distinct union of `genreIds`; the module suite passed.
- Finding, pre-existing: running `PersonRepositoryHttpTest` alone with `--tests` fails every case with `NoClassDefFoundError: okhttp3.internal.platform.Platform` (`UnsatisfiedLinkError` on `android.util.Log.isLoggable`), also on the unchanged branch; the full module task passes. OkHttp's platform detection depends on test execution order. Evidence in this change uses the full module task.

### 1.2 Feature-film filter

- Scaffolding: `DirectorFilm` and `directedFeatureFilms(crewCredits, watchedByDirector, today)` returning an empty list.
- Red: the 5 new `DirectedFeatureFilmsTest` cases failed on assertions against the empty result (director job, video, documentary, future and undated exclusions; watched documentary and watched film missing from credits kept; one entry per watched film; ordering by date then title with undated last; release today included).
- Green: `directedFeatureFilms` keeps released (on or before today) credits with the Director role that are neither video nor documentary (genre 99), adds every watched film by the director, deduplicates by ID with the watched entry winning, and orders by release date ascending, undated last, then title. `:feature:watched-movies:impl:testDebugUnitTest` passed.

## Phase 2 — Screen state

### 2.2 Show pending preference

- Scaffolding: `UserPreferencesRepository.directorShowPending` and `setDirectorShowPending`, with a stub that always emitted `true` and did not write; the three test fakes gained the member.
- Red: `:core:data:test` failed the saved-false case (`Expected: <false> but was: <true>`) and the write case (`writeString(director_show_pending, false)` was not called). The missing-or-invalid case already passed against the stub's `true` and stays as a guardrail for the default.
- Green: `DefaultUserPreferencesRepository` reads `director_show_pending` with strict boolean parsing (missing or invalid means on) and writes it on change; `:core:data:test` passed.

### 2.1 Director collection ViewModel

- Scaffolding: `DirectorCollectionIntent`, `DirectorCollectionUiState` (visible films and rounded progress percentage derived from the state), a `DirectorCollectionViewModel` whose `process` did nothing, and a `java.time.Clock` binding in `WatchedMoviesModule` so release checks use an injectable today.
- Red: 7 of 9 new `DirectorCollectionViewModelTest` cases failed on assertions: local-first state, merged success state (1 of 3, 33%), failure then retry, latest-wins retry over a slow stale request, watched change without a new request, saved hidden pending, and a failed preference write. The cancellation case (no error state) and the failed preference read (pending shown) passed against the stub's defaults and stay as guardrails.
- Green: the ViewModel initializes once, collects the local watched flow filtered by director ID, loads the filmography in a replaceable job (retry cancels the previous one), rethrows `CancellationException`, recomputes films and counts from the latest watched list without refetching, reads Show pending once (read failure keeps on) and persists changes (write failure keeps the in-memory choice). `:feature:watched-movies:impl:testDebugUnitTest` passed.
- Accepted limitation: a failed local watched read keeps the last known watched films instead of showing an error; Room reads of an existing table are not a realistic failure path.
- Gate: `ktlintCheck detekt :architecture-tests:test :core:data:test :feature:watched-movies:impl:testDebugUnitTest :feature:watched-movies:impl:compileDebugAndroidTestKotlin :feature:movie-detail:impl:compileDebugAndroidTestKotlin :app:compileJourneyTestAndroidTestKotlin :app:assembleDebug` passed.

## Phase 3 — UI

Instrumented Compose tests ran on `emulator-5554` (API 36 arm64 emulator) by installing the module's androidTest APK and running `am instrument`; the APK was uninstalled afterwards to free emulator storage.

### 3.1 Screen

- Scaffolding: English and Spanish strings, an empty `DirectorCollectionScreen` and an empty `DirectorFilmCard`.
- Red: all 8 new `DirectorCollectionScreenTest` cases and the new `WatchedPosterFallbackTest` case for an unwatched director film failed: missing nodes for the summary and percentage, watched/not-watched state descriptions, film click, director name and Back, Show pending switch, hidden pending films, loading indicator and inline error with Retry; the poster case timed out waiting for the image request. The existing shelf poster fallback case kept passing.
- Green: `DirectorCollectionRoute` initializes the ViewModel once with `LaunchedEffect(viewModel)`. `DirectorCollectionScreen` shows a top app bar with Back and the director name as a button that opens the profile; a summary with "Watched X of Y", percentage and progress bar (or "X watched" without a total, with a loading indicator while the filmography loads); an inline error with Retry; a Show pending switch row; and a three-column poster grid with stable keys, content types and bottom padding. `DirectorFilmCard` keeps F-31's placeholder under the image, dims and desaturates unwatched posters, marks watched ones, and exposes Watched or Not watched as the state description. `MovieCard.PlaceholderContent` and `BASE_POSTER_URL` became internal for reuse. All 10 selected cases passed, and the full module suite passed (26 tests).

### 3.2 Screenshots

- Recorded `DirectorCollectionScreenScreenshotTest` only (`:feature:watched-movies:impl:recordPaparazziDebug --tests '*DirectorCollectionScreenScreenshotTest*'`): partial, pending hidden, loading, error and Spanish 2x font, each in light and dark (10 new images, no other golden changed).
- Inspection: watched films at full emphasis with the check mark, unwatched films dimmed without a mark, summary 5 of 9 with 56%, the switch off keeps the summary, error banner readable in both themes. The loading spinner is captured at its initial frame. In Spanish at 2x font, long placeholder titles break mid-word as in F-31's cards; with real posters the title is covered.
- Verification: `verifyPaparazziDebug ktlintCheck detekt :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :app:assembleDebug` passed.

### 3.3 Header photo (revision 2026-11-05)

- Plan: design, spec (new "Director photo unavailable" scenario) and tasks updated; `openspec validate director-watched-filmography --strict` passed.
- ViewModel scaffolding: `DirectorCollectionUiState.profilePath`. Red: `:feature:watched-movies:impl:testDebugUnitTest` failed the photo-loaded case, the updated local-first state and the "retry does not refetch the profile" case (`Expected: </nolan.jpg> but was: <null>`); the failed-profile case passed against the stub and stays as a guardrail. Green: the ViewModel requests `PersonRepository.getPerson` once per initialization in its own coroutine, stores `profilePath` on success, ignores failures and rethrows `CancellationException`; the filmography retry does not touch it.
- Compose scaffolding: `DIRECTOR_PHOTO_TAG`. Red: `givenDirectorPhoto_whenShown_thenShowsItNextToTheName` failed (node not displayed); the no-photo case passed as a guardrail. Green: the header shows a 32dp circular `AsyncImage` before the name inside the same profile button and hides it when the image fails to load.
- Environment finding: with a photo, the real Coil loader attempted a network request and the test process crashed (`SecurityException: missing INTERNET permission`). `DirectorCollectionScreenTest` now installs a controlled `ImageLoader` (pending or failing) like `WatchedPosterFallbackTest`; the failed-photo case was added after the implementation and is regression coverage.
- The first gate failed all 10 director goldens: the row spacing moved the chevron 8dp away from the name. The spacing now applies only to the photo; goldens unchanged (screenshots use no photo, as Paparazzi cannot load remote images).
- Validation: the module's instrumented suite passed (29 tests on `emulator-5554`); `verifyPaparazziDebug ktlintCheck detekt :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :app:assembleDebug` passed.

## Phase 4 — Navigation and validation

### 4.1 Route and journey

- Test fixture: `FakePersonRepository.crewMovies` (reset per test) so the director can have crew credits.
- Red: `MainActivityWatchedCollectionJourneyTest` rewritten for Watched → Director grouping heading → Director collection → unwatched film detail → Back → director profile → Back → Back. Running it on `emulator-5554` (journey APKs installed with `adb install`, `am instrument`) failed with `ComposeTimeoutException` waiting for "Watched 1 of 2", because the heading still opened the person profile.
- Green: `DirectorCollectionRoute(personId, name)` in `feature/watched-movies/api`; `navigateToDirectorCollection` and `directorCollectionRoute` in watched-movies impl navigation; the Watched heading callback now carries the director name (`onDirectorClick: (Int, String)`), `SantoroTabNavGraph` opens the Director collection, and `MainActivity` registers the route with Back, movie detail and person profile callbacks guarded by the resumed lifecycle. The heading's accessibility label now reads "Open director collection" / "Ver colección del director". The journey passed, including a single credits request across the detail round trip; all 18 app journeys and the module's 29 instrumented tests passed.
- Gate: `verifyPaparazziDebug ktlintCheck detekt :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :feature:watched-movies:impl:compileDebugAndroidTestKotlin :app:assembleDebug` passed.
- Demo on `emulator-5554` with a guest session (the debug app's local data was cleared first so no real account was used): Christopher Nolan shows his photo, "Watched 4 of 16", 25% and real posters, with unwatched films dimmed and grey. Observation: his three early shorts (Doodlebug, Larceny, Tarantella) appear as unwatched films and count towards the 16, which is the accepted short-film limitation.

## Phase 5 — Short films (revision 2026-11-05)

Plan updated first (proposal, design "Short films", spec scenarios "Early shorts" and "Runtime unknown", tasks 5.1–5.4); `openspec validate director-watched-filmography --strict` passed.

### 5.1 Runtime repository

- Scaffolding: `MovieRuntimeRepository` in `core/domain`, `MovieRuntimeApiService` (`movie/{id}`, `id` and `runtime`), a `TmdbMovieRuntimeRepository` returning an empty map, and its Hilt binding in `PersonDetailModule`.
- Red: both `MovieRuntimeRepositoryHttpTest` cases failed on assertions (`Expected: <Pairs: []> but was: <Pairs: [(1, 30), (2, 113)]>` for known runtimes with zero, null and HTTP 500 left out, and for a second call that serves cached runtimes and retries only the failed lookup, 3 requests in total).
- Green: up to 6 parallel requests (`Semaphore`), positive runtimes cached in memory for the process, failures returned as unknown and not cached, `CancellationException` rethrown. `:feature:person-detail:impl:testDebugUnitTest` passed.

### 5.2 Short classification

- Scaffolding: `DirectorFilmography(features, shorts)` and `splitShorts(runtimes)` returning everything as features.
- Red: both `ShortFilmsTest` cases failed on assertions (40 and 9 minutes are shorts, 41 minutes and unknown runtime stay features; a watched short stays watched among shorts).
- Green: `splitShorts` partitions by a known runtime of 40 minutes or less, preserving order. The file is `DirectorFilmography.kt` (Detekt `MatchingDeclarationName`).

### 5.3 ViewModel

- Scaffolding: `DirectorCollectionUiState.shorts`, `visibleShorts`, `shortsWatchedCount`, and `MovieRuntimeRepository` injected without use.
- Red: 4 new `DirectorCollectionViewModelTest` cases failed on assertions: the short counted among feature films (`[5, 1, 2, 3]`), no runtime lookup for films without a stored runtime, `CONTENT` while runtimes were still loading, and a locally stored watched short not split out.
- Green: after the credits succeed, the filmography job requests runtimes only for candidates without a positive stored `Movie.runtime`, then publishes features and shorts together; stored runtimes classify local watched films immediately. The summary counts feature films only. `:feature:watched-movies:impl:testDebugUnitTest` passed.

### 5.4 Shorts section

- Red: `givenShorts_whenShown_thenListsThemApartWithTheirOwnCount` failed (no Doodlebug node in the grid); the hidden-section case passed as a guardrail.
- Green: below the feature grid, a Shorts heading (accessibility heading) with "X of Y watched" and the same tiles with their own keys; hidden when it has no visible films; the empty message only shows when both lists are empty. English "Shorts" / "X of Y watched", Spanish "Cortometrajes" / "X de Y vistos". The module's instrumented suite passed (31 tests on `emulator-5554`).
- Journey DI: `AppJourneyTestModule` binds a `FakeMovieRuntimeRepository` (no runtimes, so every film stays a feature film); all 18 app journeys passed.
- Screenshots: recorded `DirectorCollectionScreenScreenshotTest` with the new `shorts` scenario in light and dark (2 new images, no other golden changed) and inspected them.
- Gate: `verifyPaparazziDebug ktlintCheck detekt :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :feature:person-detail:impl:testDebugUnitTest :app:assembleDebug` passed.
- Demo on `emulator-5554` (guest session, real TMDB data): Christopher Nolan reads "Watched 4 of 13", 31%, and a Shorts section lists Tarantella, Larceny and Doodlebug with 0 of 3 watched.

## Phase 6 — Consistent status marks (revision 2026-11-05)

Plan updated first (design status-marks revision, spec scenario "Pending film in the Watchlist", tasks 6.1–6.3); `openspec validate director-watched-filmography --strict` passed.

### 6.1 Shared ribbon

- Refactor with existing coverage: `MovieStatusRibbon` moved from search-movies to `core/design-system/component/statusribbon` with `MovieRibbonStatus` (own file for Detekt `MatchingDeclarationName`) and an `announceStatus` flag; Search maps `MovieLibraryStatus` through `toRibbonStatus()`. `:feature:search-movies:impl:verifyPaparazziDebug`, `:feature:search-movies:impl:testDebugUnitTest` and the search instrumented suite (3 tests, including `MovieCardStatusSemanticsTest`) passed unchanged.

### 6.2 Watchlist membership

- Scaffolding: `DirectorFilm.isInWatchlist` (default false) and `GetWatchlistMovieIdsUseCase` (local watchlist IDs) injected without use.
- Red: both new `DirectorCollectionViewModelTest` cases failed on assertions (unwatched film 2 not marked while film 1 is watched and in the watchlist; a later watchlist change not reflected).
- Green: the ViewModel collects the watchlist IDs and marks unwatched films only, recomputing without a new credits request. `:feature:watched-movies:impl:testDebugUnitTest` passed.

### 6.3 Card

- Red: `givenPendingFilmInTheWatchlist_whenShown_thenAnnouncesItIsInTheWatchlist` failed (no "In Watchlist" content description).
- Green: `DirectorFilmCard` uses the shared ribbon (watched without a duplicate announcement, watchlist announced as "In Watchlist") at full emphasis over the poster, and the pending treatment is softened to 60% opacity and 25% saturation. The module's instrumented suite passed (32 tests).
- Screenshots: re-recorded the 12 Director collection baselines (Inception in the Watchlist in the partial scenarios) and inspected them; with placeholders in dark theme the watched ribbon has low contrast against the grey card, while on real posters (emulator demo) it reads clearly, as on Home.
- Gate: `verifyPaparazziDebug ktlintCheck detekt :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :feature:search-movies:impl:testDebugUnitTest :feature:search-movies:impl:compileDebugAndroidTestKotlin :app:compileJourneyTestAndroidTestKotlin` passed.
- Demo on `emulator-5554` (guest session): Inception and Tenet added to the Watchlist show the bookmark ribbon on their dimmed posters; watched films show the check ribbon; light and dark themes checked.


## Phase 7 — List layout (revision 2026-11-06)

Plan updated first (design list-layout revision, spec requirement and scenarios reworded from poster grid to list rows, tasks 7.1–7.2).

### 7.1 Status badge

- Refactor with existing coverage: the ribbon's icon, colors and descriptions moved into a private `MovieRibbonStatus.presentation()` shared by `MovieStatusRibbon` and a list status mark (first a trailing `MovieStatusBadge`, replaced in 7.3). `:feature:search-movies:impl:verifyPaparazziDebug` and `:core:design-system:verifyPaparazziDebug` passed unchanged.

### 7.2 List rows

- Red: `givenFilms_whenShown_thenShowsTitleAndReleaseYearInChronologicalRows` failed against the poster grid (no year text). `givenFilmWithoutReleaseDate_whenShown_thenShowsOnlyTheTitle` passed already and stays as a regression guard.
- Green: `DirectorFilmCard` replaced by `DirectorFilmRow` (thumbnail, title, year, trailing badge; pending rows dimmed with a secondary title color) and the grid by a `LazyColumn` with the same keys and content types. `WatchedPosterFallbackTest` now checks that a failed poster leaves title and year readable. `MovieCard.PlaceholderContent` is private again. The module's instrumented suite passed on `emulator-5554` (34 tests, 0 failures).
- Screenshots: re-recorded the 12 Director collection baselines and inspected partial, shorts, pending hidden, loading, error and Spanish large font in light and dark; no other goldens changed.
- Checks: `:feature:watched-movies:impl:testDebugUnitTest :feature:search-movies:impl:testDebugUnitTest :core:design-system:testDebugUnitTest detekt ktlintCheck` and `verifyPaparazziDebug` for watched-movies, search-movies and design-system passed.

### 7.3 Inline status and summary card

- Review feedback: the trailing filled check looked like a tappable checkbox and outweighed the title; the progress bar looked thin and generic.
- Red: three of the four `DirectorCollectionUiStateTest` cases for `remainingCount` failed against a `null` scaffold (the unknown-total case passed); `givenPartialCollection_whenShown_thenShowsHowManyFilmsAreLeft`, `givenCompleteCollection_whenShown_thenCelebratesTheCompleteFilmography` and the watched and watchlist label assertions failed. `givenUnknownTotal_whenShown_thenShowsNoRemainingCount` passed already and stays as a guard.
- Green: `remainingCount = (total - watched).coerceAtLeast(0)` when the total is known; `MovieStatusBadge` replaced by `MovieStatusLabel` (16dp icon, labelMedium text, accent tint); `DirectorFilmRow` shows the year, a separator hidden from accessibility services and the label, and keeps the "Not watched" state description only for pending films; the summary is a `surfaceContainer` card with a headline percentage, an 8dp round-cap bar without stop indicator and the films-left or completion line. New plural `director_collection_remaining` and string `director_collection_complete` in English and Spanish. A first green run failed because the year and separator shared one text; the separator is now its own text. `:feature:watched-movies:impl:testDebugUnitTest` and the module's instrumented suite on `emulator-5554` (37 tests, 0 failures) passed.
- Screenshots: re-recorded the 12 Director collection baselines and inspected partial, loading, light, dark and Spanish large font.
- Checks: unit tests for watched-movies, search-movies and design-system, `detekt`, `ktlintCheck` and `verifyPaparazziDebug` for those three modules passed. Demo on `emulator-5554` (guest) with real posters in light and dark.

### 7.3 Follow-up review

- The status label moved to the end of the row (year alone under the title, no separator) and the summary was compacted: percentage at `titleMedium`, 12dp vertical padding, 8dp spacing, films-left line at `bodySmall`. Copy and semantics unchanged, so no new tests; the module's instrumented suite passed again, the 12 baselines were re-recorded and inspected (including Spanish large font, where long titles wrap before the label), and unit tests, `detekt`, `ktlintCheck`, `verifyPaparazziDebug` for watched-movies, search-movies and design-system and `:app:assembleDebug` passed. Demo on `emulator-5554` with real posters.
- Second review: the right-aligned labels looked ragged ("Watched" and "In Watchlist" differ in width) and the compact card still felt heavier than the original. Restored the inline label after the year and the original summary (no card, `labelLarge` percentage, standard Material indicator), keeping the `bodySmall` films-left line. The module's instrumented suite passed, the 12 baselines were re-recorded and inspected, and unit tests, `detekt`, `ktlintCheck`, `verifyPaparazziDebug` for watched-movies, search-movies and design-system and `:app:assembleDebug` passed.
- Third review: the default Material 3 indicator showed a gap between progress and track and a stop dot at the end. Set `gapSize = 0`, no stop indicator, round caps and a `surfaceVariant` track; re-recorded and inspected the 12 baselines; unit tests, `detekt`, `ktlintCheck`, `verifyPaparazziDebug` for watched-movies, search-movies and design-system and `:app:assembleDebug` passed; checked on `emulator-5554`.
