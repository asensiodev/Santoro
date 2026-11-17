# Implementation record — watched-recent-and-az-groupings (F-34)

## Phase 1 — Grouping model and saved preference (2026-11-17)

### 1.1 Recent default and legacy Date watched

- Setup: added `RECENT` and `ALPHABETICAL` to `WatchedGrouping` while keeping `DATE_WATCHED`, so the new tests compiled and failed on behavior.
- Red: `./gradlew -PenableFirebase=false :core:data:test --tests '*DefaultUserPreferencesRepositoryTest*'` failed on `GIVEN no saved grouping WHEN init THEN grouping emits recent`, `GIVEN saved date watched grouping from an earlier version WHEN init THEN grouping emits recent` and `GIVEN missing or invalid collection preferences WHEN init THEN flows emit shelf and recent` (each emitted `DATE_WATCHED`). `GIVEN saved alphabetical grouping WHEN init THEN grouping emits alphabetical` passed immediately and is regression coverage.
- Green: removed `DATE_WATCHED`; `DefaultUserPreferencesRepository.readWatchedGrouping()` maps the stored legacy name `DATE_WATCHED` to `RECENT` and defaults to `RECENT`. Updated the `WatchedMoviesUiState` default, the chip labels (`watched_group_recent` "Recent"/"Recientes", `watched_group_alphabetical` "A–Z"; `watched_group_date` removed) and every test and fake reference. Until 1.2 and 1.3, the mapper sent both new values to the old monthly grouping so the build stayed green. `:core:data:test`, `:feature:watched-movies:impl:testDebugUnitTest` and the androidTest compilations of watched-movies, movie-detail and the app journeys passed.

### 1.2 Recent

- Red: `./gradlew -PenableFirebase=false :feature:watched-movies:impl:testDebugUnitTest --tests '*WatchedGroupMapperTest*'` failed on `GIVEN movies added in different months WHEN grouped by recent THEN shows one section without heading` (month groups returned). `GIVEN no movies WHEN grouped by recent THEN returns no groups` passed immediately (regression coverage).
- Green: `groupByRecent()` returns one group with key `recent`, no label and the repository order (`watchedAt` descending, nulls last from Room). `WatchedGroupUi.label` is nullable and `WatchedMovieList` skips the heading item for an unlabeled group. Removed `MovieUi.watchedDate`, its "MMMM yyyy" formatter, `WatchedGroupLabel.UnknownDate` and the monthly grouping; the two ViewModel tests for movies without `watchedAt` now assert they appear in the Recent section. The formatted-date test in `MovieUiMapperTest` was deleted with the field. Module unit tests and androidTest compilation passed.

### 1.3 A–Z

- Setup: added `WatchedGroupLabel.OtherTitles` (rendered from the non-translatable `watched_group_other_titles` "#") so the tests compiled.
- Red: the same mapper test command failed on all five new cases: letters ignoring case and accents with "#" last, leading article under T, leading spaces and blank title, order within a letter, unique keys (the interim mapping returned one Recent group).
- Green: `groupAlphabetically()` takes the first non-space character, NFD-normalizes it, keeps it when it is `A`–`Z` and otherwise files the movie under `OtherTitles`, last. Within a group, movies sort with a `Locale.ROOT` primary-strength `Collator` on the trimmed title, then release date. Keys are `letter:<A-Z>` and `letter:other`. `:feature:watched-movies:impl:testDebugUnitTest` passed.

### Phase checks

- `./gradlew -PenableFirebase=false detekt ktlintCheck :architecture-tests:test :core:data:test :feature:watched-movies:impl:testDebugUnitTest` passed after shortening two test names over 120 characters.
- Pending by design: Paparazzi baselines (the chip label changed and the date fixtures are replaced in 3.3) and connected tests (phases 3 and 4).
