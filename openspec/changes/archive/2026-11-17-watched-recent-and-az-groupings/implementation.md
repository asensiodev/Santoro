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

## Phase 3 — UI (2026-11-17)

New plain Compose tests live in `WatchedGroupHeadingsScreenTest` (androidTest) so `WatchedCollectionScreenTest` stays small. Instrumented runs used `emulator-5554` only (Pixel_9a AVD, Android 17). The emulator first failed with `INSTALL_FAILED_INSUFFICIENT_STORAGE`; uninstalling the release build and the system updates of preinstalled Google apps freed 1.5 GB (environment issue, not a test result).

### 3.1 Recent, A–Z and chips

- Red: `./gradlew -PenableFirebase=false :feature:watched-movies:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=…WatchedGroupHeadingsScreenTest` failed only on `givenAlphabeticalGrouping_whenShown_thenShowsLetterAndOtherTitlesHeadings` (no node announced "Numbers and symbols"). Recent without headings in Shelf and List and the chip order passed immediately: they were already delivered by 1.1 and 1.2 (regression coverage).
- Green: the "#" heading title carries the content description `watched_group_other_titles_description` ("Numbers and symbols" / "Números y símbolos").

### 3.2 Collapsible director heading

- Red: the same command failed on four new cases: collapsed group shows the count without movies, expand control (content description, state description, 48dp, toggle intent without opening the director), expanded group shows the collapse control, and Unknown director has the control without a director button. Opening the director from the name, no control while searching and no control for Genre passed immediately (regression coverage).
- Green: `WatchedGroupHeading` is now a row with an optional leading `ExpandToggle` (48dp `IconButton`, `AppIcons.ExpandMore` rotated 180° when expanded, "Expand/Collapse <name>" plus "Expanded/Collapsed" state description) and a `GroupTitle` that keeps the director button, count and trailing chevron. `WatchedMovieList` skips movies of collapsed groups and shows the control only while the query is blank. New strings in English and Spanish: `watched_group_expand`, `watched_group_collapse`, `watched_group_expanded`, `watched_group_collapsed`.
- The first green run failed twice: the M3 `IconButton` node measured 40dp (the 48dp touch area sits outside the node), fixed with an explicit `Size.size48`; and `WatchedMoviesFeatureTest` still expected the "2 movies" text twice (header and monthly heading), now once because Recent has no heading. `:feature:watched-movies:impl:connectedDebugAndroidTest` then passed 48/48.

### 3.3 Screenshots

- `WatchedMoviesScreenScreenshotTest`: `content` now renders Recent in Shelf; `listDate` replaced by `listRecent`; new `listAlphabetical` (A, D, F and "#") and `spanishLargeDirector` (List, partly expanded, Spanish, 2× font); `shelfDirector` is partly expanded and `listDirector` fully collapsed. Recorded with `./gradlew -PenableFirebase=false :feature:watched-movies:impl:recordPaparazziDebug --tests '*WatchedMoviesScreenScreenshotTest*'`; the two `listDate` baselines were deleted.
- Inspection of the first recording showed the director name wrapping ("Denis / Villeneuve") with free space and misaligned chevrons: a weighted spacer shared the width with the title. Title and count now sit in their own weighted row and the chevron follows it. Re-recorded and inspected light and dark `shelfDirector`, `listDirector`, `listAlphabetical`, `listRecent`, `content`, `spanishLargeDirector` and `spanishLargeDecade`. Other collection baselines changed only in the chip row (Recent, A–Z).
- `./gradlew -PenableFirebase=false verifyPaparazziDebug detekt ktlintCheck :architecture-tests:test :feature:watched-movies:impl:testDebugUnitTest :feature:watched-movies:impl:connectedDebugAndroidTest` passed (connected 48/48).

## Phase 4 — Navigation and validation (2026-11-17)

### 4.1 Watched collection journey

- `MainActivityWatchedCollectionJourneyTest`: the existing journey now ends by checking that, back on Watched, the Director grouping is still selected with the director collapsed (its movie hidden). New `givenCollapsedDirector_whenExpandedAndScreensAreVisited_thenBackKeepsItExpanded`: Director grouping starts collapsed, the expand control shows the director's movie, opening it and going Back keeps the group expanded, opening the Director collection from the name and going Back keeps it expanded. Written after the screen behavior existed, so this is journey coverage rather than test-first.
- `./gradlew -PenableFirebase=false :app:connectedJourneyTestAndroidTest` on `emulator-5554`: 19/19.

### 4.2 Aggregate validation

- `./gradlew -PenableFirebase=false test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug :architecture-tests:test` passed (only pre-existing deprecation and R8 rule warnings).
- Connected on `emulator-5554` (Pixel_9a AVD, Android 17): `:feature:watched-movies:impl:connectedDebugAndroidTest` 48/48 (end of phase 3, no production change since) and `:app:connectedJourneyTestAndroidTest` 19/19.
- Not run: the release startup check, which belongs to release-candidate distribution.
- Smoke check: a debug build installed on the emulator showed A–Z with a real collection (41 movies, letter headings, posters). The emulator had lost network (wifi "!") until the user cold-booted it.

### 4.3 Manual checks (2026-11-17)

- Done by the product owner: upgrade with Date watched saved, Recent and A–Z with a real collection in Shelf and List, collapse and search in Director, Spanish, light and dark, TalkBack on headings and expand controls, and large font. No issues reported.

### 4.4 Close (2026-11-17)

- PRD F-34 marked implemented, pending release, with a link to the archived change; its side-index boundary now points to the new idea F-36. The same PRD update (2.27) records why Play lists the F-23 TMDB links as misconfigured and adds the ideas F-35 (verified share links) and F-36 (alphabetical fast-scroll index).
- Delta specs synced to `openspec/specs/watched-collection` (Group the collection replaced, Collapse director groups added, Purpose updated to drop grouping by date watched), then the change was archived during the review of PR #8.
