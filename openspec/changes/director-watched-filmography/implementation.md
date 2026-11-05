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

