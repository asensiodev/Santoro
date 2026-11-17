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

## Phase 2 — Collapse state (2026-11-17)

### 2.1 Director groups collapse in the ViewModel

- Setup: added `WatchedGroupUi.isCollapsible`/`isExpanded` (defaults false/true), `WatchedMoviesUiState.expandedGroupKeys` and the `ToggleGroup(key)` intent handled as a no-op, so the tests compiled and failed on behavior.
- Red: `./gradlew -PenableFirebase=false :feature:watched-movies:impl:testDebugUnitTest --tests '*WatchedMoviesViewModelTest*'` failed on six new cases: director groups (including Unknown director) start collapsed, `ToggleGroup` expands then collapses, a new director group starts collapsed, a search expands matches and clearing restores the expanded set, `CloseSearch` restores the previous state, and the set survives switching grouping away and back. `GIVEN groupings other than director WHEN loaded THEN groups are not collapsible and show movies` passed immediately (regression coverage).
- Green: the mapper marks director groups and Unknown director as collapsible. The ViewModel keeps the query behind the shown groups (`latestQuery`) and publishes groups through `toVisibleGroups`: a collapsible group is expanded when its key is in `expandedGroupKeys` or a non-blank query is active. `ToggleGroup` adds or removes the key; the set is never changed by searching or regrouping. Three tests first compared maps with Kluent, which checks entry order; they now compare sets of `(key, expanded)` pairs. Module unit tests passed.
- Detekt reported `LargeClass` (634/600) for `WatchedMoviesViewModelTest`; the seven collapse tests moved to `WatchedDirectorGroupsViewModelTest` with their own setup.

### 2.2 Analytics values

- `GIVEN default grouping WHEN content loads and A-Z is selected THEN tracks recent and alphabetical` passed immediately: the values come from the enum names changed in 1.1. Classified as regression coverage.

### Phase checks

- `./gradlew -PenableFirebase=false detekt ktlintCheck :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest` passed after `ktlintFormat` and one manual line wrap in the ViewModel.
- The screen does not read `isExpanded` yet; collapsed headings are built in 3.2.
