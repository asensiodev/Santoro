# FIP — Search Movie Status Ribbons

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                    |
|------------------------|--------------------------------------------------------------------------|
| **FIP ID**             | FIP-021                                                                  |
| **Version**            | 2.1                                                                      |
| **Status**             | ✅ Complete — Release Ready                                               |
| **PRD ref**            | Internal UX enhancement — no PRD feature                                 |
| **Feature**            | Exclusive Watched and Watchlist status tags across Search movie cards    |
| **Date**               | 2026-08-14                                                               |
| **Author**             | @asensiodev                                                              |
| **Definition of Done** | All checkboxes are marked `[x]`, §10 is complete, and the next release version is ready |

---

> **Execution rule:** Work phase by phase, task by task. Report each completed task and its evidence to the planner.
> If anything is ambiguous or does not fit the plan — **stop and ask** before deciding.

> **Role rule:** The executor implements code and reports phase evidence without editing this FIP. A planner records that evidence, updates checkboxes, and completes §10.

> **Invariant:** A movie cannot be Watched and In Watchlist at the same time. Presentation must model one optional status and must never render two status tags on one movie.

---

## 0. Prerequisites

- FIP-019 CI run `31823938280` completed green; instrumented job `95444360577` passed in 11m21s.
- FIP-018 Phase 7 was read. Its focused-capability guidance is the relevant dependency; FIP-021 owns only the read capability required by this feature and must not mark broader FIP-018 tasks complete.
- Re-read `SearchMoviesViewModel`, `SeeAllMoviesViewModel`, `MovieUiMapper`, `MovieCard`, and `HeroMovieCard` before editing.
- The Search JVM and Paparazzi baseline is green after the scoped pre-existing snapshot repair recorded in Phase 1 and §11.
- No existing Kotlin declaration named `MovieLibraryStatus`, `StatusRibbon`, or `libraryStatus` was found; no concurrent equivalent decoration or reusable status component exists.

---

## 1. Context & Motivation

Search currently presents movie artwork, title, genre, and rating without showing whether a movie is already Watched or In Watchlist. Users must open Movie Detail or switch tabs to discover that state.

The domain `Movie` already contains `isWatched` and `isInWatchlist`, and Room exposes reactive watched/watchlist flows. Search's `MovieUi` discards those flags, while TMDB and browse-cache results are not authoritative for user state. The feature must therefore decorate every Search-owned movie list with current local state rather than persisting user state into browse cache.

Integrated, non-interactive icon-only corner tags provide useful at-a-glance context while preserving the existing card click behavior. The same treatment must appear across the Search dashboard, Hero card, query results, filtered results, pagination, and See All screens to avoid inconsistent state.

---

## 2. Goals

- Show one integrated Watched corner tag when a Search movie is Watched.
- Show one integrated Watchlist corner tag when a Search movie is In Watchlist.
- Show no tag when a movie has neither status.
- Make it structurally impossible for one card to display both tags.
- Keep status reactive when Room changes, including after returning from Movie Detail.
- Apply the same status contract to dashboard, Hero, query, filtered, paginated, and See All movie cards.
- Preserve Search loading, content, empty, stale-data, pagination, error, navigation, and cache behavior.
- Prepare the next available patch version after all quality gates pass.

---

## 3. Non-Goals

- No ability to add, remove, or toggle Watched/Watchlist state from a Search card.
- No change to the rule that Watched and In Watchlist are mutually exclusive.
- No new database table, column, migration, Firestore field, API field, or browse-cache field.
- No persistence of user state inside TMDB responses or browse-cache JSON.
- No network refresh triggered by a local movie-status change.
- No redesign of Search cards or Hero layout beyond the approved integrated status corner, and no redesign of Movie Detail actions, Watchlist, or Watched screens.
- No new destination, navigation route, analytics event, notification, or synchronization policy.
- No broad extraction of a shared cross-feature badge system unless an immediate second production consumer is identified.
- No broad `DatabaseRepository` capability split or migration of unrelated consumers from FIP-018.
- No FIP-020 test or accessibility work beyond coverage required for this feature.
- No Play Console publishing or signing-process change; this FIP prepares the next release artifact and version only.

---

## 4. User Stories

| ID    | As a… | I want to… | So that… | Acceptance Criteria |
|-------|-------|------------|----------|---------------------|
| US-01 | user | see that a Search movie is already Watched | I do not need to open Movie Detail to check | Every Search-owned card for a Watched movie shows exactly one Watched corner tag |
| US-02 | user | see that a Search movie is already in my Watchlist | I can avoid saving or checking it again | Every Search-owned card for a Watchlist movie shows exactly one Watchlist corner tag |
| US-03 | user | see status update after changing it in Movie Detail | Search remains trustworthy without a refresh | Returning to Search reflects the latest Room state while preserving loaded content and position |
| US-04 | assistive-technology user | hear the movie's current status | the visual tag is not the only source of information | The tag exposes the localized `Watched` or `In Watchlist` description |

---

## 5. UX / Flows

### Screen: Search Movies

| State | What the user sees | User actions available |
|-------|--------------------|------------------------|
| Loading | Existing loading UI; no status placeholder | Existing behavior |
| Dashboard content | Existing Hero and section cards with zero or one integrated top-right status tag | Tap card → Movie Detail; existing See All actions |
| Query/filter content | Existing result cards with zero or one integrated top-right status tag | Tap card → Movie Detail; existing search/filter actions |
| Empty / No results | Existing empty UI; no tag | Existing actions |
| Error | Existing error UI; no new status-specific error | Existing Retry behavior |
| Stale content | Existing stale movie content decorated with the latest available local status | Existing actions |

### Screen: See All Movies

| State | What the user sees | User actions available |
|-------|--------------------|------------------------|
| Loading | Existing loading UI | Existing behavior |
| Content | Existing paginated cards with zero or one integrated top-right status tag | Tap card → Movie Detail; load more |
| Empty | Existing empty UI | Existing actions |
| Error | Existing error UI; no new status-specific error | Existing Retry behavior |

### Status Tag Contract

| Movie status | Visual | Accessibility | Interaction |
|--------------|--------|---------------|-------------|
| None | No tag | No additional status description | Card remains clickable |
| Watchlist | `AppIcons.Watchlist` on `tertiaryContainer` with `onTertiaryContainer` content | Existing localized `In Watchlist` label | Informational only |
| Watched | `AppIcons.Watched` on `secondaryContainer` with `onSecondaryContainer` content | Existing localized `Watched` label | Informational only |

- Render an icon-only, integrated top-right corner tag; no visible text appears inside the tag.
- The tag is flush with the card's top and right edges, with no outer edge padding or floating separation.
- On regular `MovieCard`, the semantic Material background color fills a right isosceles triangle whose two `Size.size56` legs run along the card's top and right edges; the `Size.size18` icon uses `Spacings.spacing8` padding.
- On `HeroMovieCard`/Now Playing, the same right-isosceles contract uses two `Size.size64` legs and a 10dp-equivalent icon inset composed only from existing tokens: `Spacings.spacing8 + Size.size2`. This keeps the `Size.size18` icon balanced between the diagonal and rounded card edge.
- For both cards, one diagonal joins the two outer leg endpoints.
- The rejected quadrilateral/polygon whose diagonal ends at `Size.size32` along the bottom edge is not permitted.
- Use only the card-specific icon spacing above and design-system tokens for all geometry.
- Keep the icon legible within the filled corner region without covering Hero title/rating content.
- Use only design-system size, shape, spacing, and Material color tokens; add no raw `.dp` or hardcoded user-facing strings.
- A card accepts one nullable status value, not two independently renderable flags or tags.

---

## 6. Architecture

```text
Room DatabaseRepository
    │
    ▼
DefaultMovieLibraryStatusRepository (Search data layer)
    ├── observeWatchedMovieIds()
    └── observeWatchlistMovieIds()
              │
              ▼
MovieLibraryStatusRepository (Search domain interface)
              │
              ▼
ObserveMovieLibraryStatusesUseCase
    └── Flow<Result<Map<Int, MovieLibraryStatus>>>
              │
              ├── SearchMoviesViewModel
              └── SeeAllMoviesViewModel
                        │
                        ▼
MovieUi.libraryStatus: MovieLibraryStatus?
                        │
                        ▼
MovieCard / HeroMovieCard → MovieStatusRibbon integrated corner tag
```

- Room is the sole source of truth for Watched/Watchlist status.
- TMDB and browse cache remain sources of catalogue content only.
- The Search data implementation adapts the existing database repository behind a Search-domain interface with no Android or Room types.
- The use case combines watched and watchlist ID flows into one ID-to-status map.
- ViewModels retain the latest successful map, apply it to newly loaded pages, and update already loaded UI lists when Room emits.
- Status observation is supplementary: a status-read failure must not replace Search or See All content with an error screen.

---

## 7. Data Model

### New Feature-Domain Status

```kotlin
internal enum class MovieLibraryStatus {
    Watched,
    Watchlist,
}
```

### Modified Presentation Model

```kotlin
internal data class MovieUi(
    val libraryStatus: MovieLibraryStatus? = null,
)
```

- `null` means neither status and renders no tag.
- The model has no representation capable of rendering both statuses or tags.
- Movie Detail preserves the mutually exclusive product behavior. Because the database schema and sync merge do not enforce that invariant structurally, normalize any dual-membership input to `Watched` and never emit two statuses.
- No persisted or remote model changes are permitted.

---

## 8. Modules Affected

- `feature/search-movies/impl`
- `gradle/libs.versions.toml` only for the final next-version bump

Existing resources from `core/string-resources` and icons/tokens from `core/design-system` must be reused. No new module or dependency is planned.

---

## 9. Phases & Tasks

### Phase 1 — Baseline and Status Contract

**Data sources**
- Current Search and See All state models, ViewModels, mappers, and cards.
- Existing Room watched/watchlist flows.
- Existing Movie Detail exclusivity behavior.
- Existing Search unit and Paparazzi suites.

**Side effects**
- ✅ Allowed:
    - Run read-only tests, coverage, and task-discovery commands.
    - Report baseline results and exact Paparazzi tasks to the planner for §10.
    - Before any FIP-021 production change, inspect and resolve only the three pre-existing snapshot drifts recorded below. Baseline images may be updated only when visual inspection confirms they represent current intended behavior; otherwise stop and ask.
- ❌ Forbidden:
    - Change production behavior or version metadata while the baseline blocker is open.
    - Update any screenshot other than the three explicitly recorded pre-existing failures.
    - Treat TMDB or browse cache as authoritative user-state sources.

- [x] Run Search debug/release JVM tests and record the baseline.
- [x] Run current MovieCard and HeroMovieCard Paparazzi verification and record the baseline.
- [x] Confirm every Search-owned card surface: dashboard sections, Hero, query results, filtered results, pagination, and See All.
- [x] Confirm Movie Detail preserves Watched/Watchlist exclusivity and document that Room/sync do not enforce it structurally.
- [x] Confirm existing `Watched`, `In Watchlist`, `AppIcons.Watched`, and `AppIcons.Watchlist` resources are suitable.
- [x] Record any pre-existing failure as a blocker rather than weakening planned assertions.

#### Phase 1 Baseline

| Invocation | Result |
|------------|--------|
| Initial `./gradlew :feature:search-movies:impl:testDebugUnitTest :feature:search-movies:impl:testReleaseUnitTest :feature:search-movies:impl:verifyPaparazziDebug :feature:search-movies:impl:verifyPaparazziRelease` | ❌ Blocked on 2026-08-18: debug executed 103 tests and failed on three pre-existing snapshots; Gradle stopped before release |
| Exact invocation rerun after scoped baseline repair | ✅ `BUILD SUCCESSFUL` on 2026-08-18: debug 103 tests, release 103 tests, 0 failures and 0 ignored in both variants; `verifyPaparazziDebug` and `verifyPaparazziRelease` passed |

Visual inspection confirmed that the three authorized diffs contained only minimal rasterization/antialiasing differences, with no content, geometry, hierarchy, or production defect. Only `MovieCardScreenshotTest_captureScreenshot.png`, `SearchSuggestionsContentScreenshotTest_captureWithRecentsAndTrending.png`, and `SearchSuggestionsContentScreenshotTest_captureWithTrendingOnly.png` were updated. `HeroMovieCard` was restored and is not modified.

Search-owned surfaces are confirmed as the dashboard Hero in `NowPlayingSection`, dashboard cards and popular pagination in `MovieSection`, query/filter/pagination in `MovieList`, and See All in `SeeAllMovieGrid`; every surface consumes `HeroMovieCard` or `MovieCard`.

`MovieDetailViewModel` preserves exclusivity by forcing Watched to false when Watchlist is activated and forcing Watchlist to false when Watched is activated. `DatabaseRepository` and sync accept both booleans and do not enforce the invariant structurally.

Existing localized resources are suitable: `watchlist_icon_button_added` is `In Watchlist`/`En mi lista`, and `watched_icon_button_marked` is `Watched`/`Vista`. Existing `AppIcons.Watched` (`Rounded.CheckCircle`) and `AppIcons.Watchlist` (`Rounded.Bookmark`) are suitable. No existing `MovieLibraryStatus`, `StatusRibbon`, or `libraryStatus` Kotlin declaration was found.

Phase 1 is complete and blocker 2 is resolved. Phase 2 was authorized under its declared data-source and side-effect constraints.

### Phase 2 — Reactive Local Status Contract

**Data sources**
- `DatabaseRepository.getWatchedMovies()`.
- `DatabaseRepository.getWatchlistMovies()`.
- Movie IDs adapted by a new Search-domain `MovieLibraryStatusRepository` interface.

**Side effects**
- ✅ Allowed:
    - Observe existing Room flows.
    - Combine successful emissions into an in-memory ID-to-status map.
    - Retain the latest successful map while a later status emission fails.
    - Add a Search-domain repository interface and Search-data implementation around the existing database repository.
- ❌ Forbidden:
    - Write, remove, or update movie state.
    - Trigger network, cache refresh, Firebase, synchronization, or navigation operations.
    - Add a new database/API data source or modify the core database repository contract.

- [x] Add the internal exclusive `MovieLibraryStatus` model.
- [x] Add a pure Search-domain `MovieLibraryStatusRepository` interface exposing watched and watchlist ID flows.
- [x] Add `DefaultMovieLibraryStatusRepository` in the Search data layer to adapt the existing `DatabaseRepository` movie flows to ID sets.
- [x] Add `ObserveMovieLibraryStatusesUseCase` using only the Search-domain repository interface.
- [x] Emit `Watchlist` for IDs present only in Watchlist and `Watched` for IDs present in Watched.
- [x] Defensively normalize an ID present in both sets to `Watched` while preserving the one-status invariant.
- [x] Emit a combined map only when both latest source results are successful; emit failure when either latest source result fails.
- [x] Allow a later pair of successful source emissions to recover after failure.
- [x] Propagate expected failures as `Result.failure` and rethrow `CancellationException` rather than wrapping it.
- [x] Bind/provide the data implementation and use case through the existing Search Hilt module without unnecessary scope.

Phase 2 adds the internal `MovieLibraryStatus` enum, focused ID-flow repository contract, `DatabaseRepository` adapter, combined-status use case, and unscoped Hilt providers. The adapter uses only `getWatchedMovies()` and `getWatchlistMovies()`. The use case requires both latest results to succeed, emits failure from either source, recovers after later success, propagates direct and wrapped cancellation, and inserts Watchlist before Watched so Watched wins dual membership. Six repository tests and eight use-case tests pass; focused Kover reports 10/10 repository lines and 4/4 branches, plus 15/15 use-case/lambda lines and 4/4 branches.

### Phase 3 — Search and See All State Enrichment

**Data sources**
- Existing Search/dashboard/See All movie results.
- Latest successful map from `ObserveMovieLibraryStatusesUseCase`.
- Existing ViewModel `uiState` only.

**Side effects**
- ✅ Allowed:
    - Decorate in-memory `MovieUi` values by movie ID.
    - Update already loaded lists when local status changes.
    - Apply the latest status map to initial loads, refreshes, filters, and pagination.
- ❌ Forbidden:
    - Refetch catalogue data because status changed.
    - Reset query, filter, pagination, scroll/navigation state, stale-data state, or loaded content.
    - Surface a full-screen Search error solely because status observation failed.

- [x] Add one nullable `libraryStatus` property to `MovieUi`.
- [x] Update mapping helpers and fixtures so absent local status maps to `null`.
- [x] Inject and collect the observer in `SearchMoviesViewModel` using existing coroutine/lifecycle conventions.
- [x] Apply status to every dashboard, query, filtered, and paginated list in `SearchMoviesUiState`.
- [x] Update already loaded Search lists immediately when Room emits a changed status map.
- [x] Inject and collect the same observer in `SeeAllMoviesViewModel`.
- [x] Apply status to See All initial, refreshed, and appended pages without losing pagination state.
- [x] Retain only the latest complete successful combined map after a later observer failure; use no status tags before the first complete success.
- [x] Ensure logout/database clear emissions remove all visible status tags without a catalogue refresh.

Phase 3 adds one nullable `libraryStatus` to `MovieUi`; mapper defaults remain `null` and accept the current status map. Search retains the latest successful map, redecorates loaded Now Playing, query/filter/search pagination, Popular, Top Rated, Upcoming, and Trending content, and applies it to later loads. See All applies the same policy to initial, refreshed, and appended pages. Focused green tests cover dashboard, filter, query pagination, clear/logout, failures preserving the exact state without refetch, and See All initial/append/failure behavior.

### Phase 4 — Exclusive Integrated Corner Tag UI

**Data sources**
- `MovieUi.libraryStatus` only.
- Existing localized status strings.
- Existing design-system icons and Material/design tokens.

**Side effects**
- ✅ Allowed:
    - Render one non-interactive integrated corner tag.
    - Expose localized status semantics.
- ❌ Forbidden:
    - Mutate movie state from a status tag.
    - Add independent Watched and Watchlist booleans to the tag API.
    - Add raw dimensions, hardcoded strings, or non-token colors.

- [x] Use the approved right isosceles `MovieStatusRibbon(status)` triangle with two `Size.size56` legs and `Spacings.spacing8` icon padding for regular `MovieCard`, retaining the three-point geometry, `Size.size18`, exhaustive status branches, and `@PreviewLightDark` coverage.
- [x] Render Watchlist with `AppIcons.Watchlist`, `tertiaryContainer`, and `onTertiaryContainer` for stronger blue distinction and dark-theme contrast.
- [x] Render Watched with `AppIcons.Watched`, `secondaryContainer`, and `onSecondaryContainer`.
- [x] Reuse `watchlist_icon_button_added` and `watched_icon_button_marked` accessibility labels.
- [x] Integrate the refined `Size.size56` triangle into `MovieCard` with equal-length legs flush along the top and right edges and one diagonal joining their outer endpoints for poster, placeholder, dashboard, query, and See All usage.
- [x] Refine `HeroMovieCard`/Now Playing to use a `Size.size64` right-isosceles triangle and `Spacings.spacing8 + Size.size2` icon inset without obscuring title or rating content.
- [x] Preserve the existing card click target, minimum touch target, outer card shape, loading, image, and placeholder behavior after the visual revision.
- [x] Verify regular `MovieCard` has equal `Size.size56` top/right leg lengths, `Spacings.spacing8` icon padding, the single endpoint-to-endpoint diagonal, icon legibility, Watched secondary versus Watchlist tertiary distinction, and no edge gaps in light/dark themes and English/Spanish resources.
- [x] Verify `HeroMovieCard`/Now Playing has equal `Size.size64` top/right leg lengths, `Spacings.spacing8 + Size.size2` icon inset, balanced icon space from the diagonal and rounded edge, unobscured content, and preserved secondary/tertiary colors in light/dark themes.

Phase 4 is complete. `MovieStatusRibbon` accepts configurable `tagSize` and `iconPadding`: regular `MovieCard` uses `Size.size56` and `Spacings.spacing8`, while `HeroMovieCard`/Now Playing uses `Size.size64` and `Spacings.spacing8 + Size.size2`. Watched remains `secondaryContainer`/`onSecondaryContainer`; Watchlist remains `tertiaryContainer`/`onTertiaryContainer`. Nullable exclusivity, localized semantics, non-interaction, preserved card behavior, balanced Hero spacing, and unobscured content are approved.

### Phase 5 — Automated Coverage

**Data sources**
- Synthetic movie IDs and in-memory watched/watchlist flows.
- Existing Search test fixtures and coroutine utilities.
- Deterministic Paparazzi images with no external network dependency.

**Side effects**
- ✅ Allowed:
    - Emit local status changes through controlled test flows.
    - Record intentional Paparazzi baselines for the three status variants.
- ❌ Forbidden:
    - Use live Room, TMDB, Firebase, image network calls, or real user data in JVM tests.
    - Update unrelated screenshot baselines.

- [x] Add 100% use-case coverage for none, Watched, Watchlist, changed emissions, both-set normalization, expected failure, and cancellation.
- [x] Add repository implementation coverage for ID mapping, watched/watchlist source failures, recovery, and cancellation.
- [x] Update mapper tests for the nullable exclusive status property.
- [x] Add Search ViewModel tests proving already loaded dashboard/query/filter/pagination content updates reactively.
- [x] Add Search ViewModel tests proving state, query, filter, pagination, and content survive status changes and failures.
- [x] Add See All ViewModel tests for initial pages, appended pages, reactive changes, and status-observer failure.
- [x] Add Compose semantics assertions for None, Watched, and Watchlist, including exactly one status description per card.
- [x] Record and inspect regular MovieCard None, Watched, and Watchlist light/dark Paparazzi cases against the `Size.size56`, `Spacings.spacing8`, and tertiary Watchlist contract.
- [x] Re-record and inspect HeroMovieCard Watched and Watchlist light/dark Paparazzi cases against the `Size.size64` and `Spacings.spacing8 + Size.size2` Hero contract; preserve the restored historical no-status Hero baseline unless the unchanged state produces an intentional diff.
- [x] Confirm no test or preview can construct a card with two simultaneously rendered status tags.

Phase 5 is complete. Final regular MovieCard and Hero Watched/Watchlist light/dark snapshots were refreshed and visually approved; the historical Hero no-status baseline was restored. Focused debug and release suites each pass 135 tests, and debug/release Paparazzi verification is green. Three Android semantics tests on the `Pixel_9a` AVD at API 36 assert zero descriptions for None and exactly one mutually exclusive description for each status. Test method names use camelCase because D8 for dex versions before 040 rejects spaces from backtick names; assertions are unchanged. The nullable enum model cannot represent two simultaneous tags. Search module Detekt and ktlint checks pass.

Phases 2-5 are complete for the final approved visual contract. No unresolved implementation, behavior, semantics, or snapshot task remains.

### Phase 6 — Verification and Release Preparation

**Data sources**
- Completed implementation and generated test/Paparazzi reports.
- Current `versionCode` and `versionName` in `gradle/libs.versions.toml` at execution time.
- Existing CI and release build configuration.

**Side effects**
- ✅ Allowed:
    - Run JVM, Paparazzi, static-analysis, coverage, and build tasks.
    - Increment to the next available patch `versionName` and `versionCode` after validation passes.
    - Report exact validation evidence to the planner.
- ❌ Forbidden:
    - Change signing configuration, CI thresholds, Play Console configuration, or unrelated release metadata.
    - Publish a release before all automated gates and the manual visual check pass.

- [x] Run Search debug and release JVM suites from a clean invocation.
- [x] Run Search Paparazzi verification and inspect the intentional Hero baselines for equal `Size.size64` top/right legs, `Spacings.spacing8 + Size.size2` icon inset, balanced diagonal/rounded-edge space, unobscured content, and preserved secondary Watched versus tertiary Watchlist colors.
- [x] Run `./gradlew ktlintCheck detekt test assembleDebug`.
- [x] Run `./gradlew :koverXmlReport :koverHtmlReport :koverVerify` without changing thresholds.
- [x] Manually verify regular `MovieCard` and the refined Hero/Now Playing right-isosceles corner tags on dashboard, query, filter, pagination, See All, and return-from-Detail behavior on an emulator.
- [x] Verify None, Watched, and Watchlist in light/dark mode; confirm regular cards retain `Size.size56`/`Spacings.spacing8`, Hero uses `Size.size64`/`Spacings.spacing8 + Size.size2` with balanced diagonal and rounded-edge space, colors remain distinct, content is unobscured, and no card displays two tags.
- [x] Verify TalkBack announces the movie and its single status without introducing an extra action.
- [x] Increment to the next available patch version and version code only after all checks pass.
- [x] Run `./gradlew bundleRelease` with the existing release keystore properties and verify the signed `app/build/outputs/bundle/release/app-release.aab` artifact.
- [x] Confirm release credentials are available and the produced AAB is signed and verifiable.
- [x] Provide exact commands, versions, environment, results, and skipped checks to the planner for §10.

#### Phase 6 Final Evidence

| Invocation | Result |
|------------|--------|
| `./gradlew :feature:search-movies:impl:testDebugUnitTest :feature:search-movies:impl:recordPaparazziDebug :feature:search-movies:impl:ktlintCheck :feature:search-movies:impl:detekt` | ✅ Passed: 135 debug tests; final Hero baselines recorded; Search ktlint and Detekt green |
| `./gradlew :feature:search-movies:impl:verifyPaparazziDebug :feature:search-movies:impl:testReleaseUnitTest :feature:search-movies:impl:verifyPaparazziRelease` | ✅ Passed: 135 release tests; debug and release Paparazzi green; historical Hero no-status baseline restored |
| `./gradlew ktlintCheck detekt test assembleDebug` | ✅ Passed |
| `./gradlew :koverXmlReport :koverHtmlReport :koverVerify` | ✅ Passed without threshold changes: LINE 2,729 covered / 650 missed (80.8%); BRANCH 789 covered / 271 missed (74.4%) |
| `./gradlew bundleRelease` | ✅ Passed with signing validation; release AAB produced |
| `jarsigner -verify app/build/outputs/bundle/release/app-release.aab` | ✅ `jar verified`; certificate expiry warning is 2049-12-04 |

Manual emulator flows and TalkBack evidence remain valid, and the user explicitly validated and approved the final visual design. Version advanced from `1.0.32` (`versionCode` 40) to `1.0.33` (`versionCode` 41). The signed artifact is `app/build/outputs/bundle/release/app-release.aab`, 10,856,226 bytes, SHA-256 `85b018815ae6ab61618a15614bedda2d470395238156d3f752cb9520bf3233c3`. The user requested release/push; this FIP records release readiness and authorization but does not claim unreported push execution.

Phase 6 is complete. All automated gates, aggregate coverage thresholds, manual visual/accessibility checks, versioning, bundle generation, signing verification, and artifact-integrity recording pass. FIP-021 is complete and release-ready.

---

## 10. Validation

| What | Result | Notes |
|------|--------|-------|
| Search JVM baseline | ✅ | Exact four-task invocation passed on 2026-08-18: debug 103 tests and release 103 tests, each with 0 failures and 0 ignored |
| Paparazzi baseline | ✅ | Historical pre-feature baseline passed after visual approval and update of only the three authorized rasterization/antialiasing drifts; the historical Hero no-status baseline remains restored |
| Use-case and ViewModel behavior | ✅ | Repository/use case error, recovery, dual-membership, and cancellation contracts pass; Search and See All reactive/state-preservation tests pass in 135-test debug and release suites |
| MovieCard and Hero status tags | ✅ | Final light/dark snapshots and user validation approve regular `Size.size56`/`Spacings.spacing8` and Hero `Size.size64`/`Spacings.spacing8 + Size.size2`; Watched secondary and Watchlist tertiary colors remain distinct |
| Search and See All consistency | ✅ | Automated coverage and manual emulator flows pass for dashboard, Hero, query/filter/pagination, See All, state changes, and return from Detail |
| Exclusivity invariant | ✅ | Nullable enum representation, Watched-wins normalization, and semantics assertions prevent two simultaneous tags |
| Static analysis and build | ✅ | `./gradlew ktlintCheck detekt test assembleDebug` passed |
| Aggregate Kover verification | ✅ | Gate passed without threshold changes: LINE 2,729/3,379 (80.8%); BRANCH 789/1,060 (74.4%) |
| Manual emulator check | ✅ | Previously recorded emulator flows remain valid; user explicitly validated and approved the final visual design |
| Accessibility check | ✅ | Semantics tests pass and previously recorded TalkBack verification remains valid: one localized status, no extra action |
| Release preparation | ✅ | `1.0.32` (40) → `1.0.33` (41); signed AAB is 10,856,226 bytes with recorded SHA-256 and successful `jarsigner` verification |

---

## 11. Blockers

| # | Blocker | Raised | Resolved | Impact |
|---|---------|--------|----------|--------|
| 1 | FIP-019 CI run `31823938280` was still in progress | 2026-08-14 | 2026-08-18 | Resolved: the run completed fully green; instrumented job `95444360577` passed in 11m21s |
| 2 | Search baseline had three pre-existing snapshot failures in `MovieCardScreenshotTest` and `SearchSuggestionsContentScreenshotTest` | 2026-08-18 | 2026-08-18 | Resolved: visual inspection found only minimal rasterization/antialiasing drift; only the three authorized PNGs changed, `HeroMovieCard` remained unmodified, and the exact full invocation passed |

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Can one movie display both Watched and Watchlist tags? | No. The product behavior is mutually exclusive, the UI accepts one optional status only, and defensive normalization protects against unconstrained persisted/synced input |
| 2 | Which Search surfaces receive status tags? | All Search-owned movie cards: dashboard sections, Hero, query/filter results, pagination, and See All |
| 3 | Can users toggle status from the tag? | No. Tags are informational; Movie Detail remains the state-management surface |
| 4 | What happens if local status observation fails? | Preserve catalogue content and the latest successful status map; show no tag if no successful map exists |
| 5 | What version is released? | `1.0.33` (`versionCode` 41), incremented from `1.0.32` (`versionCode` 40) after all validation passed |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Use one nullable status instead of two UI booleans | Add `isWatched` and `isInWatchlist` to `MovieUi` | The type prevents simultaneous tags and directly encodes the product invariant |
| 2 | Observe Room through one Search-owned use case | Trust TMDB flags; persist flags in browse cache; inject two raw flows into each Composable | Room is authoritative, the join is business policy, and Composables remain presentation-only |
| 3 | Enrich existing lists in both ViewModels | Reload Search after returning from Detail | Reactive enrichment updates immediately without network work or state loss |
| 4 | Apply status tags to all Search-owned card surfaces | Query results only; poster cards only | Consistent state avoids contradictory cards for the same movie |
| 5 | Keep status tags informational | Add quick-toggle actions | This feature communicates state without changing interaction, sync, or destructive-action behavior |
| 6 | Keep the component internal to Search | Add a design-system component immediately | There is one current consumer; premature sharing would add unnecessary API surface |
| 7 | Normalize dual-membership input to Watched | Render two tags; crash; choose whichever flow emits last | Movie Detail preserves exclusivity, but Room/sync do not enforce it structurally; normalization guarantees one-status rendering |
| 8 | Add a Search-domain repository interface with a Search-data adapter | Depend on `core/database` directly from the use case | Domain policy remains independent of Android/Room infrastructure and follows the repository boundary |
| 9 | Replace the floating compact status icon with an integrated top-right corner tag | Keep the approved-at-implementation floating `Surface`; add text; make the tag interactive | The revised visual contract makes status part of the card silhouette while preserving icon-only, informational, mutually exclusive behavior and semantic status colors |
| 10 | Use a right isosceles triangle for the integrated tag | Keep the quadrilateral/polygon whose diagonal ends at `Size.size32` along the bottom | Equal-length top/right legs and one diagonal joining their outer endpoints are the corrected approved geometry; the prior shape is explicitly rejected |
| 11 | Refine the regular MovieCard triangle to `Size.size56` legs and use tertiary Watchlist colors | Keep regular MovieCard at `Size.size64`; keep Watchlist on primary colors | The smaller regular-card treatment preserves the approved triangle while tertiary Watchlist improves blue distinction from Watched and dark-theme contrast |
| 12 | Keep regular cards at `Size.size56`/`Spacings.spacing8`, but use `Size.size64` and `Spacings.spacing8 + Size.size2` for Hero | Use one size/inset for both card types | The larger Hero artwork needs balanced icon space from both the diagonal and rounded card edge without changing the regular-card treatment |

---

## 14. Out of Scope / Follow-ups

- Quick Watchlist/Watched actions directly from Search cards.
- Shared status decoration on non-Search screens.
- Animation when a status changes.
- User-configurable status-tag placement, color, or visibility.
- Repair tooling for inconsistent legacy/synchronized dual-state records.

---

## 15. Handover Notes

- Do not add user-state fields to `MovieApiModel` or browse-cache serialization.
- The executor must report completed tasks and validation evidence to a planner; the executor must not edit this FIP.
- Search has multiple mapping/update paths; verify dashboard load, query load, filtering, refresh, pagination, stale data, and See All independently.
- Keep the latest status map separate from catalogue request IDs so a late network page cannot erase current local status.
- Constructor changes will affect broad Search and See All ViewModel test setup; update fixtures centrally where practical.
- Paparazzi baselines must use deterministic image behavior and should change only for intentional status-tag cases.
- The former floating compact `Surface` and its `Spacings.spacing8` outer edge inset are superseded. The approved integrated tag uses `Spacings.spacing8` only as internal icon spacing.
- Do not reuse the rejected quadrilateral path or treat its green snapshots as geometry approval; the tag must be a right isosceles triangle.
- The final visual contract uses `Size.size56`/`Spacings.spacing8` on regular MovieCard and `Size.size64`/`Spacings.spacing8 + Size.size2` on HeroMovieCard/Now Playing. Watched uses secondary container colors and Watchlist uses tertiary container colors.
- FIP-020 remains deferred; only feature-required semantics and screenshots belong here.

---

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 2.1     | 2026-08-18 | Closed FIP-021 after final Hero approval, green focused/global/Paparazzi/coverage gates, valid manual and TalkBack evidence, version bump to `1.0.33` (41), and verified signed release AAB with recorded size and SHA-256; release/push authorized by the user |
| 2.0     | 2026-08-18 | Approved Hero-only `Size.size64` triangle and 10dp-equivalent token inset while preserving regular MovieCard and status colors; reopened only affected Hero visual/snapshot/manual tasks and retained behavior, semantics, and no-release state |
| 1.9     | 2026-08-18 | Completed `Size.size56` and tertiary Watchlist visual/snapshot tasks, recorded green focused JVM/Paparazzi/quality/debug-build evidence, installed the refined APK for USER validation, and kept manual validation/version/bundle pending with no AAB or push |
| 1.8     | 2026-08-18 | Approved `Size.size56` triangle legs and tertiary Watchlist colors, reopened only affected size/color/snapshot/manual visual tasks, and preserved architecture, behavior, semantics, and no-release state |
| 1.7     | 2026-08-18 | Completed corrected three-point triangle geometry and refreshed Paparazzi tasks, recorded green debug/release JVM, Paparazzi, Search quality, and debug-build evidence, installed the updated APK for USER validation, and kept manual validation/version/bundle pending with no AAB or push |
| 1.6     | 2026-08-18 | Corrected the tag to a right isosceles triangle, rejected the bottom-ending quadrilateral, reopened only geometry/snapshot/manual visual tasks, and preserved valid architecture, behavior, semantics, and no-release evidence |
| 1.5     | 2026-08-18 | Completed revised Phase 4/5 corner-tag and Paparazzi tasks, recorded focused JVM/static-analysis/Paparazzi/debug-build evidence, installed the debug APK for USER validation, and kept manual validation, versioning, and release bundle pending |
| 1.4     | 2026-08-18 | Recorded the approved integrated top-right corner-tag revision, preserved completed architecture/behavior evidence, reopened affected Phase 4 UI and Phase 5 Paparazzi tasks, and required fresh Phase 6 snapshot/manual visual validation |
| 1.3     | 2026-08-18 | Recorded completed Phases 2-5, focused coverage and JVM/Paparazzi/semantics evidence, restored the historical Hero baseline, and kept all Phase 6 release gates pending |
| 1.2     | 2026-08-18 | Recorded the scoped snapshot repair and green debug/release JVM and Paparazzi baseline, completed Phase 1 evidence, resolved blocker 2, and authorized Phase 2 to begin |
| 1.1     | 2026-08-18 | Corrected the FIP-018 prerequisite to Phase 7, recorded green FIP-019 CI evidence, and blocked production on three pre-existing Search snapshot drifts pending an explicitly scoped baseline repair |
| 1.0     | 2026-08-14 | Initial draft for exclusive reactive Watched/Watchlist ribbons across Search movie cards |
