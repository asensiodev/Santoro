# FIP — Architecture Reliability Refactor

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                 |
|------------------------|-----------------------------------------------------------------------|
| **FIP ID**             | FIP-018                                                               |
| **Version**            | 1.0                                                                   |
| **Status**             | 🔵 In Progress                                                        |
| **PRD ref**            | Internal refactor — no PRD feature                                    |
| **Feature**            | Correctness, lifecycle, SOLID, and quality-enforcement improvements   |
| **Date**               | 2026-07-12                                                            |
| **Author**             | @asensiodev                                                           |
| **Definition of Done** | All checkboxes in all phases marked `[x]` and validation completed    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

> **Priority rule:** Correctness and user-visible reliability take precedence over structural cleanup.
> Do not begin a lower-priority phase while a higher-priority phase has unresolved regressions.

---

## 0. Prerequisites

- Read [FIP-006 — MVI Migration](./FIP-006-mvi-migration.md) before changing presentation contracts.
- Read [FIP-016 — Unified ScreenState Pattern Migration](./FIP-016-unified-screenstate-migration.md) before changing screen states.
- Run the existing affected-module test tasks before each phase to establish a baseline.
- Preserve unrelated working-tree changes.

---

## 1. Context & Motivation

Santoro has a coherent Clean Architecture-inspired structure and a pragmatic MVI-style presentation layer. The shared domain is pure Kotlin/JVM, data models are mapped at boundaries, feature behavior is generally expressed through use cases, and Compose observes immutable state.

The current architecture does not require a broad rewrite. The audit identified targeted reliability and maintainability issues that should be fixed without replacing the established architecture:

1. Repeated intents can create duplicate long-lived collectors in Watchlist and Movie Detail.
2. Concurrent search/fetch operations can complete out of order and overwrite newer state.
3. One-off effects are collected by composition rather than explicitly by lifecycle, and buffered delivery semantics are not defined consistently.
4. Mutation failures are sometimes silent, ignored, or represented by raw exception messages.
5. Broad exception handling can convert coroutine cancellation into ordinary failure.
6. `DatabaseRepository` exposes unrelated capabilities to feature consumers.
7. Presentation sometimes depends directly on concrete infrastructure such as WorkManager scheduling or cache implementation.
8. `SearchMoviesViewModel` has accumulated several independently changing responsibilities.
9. Process-death restoration is partial for meaningful user input and pending navigation context.
10. Documented quality expectations such as coverage targets and custom Detekt policy are not fully enforced.

These issues are more important than making Santoro conform to a textbook architecture label. The plan therefore prioritizes observable correctness, explicit contracts, focused interfaces, and enforceable quality controls.

---

## 2. Goals

- Guarantee one active long-lived observation pipeline per screen instance.
- Guarantee latest-request-wins behavior where user input supersedes previous work.
- Define and implement lifecycle-aware, testable one-off effect delivery.
- Preserve coroutine cancellation across all affected repository and ViewModel paths.
- Provide localized, user-visible failure behavior for mutations and retries.
- Segregate broad repository capabilities by consumer without changing persistence technology.
- Remove direct concrete infrastructure dependencies from affected ViewModels.
- Reduce `SearchMoviesViewModel` responsibilities without introducing unnecessary classes or changing user behavior.
- Restore meaningful user-entered state after process recreation where recreation is not sufficient.
- Make Detekt, architecture, and coverage claims verifiable in local and CI workflows.
- Add regression tests for every corrected race, lifecycle contract, and failure path.

---

## 3. Non-Goals

- Do not split feature domain code into additional JVM or layer modules.
- Do not change the existing feature `api`/`impl` module strategy.
- Do not hide feature implementation navigation registration from the `app` composition root.
- Do not convert the presentation layer to strict reducer/store-based MVI.
- Do not add intent queues, middleware, mutation classes, reducers, or event replay merely for architectural purity.
- Do not replace Hilt, Compose Navigation, Room, Retrofit, WorkManager, Firebase, or the current cache technology.
- Do not redesign product flows, visual design, navigation destinations, or backend contracts.
- Do not introduce compatibility layers for behavior that has not shipped or persisted.
- Do not refactor unaffected features solely for consistency.
- Do not treat numerical coverage as a substitute for behavioral assertions.

---

## 4. User Stories

| ID    | As a… | I want to… | So that… | Acceptance Criteria |
|-------|-------|------------|----------|---------------------|
| US-01 | user | retry a failed screen safely | repeated retries do not duplicate work or state updates | At most one relevant upstream collector remains active after any retry sequence |
| US-02 | user | receive results for my latest query | stale searches do not replace current results | Older work is cancelled or ignored deterministically |
| US-03 | user | receive navigation and feedback only while the screen is active | stale events do not execute after I leave and return | Effect lifecycle and buffering behavior is explicit and tested |
| US-04 | user | understand when a mutation fails | I can recover instead of seeing silent failure | Failed remove/toggle operations show localized feedback and preserve consistent state |
| US-05 | user | return after process recreation without losing meaningful input | I can continue the current task | Selected restorable input is recovered and derived data is reloaded |
| US-06 | developer | depend only on capabilities my feature uses | changes have smaller impact and tests require smaller fakes | Affected consumers use focused domain-facing interfaces/use cases |
| US-07 | developer | trust local and CI quality claims | regressions are detected before merge | Detekt config, architecture rules, coverage reports, and CI tasks are demonstrably wired |

---

## 5. UX / Flows

No visual redesign is planned. Existing Loading, Content, Empty, and Error presentation remains.

### Retry and Query Behavior

| Situation | Expected behavior |
|-----------|-------------------|
| User retries once or repeatedly | One observation/fetch pipeline is active; no duplicate emissions are produced |
| User enters query A and then query B | Work for A is cancelled or ignored; only B can become the final visible result |
| Screen leaves the active lifecycle | Transient effects are not executed by an inactive screen |
| Screen returns to the active lifecycle | Stale navigation/share effects are not replayed unless the product contract explicitly requires it |

### Mutation Failure Behavior

| Situation | Expected behavior |
|-----------|-------------------|
| Watchlist removal fails | Item remains/restores consistently and localized retryable feedback is shown |
| Watched/Watchlist toggle fails | UI remains consistent with persisted state and localized feedback is shown |
| Sync scheduling fails after a successful local write | Local success is retained; scheduling failure is observable and does not falsely report mutation failure |

---

## 6. Architecture

The established pragmatic MVI-style flow remains:

```text
Compose UI
    │ sends sealed Intent
    ▼
ViewModel.process(intent)
    │ invokes focused Use Case
    ▼
Domain-facing capability
    │ implemented by data/infrastructure
    ▼
Result / Flow
    │ cancellation and latest-work policy
    ▼
MutableStateFlow.update
    │ exposes immutable UiState
    ▼
Lifecycle-aware Compose rendering

Transient Effect Channel/Flow
    │ explicit lifecycle delivery policy
    ▼
Navigation / share / user feedback
```

Target dependency improvement:

```text
Before:
Watchlist ViewModel ──→ WorkManagerSyncScheduler
Search ViewModel ─────→ CachingSearchMoviesRepository
Feature use cases ────→ broad DatabaseRepository

After:
Watchlist ViewModel ──→ ScheduleMovieSyncUseCase ──→ SyncScheduler contract
Search ViewModel ─────→ Cache maintenance use case/capability
Feature use cases ────→ focused Watchlist/Watched/MovieState capability
```

No reducer/store layer is introduced. Ordering is made explicit only at asynchronous boundaries that can race.

---

## 7. Data Model

No Room entity, Firestore document, API DTO, or backend schema change is expected.

Possible presentation-only saved-state keys may be added for:

- Watchlist query.
- Search filter/input that cannot be cheaply inferred from navigation or repositories.
- Pending deep-link movie ID while authentication is in progress, if current navigation restoration does not already cover the tested scenario.

Do not save derived movie lists, repository results, throwable instances, effects, or large UI models.

---

## 8. Modules Affected

Expected modules, subject to each phase's findings:

- `feature/watchlist/impl`
- `feature/movie-detail/impl`
- `feature/search-movies/impl`
- `feature/watched-movies/impl`
- `feature/settings/impl` only if shared error/restoration behavior requires it
- `core/domain`
- `core/database`
- `core/sync`
- `core/ui`
- `core/string-resources`
- `core/testing`
- `core/data` where dispatcher/error contracts are implemented
- `architecture-tests`
- `build-logic/convention`
- `.github/workflows/ci.yml`
- `build.gradle.kts`
- `config/detekt/detekt.yml`
- `hooks/pre-commit` only if local/CI parity requires it

No new feature, API, implementation, or layer module is planned.

---

## 9. Phases & Tasks

### Phase 1 — P0: Establish Regression Tests and Concurrency Contracts

**Data sources**
- Existing ViewModel intents and state.
- Existing repository/use-case Flows and `Result` values.
- Controlled test dispatchers, virtual time, Turbine, and existing test doubles.

**Side effects**
- ✅ Allowed:
    - Execute existing use cases through fakes/mocks.
    - Count subscriptions, cancellations, calls, state emissions, and effects in tests.
- ❌ Forbidden:
    - Production code changes before failing regression tests demonstrate each targeted defect.
    - Real network, Room, Firebase, or WorkManager operations in unit tests.
    - Tests that depend on wall-clock time or arbitrary delays.

- [x] Add a Watchlist test proving repeated `LoadMovies`/retry does not create multiple database subscriptions.
- [ ] Add a Watchlist test proving repeated `LoadMovies` does not create multiple query observers.
- [x] Add a Watchlist latest-query-wins test where query A completes after query B.
- [x] Add a Movie Detail test proving retry replaces or reuses the active fetch rather than adding a collector.
- [ ] Add rapid-toggle tests defining the accepted behavior while a mutation is pending.
- [ ] Add cancellation tests proving affected repositories/use cases do not convert cancellation into `Result.failure`.
- [ ] Add effect tests defining whether each navigation/share/error effect is delivered, dropped, or retained across collector inactivity.
- [ ] Record the accepted concurrency/effect contracts in test names and this FIP's Decisions section during implementation.

### Phase 2 — P0: Make Long-Lived Observation Idempotent

**Data sources**
- Watchlist Room-backed Flow from the existing use case.
- Watchlist query from existing `UiState` or a private ViewModel Flow.
- Movie ID from the existing typed navigation route/ViewModel fetch intent.
- Movie Detail Flow from the existing use case/repository.

**Side effects**
- ✅ Allowed:
    - Cancel and replace ViewModel-owned jobs.
    - Use `debounce`, `distinctUntilChanged`, `flatMapLatest`, `collectLatest`, or idempotent setup guards.
    - Reinvoke existing use cases.
- ❌ Forbidden:
    - New data sources, repositories, network calls, or database queries beyond existing behavior.
    - A global reducer/store or serialized MVI framework.
    - Restarting successful long-lived observation solely because the user presses Retry.

- [x] Refactor Watchlist so observer setup is idempotent for the ViewModel lifetime.
- [x] Separate initial observation setup from retrying a failed upstream operation.
- [x] Implement explicit latest-query-wins semantics for Watchlist search.
- [x] Ensure the unfiltered Room stream and filtered search stream cannot race to publish contradictory movie lists.
- [x] Retain/cancel the active Movie Detail fetch job or model route IDs through a latest-only Flow.
- [ ] Ensure tooltip checks and other success follow-ups execute once per accepted detail result.
- [x] Preserve current Loading, Content, Empty, NoResults, and Error behavior.
- [ ] Make all Phase 1 concurrency tests pass.

### Phase 3 — P0: Serialize User Mutations and Preserve State Consistency

**Data sources**
- Current movie state from immutable `UiState`.
- Existing add/remove/toggle use cases.
- Existing Room-backed source-of-truth emissions.

**Side effects**
- ✅ Allowed:
    - Disable a per-item action while its mutation is pending.
    - Serialize commands per movie or retain/cancel an operation job where product semantics permit.
    - Perform optimistic state updates only with explicit rollback behavior.
- ❌ Forbidden:
    - Parallel duplicate mutations for the same movie/action.
    - Treating sync scheduling failure as failure of an already successful local mutation.
    - Adding backend APIs or changing persistence schemas.

- [ ] Choose and document pessimistic, optimistic-with-rollback, or serialized mutation behavior for Watchlist removal.
- [ ] Choose and document the same behavior for Movie Detail watched/watchlist toggles.
- [ ] Prevent rapid repeated taps from issuing duplicate operations based on stale state.
- [ ] Keep dialog/pending state consistent until mutation success or failure is resolved.
- [ ] Separate local mutation outcome from subsequent sync-scheduling outcome.
- [ ] Add tests for success, failure, repeated tap, cancellation, and sync-scheduling failure.

### Phase 4 — P0: Preserve Structured Cancellation

**Data sources**
- Existing coroutine exceptions from repositories, data sources, and use cases.
- Existing `Result<T>` contracts.

**Side effects**
- ✅ Allowed:
    - Narrow exception catches.
    - Rethrow `CancellationException` before mapping expected failures.
    - Preserve the original throwable as an observability cause.
- ❌ Forbidden:
    - Catching `Throwable` as a generic recovery mechanism.
    - Converting cancellation into user-visible error state.
    - Changing API/backend error payloads.

- [ ] Audit production `catch (Exception)` and Flow `.catch` blocks in affected data, sync, and feature paths.
- [ ] Rethrow cancellation consistently before converting failures to `Result`.
- [ ] Narrow catches to expected transport, database, mapping, or authentication failures where practical.
- [ ] Preserve diagnostic causes while exposing stable domain/presentation errors.
- [ ] Add cancellation regression tests with controlled test dispatchers.
- [ ] Extend architecture/static checks only if a reliable non-textual rule can prevent recurrence.

### Phase 5 — P1: Define Lifecycle-Aware Effect Delivery

**Data sources**
- Existing ViewModel effect streams.
- Existing screen lifecycle and navigation back-stack state.
- Existing user intents that cause navigation, share, and error feedback.

**Side effects**
- ✅ Allowed:
    - Collect effects through `repeatOnLifecycle` or a focused reusable lifecycle-aware collector.
    - Change Channel capacity/type only after tests define required semantics.
    - Convert an effect to durable state only when it must survive collector inactivity or process recreation.
- ❌ Forbidden:
    - Replaying stale navigation or platform actions without an explicit product requirement.
    - Creating a global event bus.
    - Converting all effects into state mechanically.
    - Standardizing direct navigation callbacks solely for stylistic consistency when no bug exists.

- [ ] Inventory every feature effect and classify it as navigation, platform action, user feedback, or durable state transition.
- [ ] Define drop, buffer, replay, and acknowledgement semantics for each category.
- [ ] Introduce or reuse one lifecycle-aware effect collection pattern.
- [ ] Migrate Search, Watchlist, and Movie Detail effect collection first.
- [ ] Remove declared effects that are unreachable or intentionally use direct callbacks.
- [ ] Ensure `trySend`/`send` failures are handled according to the chosen contract.
- [ ] Add lifecycle transition tests for navigation and user-feedback effects where feasible.
- [ ] Verify no effect executes after its destination is no longer active.

### Phase 6 — P1: Localized and Recoverable Error Handling

**Data sources**
- Existing `Result.failure` causes.
- Existing domain error types and HTTP/database exceptions.
- Existing string resources in `core:string-resources`.
- Existing observability tracker for diagnostic reporting.

**Side effects**
- ✅ Allowed:
    - Map known failures to stable domain/presentation error categories.
    - Add English and Spanish string resources.
    - Emit snackbar/user-feedback effects.
    - Record sanitized diagnostics through the existing observability contract.
- ❌ Forbidden:
    - Display raw `Throwable.message` to users.
    - Add a new logging/analytics provider.
    - Include tokens, email addresses, search text, or private user data in diagnostics.

- [ ] Define the minimum shared error categories required by affected features.
- [ ] Replace raw exception-message state in Watchlist, Search, and Movie Detail.
- [ ] Remove hardcoded fallback error text from production Kotlin.
- [ ] Ensure Watchlist removal failure is visible and recoverable.
- [ ] Ensure Movie Detail `ShowError` effects are rendered rather than ignored, or remove them if state handles the failure.
- [ ] Keep screen-level loading failures distinct from non-blocking mutation failures.
- [ ] Add matching English and Spanish resources.
- [ ] Add tests for error mapping, localized resource selection, state preservation, and observability calls.

### Phase 7 — P1: Segregate Repository and Infrastructure Capabilities

**Data sources**
- Existing Room-backed `DatabaseRepository` operations.
- Existing sync scheduler behavior.
- Existing search cache operations.

**Side effects**
- ✅ Allowed:
    - Add focused domain-facing interfaces in existing modules.
    - Make the current Room repository implement multiple focused interfaces.
    - Add focused use cases for sync scheduling and cache maintenance.
    - Update Hilt bindings and affected test doubles.
- ❌ Forbidden:
    - New modules or persistence implementations.
    - Duplicate repository implementations that wrap the same operations without reducing consumer coupling.
    - Data migrations, service calls, or navigation changes.
    - Abstracting every concrete class without a current consumer need.

- [ ] Map each `DatabaseRepository` method to its actual consumers.
- [ ] Define the smallest useful Watchlist, Watched Movies, Movie State, Sync Store, and User Data capabilities.
- [ ] Decide whether focused interfaces belong in `core/domain` or the existing consuming feature/domain package without creating new modules.
- [ ] Make the current Room implementation satisfy the focused contracts.
- [ ] Migrate affected use cases away from the broad `DatabaseRepository` dependency.
- [ ] Introduce a domain-facing sync-scheduling use case/contract for ViewModels that currently know WorkManager.
- [ ] Introduce a focused cache-maintenance use case/capability for Search presentation.
- [ ] Remove concrete `WorkManagerSyncScheduler` and `CachingSearchMoviesRepository` dependencies from affected ViewModels.
- [ ] Keep Hilt bindings explicit and avoid singleton scope for stateless use cases unless required.
- [ ] Add contract/implementation tests and update feature tests with smaller fakes.

### Phase 8 — P2: Reduce Search ViewModel Responsibilities

**Data sources**
- Existing search query and filter state.
- Existing browse/dashboard, recent-search, trending, pagination, and cache use cases.
- Existing `SavedStateHandle` query.

**Side effects**
- ✅ Allowed:
    - Extract cohesive orchestration/state helpers with independent reasons to change.
    - Keep the ViewModel as the screen-level state owner.
    - Preserve the public intent/state/effect contract unless a test demonstrates a required correction.
- ❌ Forbidden:
    - Strict MVI reducer/store migration.
    - One class per method or speculative abstractions.
    - New service calls, repositories, or user-visible behavior.
    - Splitting the Search feature into additional Gradle modules.

- [ ] Identify independent responsibility clusters using current tests and change history.
- [ ] Establish characterization tests for dashboard loading, search session, pagination, suggestions, filters, refresh, and effects.
- [ ] Extract only responsibilities that have independent lifecycle, cancellation policy, or test setup.
- [ ] Keep one authoritative aggregate `SearchMoviesUiState` for Compose rendering.
- [ ] Centralize latest-query and pagination ordering rules.
- [ ] Ensure cache refresh and dashboard requests cannot overwrite active search state incorrectly.
- [ ] Keep observability at explicit user/application boundaries rather than inside pure state transformations.
- [ ] Compare complexity, constructor size, and test readability before and after; revert extractions that add indirection without reducing coupling.

### Phase 9 — P2: Process-Death and Restorable Input Policy

**Data sources**
- Typed navigation arguments.
- Existing `SavedStateHandle` values.
- User-entered query/filter state.
- Room/cloud data that can be reloaded.
- Incoming deep-link intent movie ID.

**Side effects**
- ✅ Allowed:
    - Save small user-entered values and stable identifiers.
    - Recreate derived data from existing repositories/use cases.
    - Make initialization idempotent in ViewModels.
- ❌ Forbidden:
    - Persisting movie lists, effects, throwable objects, or full UI models in saved state.
    - Adding a new persistence store for transient screen state.
    - Replaying one-off effects after process death without an explicit requirement.

- [ ] Document which screen values must survive configuration change, process death, or neither.
- [ ] Restore Watchlist query if losing it materially interrupts the user.
- [ ] Review Search query, genre/filter, pagination, and suggestion state; save only meaningful user choices.
- [ ] Confirm Movie Detail reconstruction uses the typed route ID and one idempotent fetch.
- [ ] Test authenticated deep-link process recreation and preserve the pending ID only if navigation restoration is insufficient.
- [ ] Remove or refactor initialization helpers whose `rememberSaveable` state can disagree with a newly created ViewModel.
- [ ] Add `SavedStateHandle` and recreation tests for every adopted value.

### Phase 10 — P2: Expand Architecture and Contract Enforcement

**Data sources**
- Kotlin production source declarations/imports.
- Gradle project dependencies.
- Existing architecture policy in `AGENTS.md`.
- Existing Konsist test suite.

**Side effects**
- ✅ Allowed:
    - Add semantic Konsist or Gradle dependency assertions.
    - Add approved exceptions with narrow documented reasons.
- ❌ Forbidden:
    - Text rules that are trivially bypassed by aliases/spacing when semantic alternatives exist.
    - Rules enforcing the explicitly accepted non-goals: separate feature-domain modules, hidden app composition, or pure MVI.
    - Architecture tests that require production reflection or runtime overhead.

- [ ] Add a rule preventing feature modules from depending on another feature's `impl` module.
- [ ] Add a rule preventing feature presentation packages from importing data/infrastructure packages.
- [ ] Add a rule preventing public mutable Flow exposure from ViewModels.
- [ ] Add a rule limiting public declarations in `impl` modules to approved entry points where reliable.
- [ ] Add a rule preventing Room entities and API DTOs from escaping data packages where reliable.
- [ ] Add a rule or focused test for raw exception messages entering UI state.
- [ ] Remove duplicated or low-value substring checks when semantic rules supersede them.
- [ ] Document accepted pragmatic boundaries so future audits do not misclassify them as defects.

### Phase 11 — P2: Make Static Analysis and Coverage Verifiable

**Data sources**
- Existing Gradle task graph.
- `config/detekt/detekt.yml`.
- Existing Kover plugin configuration and module tests.
- Existing CI workflow and test reports.

**Side effects**
- ✅ Allowed:
    - Wire existing Detekt configuration explicitly.
    - Configure Kover reports, exclusions, aggregation, and staged verification thresholds.
    - Upload non-sensitive reports/artifacts in CI.
    - Split CI steps/jobs for visible failure ownership.
- ❌ Forbidden:
    - Inflating coverage by testing generated/trivial code.
    - Setting unmeasured thresholds that immediately block unrelated development.
    - Disabling rules/tests to make CI green without documenting the reason.
    - Adding external paid services.

- [ ] Prove whether the custom Detekt YAML is loaded; wire it explicitly if it is not.
- [ ] Confirm Detekt analyzes Android `src/main/java` and KTS sources as intended.
- [ ] Generate an aggregate baseline Kover report with deliberate generated/DI/UI exclusions documented.
- [ ] Replace aspirational coverage claims with measured module/class baselines.
- [ ] Introduce staged verification thresholds that cannot regress below the measured baseline.
- [ ] Keep stricter use-case/mapper expectations only where meaningful executable code exists.
- [ ] Add explicit CI steps for architecture tests and Paparazzi verification so results are visible.
- [ ] Add coverage and static-analysis report artifacts.
- [ ] Add Android lint to CI if the current build does not already execute it transitively.
- [ ] Investigate and resolve the `:library:remote-config:impl:testDebugUnitTest` timeout observed during the architecture audit.
- [ ] Ensure local documentation lists commands that actually exist and match CI.

### Phase 12 — P3: Security, Observability, and Performance Follow-ups

**Data sources**
- Existing secure-storage implementation.
- Existing observability API/implementation.
- Existing CI dependency graph.
- Measured startup, scrolling, sync, and APK behavior collected during this phase.

**Side effects**
- ✅ Allowed:
    - Add tests for existing secure storage and observability behavior.
    - Add free dependency/security review tooling.
    - Add baseline profiles or macrobenchmarks after selecting measured critical journeys.
    - Sanitize telemetry through existing observability contracts.
- ❌ Forbidden:
    - Certificate pinning without a rotation/failure strategy.
    - Recording credentials, tokens, email addresses, search text, or private movie/user data.
    - Performance refactors without a measured baseline.
    - Changing authentication or encryption providers as part of this plan.

- [ ] Add secure-storage tests for normal access, corrupted/invalid key recovery, and data-loss boundaries.
- [ ] Narrow encrypted-preference destructive recovery to expected key/storage failures.
- [ ] Add observability tests for event sanitization, truncation, user clearing, and stale Crashlytics metadata.
- [ ] Define a telemetry privacy allowlist.
- [ ] Add dependency review/vulnerability and secret scanning to CI where available.
- [ ] Add Gradle wrapper validation and dependency verification/checksum policy where practical.
- [ ] Measure startup and one critical lazy-list journey before selecting baseline-profile/macrobenchmark work.
- [ ] Add only benchmarks that produce actionable regression thresholds.

### Phase 13 — Documentation and Final Verification

**Data sources**
- Completed phase decisions and test evidence.
- Final Gradle/CI task output.
- Updated architecture and quality configuration.

**Side effects**
- ✅ Allowed:
    - Update this FIP, `AGENTS.md`, `docs/README.md`, and directly affected technical documentation to match reality.
    - Record commands and measured results.
- ❌ Forbidden:
    - Claiming manual/device/accessibility verification that was not performed.
    - Rewriting historical completed FIPs to pretend new constraints existed at the time.

- [ ] Run affected module tests after every phase.
- [ ] Run `./gradlew ktlintCheck detekt test assembleDebug`.
- [ ] Run explicit architecture, Paparazzi, coverage verification, and Android lint tasks adopted by this FIP.
- [ ] Run affected instrumentation tests on the CI API level and one supported physical device where available.
- [ ] Verify repeated retry, rapid query, rapid toggle, background/foreground, rotation, and process recreation manually.
- [ ] Update documentation to distinguish pragmatic MVI from strict reducer-based MVI.
- [ ] Document accepted module/composition non-goals as intentional tradeoffs.
- [ ] Record measured coverage rather than unsupported target claims.
- [ ] Complete §10 Validation with actual results.

---

## 10. Validation

_Fill after implementation. A phase is not considered validated solely because its checkboxes are marked._

| What | Result | Notes |
|------|--------|-------|
| Unit and Flow regression tests | ⏳ | Duplicate collectors, latest-wins, cancellation, mutations, effects |
| Architecture tests | ⏳ | Existing and new semantic rules |
| Static analysis and formatting | ⏳ | ktlint and explicitly wired Detekt configuration |
| Coverage verification | ⏳ | Measured aggregate baseline and staged thresholds |
| Android lint | ⏳ | Record exact task and result |
| Paparazzi verification | ⏳ | Record exact task and result |
| Instrumented feature tests | ⏳ | Record emulator API/device |
| Manual retry/query/toggle stress test | ⏳ | Record scenarios and result |
| Lifecycle background/foreground test | ⏳ | Confirm effects are not stale or duplicated |
| Rotation and process recreation | ⏳ | Confirm adopted saved-state policy |
| Accessibility check | ⏳ | Confirm changed error feedback is announced/readable |
| Real device smoke test | ⏳ | Record model and Android version |

---

## 11. Blockers

| # | Blocker | Raised | Resolved | Impact |
|---|---------|--------|----------|--------|
| 1 | None at planning time | 2026-07-12 | — | — |

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should transient effects be dropped while inactive or buffered until STARTED? | Resolve per effect category in Phase 5 before implementation |
| 2 | Should failed movie mutations be pessimistic or optimistic with rollback? | Resolve per feature in Phase 3 using current UX and Room source-of-truth behavior |
| 3 | Which Search responsibilities merit extraction rather than private functions? | Resolve in Phase 8 using independent lifecycle/cancellation/test complexity as criteria |
| 4 | Which state values materially need process-death restoration? | Resolve in Phase 9; default to reloading derived data |
| 5 | What are the measured Kover baselines after correct aggregation/exclusions? | Measure in Phase 11 before selecting thresholds |
| 6 | Is the remote-config test timeout reproducible in CI or isolated to the audit environment? | Investigate in Phase 11 |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Keep the current feature-domain package/module arrangement | Separate JVM domain module per feature | Current arrangement is pragmatic; stronger physical isolation does not justify module/build cost for this project |
| 2 | Keep `app` as the explicit composition root that knows feature implementations | Navigation registrar/plugin abstraction | Current direct wiring is clear and replacement/dynamic delivery is not a requirement |
| 3 | Keep pragmatic MVI-style ViewModels | Strict reducer/store MVI; direct-method MVVM rewrite | Current intent/state/effect contracts are useful; concrete races can be fixed without framework churn |
| 4 | Prioritize behavioral correctness before SOLID cleanup | Start with interface/viewmodel refactors | Duplicate collectors, stale results, cancellation, and silent failures affect users directly |
| 5 | Introduce abstractions only for existing concrete coupling | Interface for every implementation | Focused capabilities should reduce consumer dependencies, not add ceremonial indirection |
| 6 | Save user input/identifiers, reload derived data | Save complete screen state | Small saved state is safer and avoids stale/oversized bundles |
| 7 | Measure coverage before enforcing thresholds | Apply documented targets immediately | Unmeasured thresholds can block work or reward low-value tests |
| 8 | Use one query-driven Watchlist pipeline with `flatMapLatest` and one replaceable Movie Detail fetch job | Independent coroutine launches; strict serialized MVI store | The latest query/fetch is authoritative without changing the established MVI-style architecture |

---

## 14. Out of Scope / Follow-ups

- Separate feature-domain JVM modules.
- Dynamic feature delivery or plugin-based navigation registration.
- Strict reducer/store MVI and time-travel debugging.
- Replacing `Result<T>` across the entire repository with a new universal result framework.
- Full offline-first redesign of all catalogue data.
- Backend conflict-resolution redesign.
- Complete design-system/accessibility audit outside UI touched by this refactor.
- Desktop, iOS, or multiplatform work.
- New product features.

---

## 15. Handover Notes

- Execute phases in priority order; do not bundle this FIP into one large pull request.
- Recommended delivery is one pull request per phase or tightly related phase pair.
- Re-read the latest affected code before implementation because active development may have resolved or changed audit findings.
- Preserve current user behavior unless a phase explicitly defines a corrected failure/concurrency contract.
- If a regression test cannot reproduce an audited issue, document the evidence and remove the corresponding production task rather than forcing a speculative refactor.
- Do not use this FIP to claim Santoro has severe architectural failure. It is a targeted reliability and enforcement initiative.

---

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-07-12 | Initial prioritized architecture reliability refactor plan |
| 1.1     | 2026-07-12 | Started execution of Phases 1–2 |
| 1.2     | 2026-07-12 | Completed the urgent duplicate-collector and latest-result subset of Phases 1–2 |
