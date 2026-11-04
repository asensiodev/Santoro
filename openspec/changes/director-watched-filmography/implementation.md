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
