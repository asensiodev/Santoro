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

