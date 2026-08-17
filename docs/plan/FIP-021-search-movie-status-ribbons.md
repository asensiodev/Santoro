# FIP — Search Movie Status Ribbons

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                    |
|------------------------|--------------------------------------------------------------------------|
| **FIP ID**             | FIP-021                                                                  |
| **Version**            | 1.0                                                                      |
| **Status**             | 🟡 Draft                                                                 |
| **PRD ref**            | Internal UX enhancement — no PRD feature                                 |
| **Feature**            | Exclusive Watched and Watchlist status ribbons across Search movie cards |
| **Date**               | 2026-08-14                                                               |
| **Author**             | @asensiodev                                                              |
| **Definition of Done** | All checkboxes are marked `[x]`, §10 is complete, and the next release version is ready |

---

> **Execution rule:** Work phase by phase, task by task. Report each completed task and its evidence to the planner.
> If anything is ambiguous or does not fit the plan — **stop and ask** before deciding.

> **Role rule:** The executor implements code and reports phase evidence without editing this FIP. A planner records that evidence, updates checkboxes, and completes §10.

> **Invariant:** A movie cannot be Watched and In Watchlist at the same time. Presentation must model one optional status and must never render two status ribbons on one movie.

---

## 0. Prerequisites

- Confirm the FIP-019 CI run is green before starting implementation.
- Read FIP-018 Phase 9 before adding the focused status repository; FIP-021 owns only the read capability required by this feature and must not mark broader FIP-018 tasks complete.
- Re-read `SearchMoviesViewModel`, `SeeAllMoviesViewModel`, `MovieUiMapper`, `MovieCard`, and `HeroMovieCard` before editing.
- Run the Search JVM and Paparazzi suites to establish a green baseline.
- Confirm no concurrent work has introduced equivalent movie-status decoration or a reusable status component.

---

## 1. Context & Motivation

Search currently presents movie artwork, title, genre, and rating without showing whether a movie is already Watched or In Watchlist. Users must open Movie Detail or switch tabs to discover that state.

The domain `Movie` already contains `isWatched` and `isInWatchlist`, and Room exposes reactive watched/watchlist flows. Search's `MovieUi` discards those flags, while TMDB and browse-cache results are not authoritative for user state. The feature must therefore decorate every Search-owned movie list with current local state rather than persisting user state into browse cache.

Compact, non-interactive icon ribbons provide useful at-a-glance context while preserving the existing card click behavior. The same treatment must appear across the Search dashboard, Hero card, query results, filtered results, pagination, and See All screens to avoid inconsistent state.

---

## 2. Goals

- Show one compact Watched ribbon when a Search movie is Watched.
- Show one compact Watchlist ribbon when a Search movie is In Watchlist.
- Show no ribbon when a movie has neither status.
- Make it structurally impossible for one card to display both ribbons.
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
- No redesign of Search cards, Hero layout, Movie Detail actions, Watchlist, or Watched screens.
- No new destination, navigation route, analytics event, notification, or synchronization policy.
- No broad extraction of a shared cross-feature badge system unless an immediate second production consumer is identified.
- No broad `DatabaseRepository` capability split or migration of unrelated consumers from FIP-018.
- No FIP-020 test or accessibility work beyond coverage required for this feature.
- No Play Console publishing or signing-process change; this FIP prepares the next release artifact and version only.

---

## 4. User Stories

| ID    | As a… | I want to… | So that… | Acceptance Criteria |
|-------|-------|------------|----------|---------------------|
| US-01 | user | see that a Search movie is already Watched | I do not need to open Movie Detail to check | Every Search-owned card for a Watched movie shows exactly one Watched ribbon |
| US-02 | user | see that a Search movie is already in my Watchlist | I can avoid saving or checking it again | Every Search-owned card for a Watchlist movie shows exactly one Watchlist ribbon |
| US-03 | user | see status update after changing it in Movie Detail | Search remains trustworthy without a refresh | Returning to Search reflects the latest Room state while preserving loaded content and position |
| US-04 | assistive-technology user | hear the movie's current status | the visual ribbon is not the only source of information | The ribbon exposes the localized `Watched` or `In Watchlist` description |

---

## 5. UX / Flows

### Screen: Search Movies

| State | What the user sees | User actions available |
|-------|--------------------|------------------------|
| Loading | Existing loading UI; no status placeholder | Existing behavior |
| Dashboard content | Existing Hero and section cards with zero or one top-end status ribbon | Tap card → Movie Detail; existing See All actions |
| Query/filter content | Existing result cards with zero or one top-end status ribbon | Tap card → Movie Detail; existing search/filter actions |
| Empty / No results | Existing empty UI; no ribbon | Existing actions |
| Error | Existing error UI; no new status-specific error | Existing Retry behavior |
| Stale content | Existing stale movie content decorated with the latest available local status | Existing actions |

### Screen: See All Movies

| State | What the user sees | User actions available |
|-------|--------------------|------------------------|
| Loading | Existing loading UI | Existing behavior |
| Content | Existing paginated cards with zero or one top-end status ribbon | Tap card → Movie Detail; load more |
| Empty | Existing empty UI | Existing actions |
| Error | Existing error UI; no new status-specific error | Existing Retry behavior |

### Ribbon Contract

| Movie status | Visual | Accessibility | Interaction |
|--------------|--------|---------------|-------------|
| None | No ribbon | No additional status description | Card remains clickable |
| Watchlist | `AppIcons.Watchlist` on `primaryContainer` with `onPrimaryContainer` content | Existing localized `In Watchlist` label | Informational only |
| Watched | `AppIcons.Watched` on `secondaryContainer` with `onSecondaryContainer` content | Existing localized `Watched` label | Informational only |

- Render an icon-only `Surface` using `MaterialTheme.shapes.small`; no visible text appears inside the ribbon.
- Use `Size.size18` for the icon and `Spacings.spacing4` for internal padding.
- Position the ribbon at the card top end with `Spacings.spacing8` edge padding without covering Hero title/rating content.
- Use only design-system size, shape, spacing, and Material color tokens; add no raw `.dp` or hardcoded user-facing strings.
- A card accepts one nullable status value, not two independently renderable flags.

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
MovieCard / HeroMovieCard → MovieStatusRibbon
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

- `null` means neither status and renders no ribbon.
- The model has no representation capable of rendering both statuses.
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
- ❌ Forbidden:
    - Change production behavior, screenshots, or version metadata.
    - Treat TMDB or browse cache as authoritative user-state sources.

- [ ] Run Search debug/release JVM tests and record the baseline.
- [ ] Run current MovieCard and HeroMovieCard Paparazzi verification and record the baseline.
- [ ] Confirm every Search-owned card surface: dashboard sections, Hero, query results, filtered results, pagination, and See All.
- [ ] Confirm Movie Detail preserves Watched/Watchlist exclusivity and document that Room/sync do not enforce it structurally.
- [ ] Confirm existing `Watched`, `In Watchlist`, `AppIcons.Watched`, and `AppIcons.Watchlist` resources are suitable.
- [ ] Record any pre-existing failure as a blocker rather than weakening planned assertions.

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

- [ ] Add the internal exclusive `MovieLibraryStatus` model.
- [ ] Add a pure Search-domain `MovieLibraryStatusRepository` interface exposing watched and watchlist ID flows.
- [ ] Add `DefaultMovieLibraryStatusRepository` in the Search data layer to adapt the existing `DatabaseRepository` movie flows to ID sets.
- [ ] Add `ObserveMovieLibraryStatusesUseCase` using only the Search-domain repository interface.
- [ ] Emit `Watchlist` for IDs present only in Watchlist and `Watched` for IDs present in Watched.
- [ ] Defensively normalize an ID present in both sets to `Watched` while preserving the one-status invariant.
- [ ] Emit a combined map only when both latest source results are successful; emit failure when either latest source result fails.
- [ ] Allow a later pair of successful source emissions to recover after failure.
- [ ] Propagate expected failures as `Result.failure` and rethrow `CancellationException` rather than wrapping it.
- [ ] Bind/provide the data implementation and use case through the existing Search Hilt module without unnecessary scope.

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

- [ ] Add one nullable `libraryStatus` property to `MovieUi`.
- [ ] Update mapping helpers and fixtures so absent local status maps to `null`.
- [ ] Inject and collect the observer in `SearchMoviesViewModel` using existing coroutine/lifecycle conventions.
- [ ] Apply status to every dashboard, query, filtered, and paginated list in `SearchMoviesUiState`.
- [ ] Update already loaded Search lists immediately when Room emits a changed status map.
- [ ] Inject and collect the same observer in `SeeAllMoviesViewModel`.
- [ ] Apply status to See All initial, refreshed, and appended pages without losing pagination state.
- [ ] Retain only the latest complete successful combined map after a later observer failure; use no ribbons before the first complete success.
- [ ] Ensure logout/database clear emissions remove all visible ribbons without a catalogue refresh.

### Phase 4 — Exclusive Ribbon UI

**Data sources**
- `MovieUi.libraryStatus` only.
- Existing localized status strings.
- Existing design-system icons and Material/design tokens.

**Side effects**
- ✅ Allowed:
    - Render one non-interactive ribbon overlay.
    - Expose localized status semantics.
- ❌ Forbidden:
    - Mutate movie state from a ribbon.
    - Add independent Watched and Watchlist booleans to the ribbon API.
    - Add raw dimensions, hardcoded strings, or non-token colors.

- [ ] Add an internal icon-only `MovieStatusRibbon(status)` composable using a small `Surface`, `Size.size18`, `Spacings.spacing4`, exhaustive status branches, and `@PreviewLightDark` coverage.
- [ ] Render Watchlist with `AppIcons.Watchlist`, `primaryContainer`, and `onPrimaryContainer`.
- [ ] Render Watched with `AppIcons.Watched`, `secondaryContainer`, and `onSecondaryContainer`.
- [ ] Reuse `watchlist_icon_button_added` and `watched_icon_button_marked` accessibility labels.
- [ ] Overlay the ribbon at the top end of `MovieCard` with `Spacings.spacing8` edge padding for poster, placeholder, dashboard, query, and See All usage.
- [ ] Overlay the ribbon at the top end of `HeroMovieCard` with the same edge padding without obscuring title or rating content.
- [ ] Preserve the existing card click target, minimum touch target, shape, loading, image, and placeholder behavior.
- [ ] Verify compact layout in light/dark themes and English/Spanish resources.

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

- [ ] Add 100% use-case coverage for none, Watched, Watchlist, changed emissions, both-set normalization, expected failure, and cancellation.
- [ ] Add repository implementation coverage for ID mapping, watched/watchlist source failures, recovery, and cancellation.
- [ ] Update mapper tests for the nullable exclusive status property.
- [ ] Add Search ViewModel tests proving already loaded dashboard/query/filter/pagination content updates reactively.
- [ ] Add Search ViewModel tests proving state, query, filter, pagination, and content survive status changes and failures.
- [ ] Add See All ViewModel tests for initial pages, appended pages, reactive changes, and status-observer failure.
- [ ] Add Compose semantics assertions for None, Watched, and Watchlist, including exactly one status description per card.
- [ ] Add focused light/dark Paparazzi cases for MovieCard and HeroMovieCard in None, Watched, and Watchlist states.
- [ ] Confirm no test or preview can construct a card with two simultaneously rendered ribbons.

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

- [ ] Run Search debug and release JVM suites from a clean invocation.
- [ ] Run Search Paparazzi verification and inspect only intentional changed baselines.
- [ ] Run `./gradlew ktlintCheck detekt test assembleDebug`.
- [ ] Run `./gradlew :koverXmlReport :koverHtmlReport :koverVerify` without changing thresholds.
- [ ] Manually verify dashboard, Hero, query, filter, pagination, See All, and return-from-Detail behavior on an emulator.
- [ ] Verify None, Watched, and Watchlist in light/dark mode and confirm no card displays two ribbons.
- [ ] Verify TalkBack announces the movie and its single status without introducing an extra action.
- [ ] Increment to the next available patch version and version code only after all checks pass.
- [ ] Run `./gradlew bundleRelease` with the existing release keystore properties and verify the signed `app/build/outputs/bundle/release/app-release.aab` artifact.
- [ ] If release credentials are unavailable, record a blocker and do not claim release readiness or accept an unsigned AAB as complete.
- [ ] Provide exact commands, versions, environment, results, and skipped checks to the planner for §10.

---

## 10. Validation

| What | Result | Notes |
|------|--------|-------|
| Search JVM baseline | ⏳ | Record debug/release commands and counts |
| Paparazzi baseline | ⏳ | Record exact verification task and existing cases |
| Use-case and ViewModel behavior | ⏳ | Record exclusive/reactive/error/cancellation results |
| MovieCard and Hero ribbons | ⏳ | Record None, Watched, and Watchlist visual/semantics results |
| Search and See All consistency | ⏳ | Record all surfaces and pagination/return behavior |
| Exclusivity invariant | ⏳ | Confirm no movie displays two ribbons |
| Static analysis and build | ⏳ | Record exact command |
| Aggregate Kover verification | ⏳ | Record lines/branches without threshold changes |
| Manual emulator check | ⏳ | Record emulator and API level |
| Accessibility check | ⏳ | Record TalkBack result |
| Release preparation | ⏳ | Record previous/new version code/name and artifact result |

---

## 11. Blockers

| # | Blocker | Raised | Resolved | Impact |
|---|---------|--------|----------|--------|
| 1 | FIP-019 CI run `31823938280` is still in progress | 2026-08-14 | Open | Phase 1 must not start until the run completes successfully |

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Can one movie display both Watched and Watchlist ribbons? | No. The product behavior is mutually exclusive, the UI accepts one optional status only, and defensive normalization protects against unconstrained persisted/synced input |
| 2 | Which Search surfaces receive ribbons? | All Search-owned movie cards: dashboard sections, Hero, query/filter results, pagination, and See All |
| 3 | Can users toggle status from the ribbon? | No. Ribbons are informational; Movie Detail remains the state-management surface |
| 4 | What happens if local status observation fails? | Preserve catalogue content and the latest successful status map; show no ribbon if no successful map exists |
| 5 | What version is released? | Increment the current values to the next available patch version/code after validation; do not hardcode planning-time values |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Use one nullable status instead of two UI booleans | Add `isWatched` and `isInWatchlist` to `MovieUi` | The type prevents simultaneous ribbons and directly encodes the product invariant |
| 2 | Observe Room through one Search-owned use case | Trust TMDB flags; persist flags in browse cache; inject two raw flows into each Composable | Room is authoritative, the join is business policy, and Composables remain presentation-only |
| 3 | Enrich existing lists in both ViewModels | Reload Search after returning from Detail | Reactive enrichment updates immediately without network work or state loss |
| 4 | Apply ribbons to all Search-owned card surfaces | Query results only; poster cards only | Consistent state avoids contradictory cards for the same movie |
| 5 | Keep ribbons informational | Add quick-toggle actions | This feature communicates state without changing interaction, sync, or destructive-action behavior |
| 6 | Keep the component internal to Search | Add a design-system component immediately | There is one current consumer; premature sharing would add unnecessary API surface |
| 7 | Normalize dual-membership input to Watched | Render two ribbons; crash; choose whichever flow emits last | Movie Detail preserves exclusivity, but Room/sync do not enforce it structurally; normalization guarantees one-status rendering |
| 8 | Add a Search-domain repository interface with a Search-data adapter | Depend on `core/database` directly from the use case | Domain policy remains independent of Android/Room infrastructure and follows the repository boundary |

---

## 14. Out of Scope / Follow-ups

- Quick Watchlist/Watched actions directly from Search cards.
- Shared status decoration on non-Search screens.
- Animation when a status changes.
- User-configurable ribbon placement, color, or visibility.
- Repair tooling for inconsistent legacy/synchronized dual-state records.

---

## 15. Handover Notes

- Do not add user-state fields to `MovieApiModel` or browse-cache serialization.
- The executor must report completed tasks and validation evidence to a planner; the executor must not edit this FIP.
- Search has multiple mapping/update paths; verify dashboard load, query load, filtering, refresh, pagination, stale data, and See All independently.
- Keep the latest status map separate from catalogue request IDs so a late network page cannot erase current local status.
- Constructor changes will affect broad Search and See All ViewModel test setup; update fixtures centrally where practical.
- Paparazzi baselines must use deterministic image behavior and should change only for intentional ribbon cases.
- FIP-020 remains deferred; only feature-required semantics and screenshots belong here.

---

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-14 | Initial draft for exclusive reactive Watched/Watchlist ribbons across Search movie cards |
