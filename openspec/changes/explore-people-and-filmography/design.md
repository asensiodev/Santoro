## Context

See proposal.md for motivation and scope. Relevant observed code:

- `MovieDetailScreen.kt`: `CastMemberItem` and `CrewMemberItem` are informational; neither receives a person click callback.
- `CastMemberUi` retains a person ID and a separate credit ID. `CrewMember` retains person ID, but `CrewMemberUi` currently drops it.
- `MainActivity.kt`: `SantoroApp` owns the main Navigation Compose host and registers movie-detail destinations.
- Existing project boundaries require feature `api`/`impl`, pure domain contracts, Hilt wiring, immutable MVI state, and design-system tokens.

The graph checks for these paths report no recorded coverage gaps. App navigation was read directly after a graph name search returned insufficient matches. This is task-directed evidence, not a complete architecture audit.

## Goals / Non-Goals

**Goals:** One person identity and one profile destination for actors and crew; reuse existing movie detail, networking, lifecycle collectors, and UI patterns.

**Non-Goals:** Person persistence, offline profile guarantees, changes to tracked movie ownership or account isolation, a custom navigation framework, or a new reusable card abstraction without concrete reuse.

## Decisions

### Data boundaries

Add `feature/person-detail/api` for a serializable person route and `impl` for service, repository implementation, Hilt bindings, ViewModel, and Compose UI. Put person/credit values and repository contract in `core/domain`, following Presentation → Domain ← Data. Reuse existing Retrofit configuration and TMDB image URL policy. DTOs stay at the data boundary. Extend movie-detail crew mapping to retain person ID.

Alternative: a separate actor feature and director feature duplicates the same identity and profile data. A shared person feature handles both, including a person who acts and directs.

### Data sources and side effects

- Read-only TMDB `GET /3/person/{person_id}` and `GET /3/person/{person_id}/movie_credits`, using the existing API authentication and current language parameter. Sources: https://developer.themoviedb.org/reference/person-details and https://developer.themoviedb.org/reference/person-movie-credits.
- Allowed: these HTTP reads, image requests, in-memory state updates, and app navigation.
- Forbidden: Room migrations or writes for person browsing, Firebase/auth changes, adding movies to lists by opening a profile, external biography providers, scraping, or telemetry changes for this feature.
- Load details first; after success load credits into a separately retryable section. This keeps retry authority simple and the biography usable when credits fail. No second-language biography request in this pilot.

### UI composition

Use a scrollable person screen with Back, portrait/name/facts, expandable biography, and acting/crew filmography sections. Movie rows show poster, title, year, and character/job labels. Deduplicate by movie ID per section and join distinct role labels; preserve acting and crew distinctions across sections. Use descending release dates, undated last, then ID for stable ordering. Use existing image placeholders and locale formatting. Server-provided role labels can remain TMDB content; UI labels require English and Spanish resources.

Alternative: popularity sorting plus filters introduces choices and extra controls before the basic discovery path is useful. Date ordering supplies a predictable first version. Poster rows make filmography easier to scan without redesigning other screens.

### State, cancellation, and navigation

Use the existing MVI process(intent) pattern with immutable profile and filmography state. Initialize with `LaunchedEffect(viewModel)` and collect render state with lifecycle awareness. Screen-bound requests run in ViewModel scope, use injected dispatchers where required, preserve cancellation, and avoid duplicate initialization or overlapping retry jobs.

Pass person ID between destinations; never identify a person by name or credit ID. Register person → movie and movie → person callbacks in the app composition root. Follow the existing RESUMED navigation guard and effect collection conventions to suppress repeated taps. Keep each back-stack entry's ViewModel separate, with saveable scroll and biography expansion state. Configuration recreation must keep loaded content; process recreation may reload from the route ID.

### Validation

Test mappings, multi-role grouping, stable ordering, empty/missing fields, success/failure/retry/cancellation, and duplicate initialization. Verify cast and director click semantics and round-trip navigation with existing Compose/journey patterns. Add previews and Paparazzi coverage for content, missing biography, and filmography failure in light/dark themes. Run focused tests before repository aggregate gates. Manual checks cover an actor/director, Spanish missing biography, credits failure, rotation, process recreation, and rapid taps.

## Risks / Trade-offs

- Missing localized biography is reachable for lesser-known people and some Spanish profiles; users lose context but can still browse movies → accept an explicit empty message rather than translation or a fallback-request workflow.
- Large filmographies are reachable for prolific actors; rendering every row eagerly would slow scrolling → use lazy layouts and stable movie keys. The movie credits endpoint is a single response; do not invent server pagination.
- Repeated A → P → A exploration grows the back stack under deliberate repeated browsing → accept ordinary Android back-stack semantics for the pilot; verify restoration without adding cycle-pruning behavior.
- The first offline visit cannot retrieve a person → use existing error/retry UI; persistent person caching is outside this pilot.
- Existing FIP-023 manual/external gates remain open → do not imply this read-only pilot completes account validation or makes the release ready.

## Migration Plan

No database or account migration. Add modules, resources, and routes; keep existing movie and deep-link routes compatible. Roll back by removing the person route wiring and click affordances together with the new feature modules. Archive only after implementation, focused/aggregate validation, and required manual evidence; human archive review remains required by the OpenSpec workflow in `docs/README.md`.

## Approved profile polish

Use TMDB known_for_department to put Acting first for actors and Behind the camera first for other known departments. Unknown department defaults to Acting first; omit empty sections. Retain the existing release-date ordering. Each profile section previews at most eight horizontal poster cards with title, year and role labels, a count, and See all only when additional movies exist. The full filmography is a separate lazy vertical list with movie navigation and Back. No server or numbered pagination is needed for the single credits response.

Share the profile back-stack entry's ViewModel with its filmography destination. Navigation owns data lifetime; Compose saveable lazy state owns scroll and biography expansion. Restoring a process may reload from person identity.

The user approved the existing testing seams: repository HTTP contract, plain state-driven Compose screens, app navigation journeys and deterministic light/dark screenshots. Use MockK for isolated repository responses in ViewModel tests, MockWebServer for HTTP integration and the existing journey fake for deterministic Hilt app data. Add no new fake class. Implement one observed behavioral red/green slice at a time; interface declarations alone are not behavioral red evidence.

## Approved year grouping

The complete chronological filmography uses subtle accessible year headings for both acting and crew. Cards omit the repeated year; horizontal profile previews retain it. Movies without a release date appear last under a localized No release date / Sin fecha heading. Preserve release-date order, roles, movie callbacks and stable lazy keys. No alternate sorting controls are introduced.

Reuse the already approved plain state-driven Compose and deterministic screenshot boundaries. Verify a single heading for multiple movies in one year, descending section order, singleton crew years and undated entries, and existing click/state contracts. Record behavioral red before implementation and inspect the updated complete-list light/dark snapshots before independent verification.

## Cast scroll restoration regression

The user confirmed the app journey boundary on 2026-10-07: scroll the movie's cast, open an actor, then press Back and retain the actor's position. The regression uses thirteen cast entries and a delayed repository response so the normal Loading transition occurs when returning. It asserts both visibility and the actor's position in the root after content returns.

Returning re-runs the movie fetch and temporarily removes Content. The cast row's locally owned lazy state was lost during that transition. `MovieDetailScreen` now owns the saveable `LazyListState` outside the Loading/Content branches and passes it to the cast row. Fetch behavior remains intact, including refreshing movie status from the repository.

Validation uses the managed `gradle-run.py` workflow `e4ba00650206214e030b6f887722d11b`, JDK 21, `-Pkotlin.compiler.execution.strategy=in-process`, and `-PenableFirebase=false`. The focused command is `:app:connectedJourneyTestAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.santoro.MainActivityPersonExplorationJourneyTest#givenScrolledCast_whenReturningFromActor_thenPositionIsRetained`.

- Behavioral red (run 0003): the actor was not displayed after Back, on Pixel_9a API 37.
- Green (run 0004): the same test passed after hoisting state, including exact position equality.
- Setup failures are separate: run 0001 used the obsolete app Debug test task; run 0002 exposed a test selector that depended on a scrolled-away actor. Run 0005 stopped on a new test line's formatting, which was corrected.
- Run 0006 found one additional call-formatting violation, which was corrected. Run 0007 completed the module tests but the instrumentation process died during the journey suite without a behavioral assertion. Run 0008 passed on retry: seven person exploration journeys and 61 movie-detail JVM tests, including screenshot cases, with Paparazzi verification and both modules' ktlint/detekt checks successful.

Run 0008 used these Gradle tasks and filter through the wrapper:

```text
:app:connectedJourneyTestAndroidTest
-Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.santoro.MainActivityPersonExplorationJourneyTest
:feature:movie-detail:impl:testDebugUnitTest
:feature:movie-detail:impl:verifyPaparazziDebug
:feature:movie-detail:impl:ktlintCheck
:feature:movie-detail:impl:detekt
:app:ktlintCheck
:app:detekt
```

Verification questions and bounded answers:

- "Does the cast round-trip journey retain its scrolled position before the fix?" No: run 0003 reproduced the reported failure.
- "Does the cast round-trip journey retain its scrolled position after hoisting its state?" Yes: run 0004 passed.
- "Do all person exploration journeys and affected movie-detail unit, screenshot and static checks pass?" Yes: run 0008 passed after the separate formatting and instrumentation interruptions recorded above.

Process recreation and the pilot's existing manual/release gates remain outside this bug fix's validation.
