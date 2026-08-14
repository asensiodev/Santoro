# FIP — High-Risk Test Coverage

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                    |
|------------------------|--------------------------------------------------------------------------|
| **FIP ID**             | FIP-019                                                                  |
| **Version**            | 1.0                                                                      |
| **Status**             | 🔵 In Progress                                                           |
| **PRD ref**            | Internal quality initiative — no PRD feature                             |
| **Feature**            | Behavioral coverage for high-risk Android and service boundaries        |
| **Date**               | 2026-08-06                                                               |
| **Author**             | @asensiodev                                                              |
| **Definition of Done** | All checkboxes are marked `[x]`, CI runs the adopted suites, and §10 is complete |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or does not fit the plan — **stop and ask** before deciding.

> **Defect rule:** Tests must describe the current intended contract. If a new test exposes a production defect or an undocumented contract, record the evidence and update this FIP before changing production behavior.

---

## 0. Prerequisites

- Read [FIP-018 — Architecture Reliability Refactor](./FIP-018-architecture-reliability-refactor.md), especially Phases 11–13.
- Re-read the affected production class and its existing tests before each phase.
- Run the affected module test task before editing to establish a green baseline.
- Confirm whether concurrent work has already added equivalent coverage; do not duplicate tests.
- Coordinate secure-storage and CI changes with FIP-018. When both plans request the same outcome, one implementation must satisfy both checklists.
- Do not start FIP-020 until this FIP has no unresolved high-risk regressions.

---

## 1. Context & Motivation

Santoro has broad ViewModel, use-case, repository, mapper, screenshot, architecture, and instrumented coverage. Aggregate Kover verification currently protects JVM-tested logic, while CI separately runs selected instrumented suites.

The remaining highest-risk gaps are concentrated at Android and SDK boundaries where mock-heavy upper-layer tests cannot validate the real contract:

1. Firebase Auth and Credential Manager adapters have limited direct coverage.
2. Encrypted preferences and recent-search DataStore persistence have no direct behavioral suites.
3. WorkManager request construction is untested even though worker behavior is covered.
4. OkHttp request interceptors and assembled request behavior are untested.
5. Room migration and several real DAO/cache queries are not validated against SQLite.
6. The app composition root has no instrumented navigation/authentication journey coverage.

These gaps can allow authentication, persistence, scheduling, networking, migration, or navigation regressions despite a passing aggregate coverage threshold.

---

## 2. Goals

- Add deterministic behavioral tests for the existing authentication adapters and credential parsing paths.
- Verify encrypted preference and recent-search persistence contracts, including documented recovery and malformed-data boundaries.
- Verify WorkManager request names, policies, constraints, intervals, tags, and input data.
- Verify authorization and locale request construction without real network traffic.
- Validate all supported Room migrations and high-risk DAO/cache SQL against a real in-memory database.
- Add app-level instrumented coverage for authentication routing, deep links, tab/navigation state, and logout back-stack behavior.
- Run every adopted suite explicitly in local commands and CI with visible failure ownership.
- Preserve meaningful assertions rather than optimizing tests for aggregate coverage percentage.

---

## 3. Non-Goals

- No new product features, screens, destinations, services, analytics events, or backend operations.
- No live Firebase, TMDB, Firestore, Remote Config, or Google account calls from automated tests.
- No Firestore security-rules or Firebase Emulator initiative in this FIP.
- No database schema, migration, account-deletion, sync-conflict, or authentication-flow redesign.
- No replacement of Hilt, Room, DataStore, WorkManager, Retrofit, OkHttp, Firebase, Credential Manager, or encrypted preferences.
- No arbitrary increase to Kover or Codecov thresholds.
- No tests for generated, DI, trivial delegation, or framework code solely to raise coverage.
- No broad production refactor for testability. A minimal seam requires evidence that the existing boundary cannot be controlled deterministically and must be recorded in §13 first.
- No work assigned to FIP-020, including full-screen visual matrices, broad accessibility testing, observability tests, or expanded architecture policy.

---

## 4. User Stories

| ID    | As a… | I want to… | So that… | Acceptance Criteria |
|-------|-------|------------|----------|---------------------|
| US-01 | developer | verify Android and SDK boundary contracts deterministically | regressions are found before manual testing | Each high-risk boundary has focused success, failure, cancellation, and edge-case coverage where applicable |
| US-02 | developer | validate real persistence and SQL behavior | mocks do not hide serialization, migration, or query defects | Adopted DataStore, encrypted-storage, migration, and DAO tests use real local storage/database behavior |
| US-03 | developer | verify scheduled background work | synchronization is neither silently disabled nor duplicated | Work names, policies, constraints, intervals, tags, and input data are asserted |
| US-04 | developer | verify app-level navigation journeys | isolated feature tests do not hide composition-root defects | Authentication routing, deep links, logout, and representative cross-feature navigation are exercised through app navigation |
| US-05 | maintainer | see which quality layer failed in CI | failures are actionable | JVM, boundary/instrumented, and app-navigation suites have explicit CI commands and reports |

---

## 6. Architecture

```text
Contract under test
       │
       ├── JVM test with controlled SDK collaborators
       ├── Local integration test with temporary storage / MockWebServer
       └── Instrumented test with Android framework implementation

No live external service is a test data source.
```

Test at the narrowest layer that can validate the real contract. Use an instrumented test only when the Android framework implementation itself is material to the behavior.

---

## 8. Modules Affected

- `app`
- `core/auth`
- `core/database`
- `core/network`
- `core/sync`
- `feature/search-movies/impl`
- `library/secure-storage/impl`
- `core/testing` only for reusable test utilities with at least two immediate consumers
- `build-logic/convention` only if an adopted test dependency/configuration is genuinely common
- `gradle/libs.versions.toml` only for required test libraries
- `.github/workflows/ci.yml`
- `docs/plan/FIP-018-architecture-reliability-refactor.md` only to mark genuinely shared completed work

No new Gradle module is planned.

---

## 9. Phases & Tasks

### Phase 1 — Baseline and Contract Inventory

**Data sources**
- Current production behavior in the affected classes.
- Existing tests and test conventions.
- Current Gradle task graph, CI workflow, and generated test reports.

**Side effects**
- ✅ Allowed:
    - Run read-only test, task-discovery, and coverage commands.
    - Record baseline results and exact adopted test tasks in this FIP.
- ❌ Forbidden:
    - Change production behavior or quality thresholds.
    - Delete failing tests or exclusions to create a green baseline.

- [x] Run all currently affected JVM and instrumented suites and record exact results.
- [x] Build a traceability table mapping every goal to production classes, test class, test type, and CI task.
- [x] Identify tests already covered by FIP-018 and assign one owning FIP for each shared outcome.
- [x] Confirm test dependencies already available before adding new version-catalog entries.
- [x] Record any pre-existing failure as a blocker rather than weakening the planned assertion.

#### Phase 1 Baseline

| Suite | Command | Result |
|-------|---------|--------|
| Affected JVM suites | `./gradlew :core:auth:testDebugUnitTest :core:database:testDebugUnitTest :core:network:testDebugUnitTest :core:sync:testDebugUnitTest :feature:search-movies:impl:testDebugUnitTest :library:secure-storage:impl:testDebugUnitTest :app:testDebugUnitTest` | ✅ Passed on 2026-08-07; 494 tasks, 1 executed and 493 up-to-date |
| Database, secure-storage, and app instrumentation | `./gradlew :core:database:connectedDebugAndroidTest :library:secure-storage:impl:connectedDebugAndroidTest :app:connectedDebugAndroidTest` | ✅ Passed on 2026-08-07 using `Pixel_9a` (`sdk_gphone16k_arm64`, API 37): 6 database tests, 0 secure-storage tests, and 0 app tests |

The first instrumentation attempt found no connected device. After starting the available `Pixel_9a` AVD, the complete command passed. A Kotlin daemon `NoSuchMethodError` occurred during the baseline and recurred during Phase 3; Gradle's non-daemon fallback compiled the affected suites successfully and no test failed because of it.

#### Traceability

| Goal | Production contract | Existing or planned test | Type and task |
|------|---------------------|--------------------------|---------------|
| Secure persistence | `EncryptedPrefsSecureKeyValueStore` | Planned `EncryptedPrefsSecureKeyValueStoreTest` | Instrumented, `:library:secure-storage:impl:connectedDebugAndroidTest` |
| Recent-search persistence | `DataStoreRecentSearchesDataSource` | Planned `DataStoreRecentSearchesDataSourceTest`; existing use-case tests remain repository-level only | JVM local integration, `:feature:search-movies:impl:testDebugUnitTest` |
| Firebase authentication | `FirebaseAuthDataSource` | Expand existing `FirebaseAuthDataSourceTest`; repository tests already cover wrapped cancellation | JVM, `:core:auth:testDebugUnitTest` |
| Credential parsing | `GoogleSignInHelper` | `GoogleSignInHelperTest` | Robolectric JVM test through JUnit 4/Vintage, `:core:auth:testDebugUnitTest` |
| Work scheduling | `WorkManagerSyncScheduler` | Planned `WorkManagerSyncSchedulerTest`; existing worker tests cover execution rather than request construction | JVM or Robolectric, `:core:sync:testDebugUnitTest` |
| HTTP authorization and locale | `AuthorizationInterceptor`, `LanguageInterceptor`, `ApiKeyAuthenticator`, assembled `OkHttpClient` | Planned interceptor/chain tests; existing `ApiKeyAuthenticatorTest` owns retry and cancellation behavior | JVM with MockWebServer, `:core:network:testDebugUnitTest` |
| Room migrations | `SantoroRoomDatabase` migrations 1–5 | Planned `SantoroRoomDatabaseMigrationTest`; version-1 schema remains unavailable | Instrumented, `:core:database:connectedDebugAndroidTest` |
| DAO and browse-cache SQL | `MovieDao`, `BrowseCacheDao` | Expand `MovieDaoTest` and add `BrowseCacheDaoTest`; mocked repository/data-source tests do not cover SQLite semantics | Instrumented, `:core:database:connectedDebugAndroidTest` |
| Authentication routing | `MainActivity`, `SantoroNavHost` | `MainActivityAuthenticationJourneyTest` | Hilt/Compose instrumentation, `:app:connectedJourneyTestAndroidTest` |
| Deep links and navigation | `MainActivity`, `DeepLinkHandler`, `SantoroTabNavGraph`, `SantoroMainTabComponent` | App journey tests; `DeepLinkHandlerTest` owns parser-only cases | Hilt/Compose instrumentation, `:app:connectedJourneyTestAndroidTest` |

#### Dependency Inventory

| Dependency or support | Baseline status |
|-----------------------|-----------------|
| WorkManager testing | Already catalogued as `workmanager-testing` and used by `core/sync` test configuration |
| Compose UI test, AndroidX JUnit, Espresso, Kluent Android | Already supplied to Android modules by convention plugins |
| DataStore and coroutine testing | Existing Search production dependency and shared JVM test configuration are sufficient for temporary-file tests |
| Firebase Auth, Google Tasks, MockK, coroutine testing | Already available to `core/auth` JVM tests |
| MockWebServer | Missing; add only to `core/network` when Phase 5 starts |
| Room testing | Missing; add only to `core/database` instrumented tests when Phase 6 starts |
| Robolectric | Added module-locally to `core/auth` after Phase 3 proved that real `Bundle` request and token parsing required a local Android runtime |
| Hilt Android testing, `kspAndroidTest`, navigation testing, Hilt test runner | Missing from `app`; add the minimum module-local setup when Phase 7 starts |

#### Shared Ownership with FIP-018

| Outcome | Owning implementation |
|---------|-----------------------|
| Secure-storage behavioral characterization | FIP-019; the same suite may satisfy FIP-018 Phase 12, while any production recovery narrowing remains owned by FIP-018 unless a FIP-019 defect amendment says otherwise |
| Authentication and authenticator cancellation | Existing FIP-018 tests; FIP-019 expands success, failure, listener, malformed-result, and request contracts without duplicating cancellation cases |
| WorkManager request construction | FIP-019; FIP-018 may later own the domain-facing scheduler abstraction |
| Room migrations and real SQL semantics | FIP-019 |
| Feature effect lifecycle and deep-link process restoration | FIP-018; FIP-019 tests only app assembly and navigation outcomes |
| CI visibility and reports | First completed implementation satisfies both plans; FIP-019 must not change thresholds owned by FIP-018 |

### Phase 2 — Secure and Recent-Search Persistence

**Data sources**
- Temporary test preference files and deterministic test keys/values.
- Temporary Preferences DataStore files.
- Existing `EncryptedPrefsSecureKeyValueStore` and `DataStoreRecentSearchesDataSource` APIs.

**Side effects**
- ✅ Allowed:
    - Read, write, remove, and clear temporary test-only storage.
    - Recreate the subject under test to verify persistence across instances.
    - Simulate documented malformed/corrupted local state without using real user data.
- ❌ Forbidden:
    - Access developer or user preference files.
    - Change encryption providers or persist test credentials.
    - Add network, Firebase, or database calls.

- [x] Add direct tests for secure-store put/get, overwrite, remove, clear, missing keys, and instance recreation.
- [x] Verify the documented encrypted-preference recovery path and its data-loss boundary using an isolated test context.
- [x] Add direct tests for recent-search insertion, deduplication, recency ordering, maximum-size truncation, clear, and instance recreation.
- [x] Define and test the existing malformed/blank JSON contract without silently inventing recovery behavior.
- [x] Add concurrency coverage for overlapping DataStore edits where the public API permits them.
- [x] Keep test files and temporary state isolated so execution order cannot affect results.

Phase 2 preserves the existing malformed-data contract: blank JSON reads as an empty list; malformed JSON fails the read Flow with `JsonSyntaxException`; a later `saveSearch` recovers from either value and starts a new history. Secure-storage corruption coverage proves that an invalid encrypted keyset deletes all values in that isolated preference file before recreating usable encrypted storage.

### Phase 3 — Authentication and Credential Adapters

**Data sources**
- Mocked Firebase Auth tasks/results/users and auth-state listeners.
- Synthetic Credential Manager results and Google ID tokens created only in test memory.
- Existing authentication error types and repository contracts.

**Side effects**
- ✅ Allowed:
    - Invoke existing sign-in, linking, sign-out, deletion, and auth-state APIs against controlled collaborators.
    - Register and remove in-memory listener callbacks.
- ❌ Forbidden:
    - Contact Firebase or Google services.
    - Use real accounts, tokens, client IDs, credentials, or emulator secrets.
    - Change account-switching, deletion, linking, or reauthentication product behavior.

- [x] Expand `FirebaseAuthDataSourceTest` for auth-state initial/emitted values and listener cleanup.
- [x] Cover anonymous sign-in success, expected failure, and cancellation propagation.
- [x] Cover Google sign-in success, null/malformed SDK results, expected failure, and cancellation propagation.
- [x] Cover anonymous account linking, account collision mapping, missing current user, expected failure, and cancellation propagation.
- [x] Cover sign-out and account-deletion success, missing-user, failure, and cancellation contracts.
- [x] Add `GoogleSignInHelper` tests for primary credential flow, fallback flow, token parsing, unsupported credential type, cancellation, and expected exceptions.
- [x] Assert that tests and logs never include raw credentials or tokens.

Phase 3 adds 21 direct `FirebaseAuthDataSource` tests and six Robolectric `GoogleSignInHelper` tests, bringing `core/auth` to 31 passing tests in both debug and release variants. Sign-out has no current-user branch and is covered as direct success, failure, and cancellation propagation; missing-user behavior is covered for linking and deletion. The unsupported-type regression first demonstrated that a custom credential carrying a valid Google bundle was accepted, then added explicit type validation before parsing. Assertions compare token digests, the suite checks captured logs for sensitive fixtures, and generated XML/HTML reports contain neither the synthetic token nor client ID.

### Phase 4 — WorkManager Scheduling Contract

**Data sources**
- WorkManager test driver/in-memory WorkManager state.
- Existing scheduler constants and method parameters.
- Synthetic non-sensitive movie IDs.

**Side effects**
- ✅ Allowed:
    - Enqueue and inspect test-only work requests.
    - Advance WorkManager test state without running production network/Firebase work.
- ❌ Forbidden:
    - Execute live synchronization or upload side effects.
    - Change scheduling policy, periodicity, worker retry behavior, or worker payload semantics under the guise of testing.

- [x] Add the existing catalogued WorkManager testing dependency to the narrowest required test configuration.
- [x] Verify periodic work name, six-hour interval, connected-network constraint, and existing-work policy.
- [x] Verify immediate sync name, connected-network constraint, and existing-work policy.
- [x] Verify per-movie upload unique name, tag, input movie ID, connected-network constraint, and existing-work policy.
- [x] Verify repeated scheduling produces the currently documented replacement/retention behavior.
- [x] Keep scheduler tests separate from existing worker behavior tests.

Phase 4 adds three Robolectric tests against WorkManager's in-memory test database. They verify periodic `KEEP`, immediate `REPLACE`, per-movie upload `REPLACE`, unique names, tags, persisted input, connected-network constraints, and the six-hour repeat/flex interval without executing workers. FIP-003's historical `APPEND_OR_REPLACE` upload note does not match current or pre-existing production behavior; this phase characterizes the existing `REPLACE` contract without changing scheduling behavior.

### Phase 5 — HTTP Request Contract

**Data sources**
- In-process MockWebServer responses and recorded requests.
- Synthetic API keys and locale values that are visibly test-only.
- Existing OkHttp interceptors/authenticator configuration.

**Side effects**
- ✅ Allowed:
    - Send requests only to an in-process loopback test server.
    - Inspect headers, query parameters, retry count, and request ordering.
- ❌ Forbidden:
    - Contact TMDB, Firebase, or any external host.
    - Read real API-key storage or include real credentials in fixtures/reports.
    - Change network timeout, retry, authentication, or localization behavior without a separately approved defect amendment.

- [x] Add a MockWebServer test dependency aligned with the repository’s OkHttp version if no existing dependency can validate the contract.
- [x] Test blank and nonblank authorization-key behavior, header replacement, and absence of duplicate authorization headers.
- [x] Test language tags with and without country and behavior when a language query parameter already exists.
- [x] Test the assembled interceptor chain against recorded requests, including JSON accept headers.
- [x] Verify authenticator retry requests do not expose secrets in assertions or reports.
- [x] Add a regression assertion that debug logging redacts the authorization header; if production redaction is absent, record it as a blocking defect before changing code.

Phase 5 baseline evidence found that `AuthorizationInterceptor.addHeader` duplicates a caller-provided authorization value and that debug BODY logging emits the resulting authorization header without redaction. It also confirmed that `LanguageInterceptor.addQueryParameter` duplicates an existing `language` parameter. On 2026-08-14 the intended contracts were approved before production changes: authorization and language values are replaced so each request contains exactly one authoritative value, and debug logging redacts authorization credentials.

Phase 5 adds eight loopback request-contract tests and one authenticator replacement regression, bringing `core/network` to 24 passing tests in both debug and release variants. MockWebServer 5.1.0 matches OkHttp, all network traffic remains in-process, module Detekt and ktlint pass, and generated XML/HTML results contain none of the synthetic key fixtures.

### Phase 6 — Room Migrations and SQL Semantics

**Data sources**
- Checked-in Room schemas and temporary databases created from supported historical versions.
- Synthetic movie and browse-cache entities.
- Existing DAO and repository contracts.

**Side effects**
- ✅ Allowed:
    - Create, migrate, query, and delete isolated test databases.
    - Add Room testing dependencies and generated historical fixtures required for validation.
- ❌ Forbidden:
    - Access or mutate a developer/user database.
    - Change schema, migration SQL, conflict policy, or query semantics unless a failing regression demonstrates a defect and this FIP is amended.
    - Use destructive migration fallback.

- [x] Add the Room migration-testing dependency to the database module’s instrumented test configuration.
- [x] Validate each available migration path and the complete supported upgrade path to version 5.
- [x] Resolve the missing version-1 schema prerequisite explicitly: validation is skipped because no trustworthy shipped v1 schema exists.
- [x] Add real SQLite tests for browse-cache composite lookup, replacement, section deletion, age cutoff, and full clear.
- [x] Extend `MovieDaoTest` for watchlist search, watchlist removal timestamps, sync upsert fields, sync-state updates, watched ordering, and clear-all behavior.
- [x] Assert preserved data and default/null values introduced by each migration.

Phase 6 adds four migration tests covering every trustworthy checked schema edge (`2→3`, `3→4`, `4→5`) and the complete available `2→5` chain, five browse-cache DAO tests, and six additional MovieDao tests. All 21 database instrumented tests pass on the API 37 Pixel 9a AVD; JVM debug/release tests, Android-test assembly, Detekt, and ktlint also pass with no checked-schema drift.

Version 1 remains intentionally unclaimed. Git history proves that `watchedAt` was added while the database remained at version 1, producing early and late v1 shapes, and no v1 schema was exported. Distribution provenance is unknown, so generating one canonical schema would hide a potentially unsupported early-v1 migration. On 2026-08-14 the missing v1 validation was explicitly accepted as skipped; the production migration remains unchanged and the residual upgrade risk remains documented.

### Phase 7 — App Navigation and Authentication Journeys

**Data sources**
- Synthetic authentication state from test replacements/fakes.
- Typed navigation routes and synthetic movie IDs.
- App-local navigation state and test intents.

**Side effects**
- ✅ Allowed:
    - Launch the app/activity in an instrumented test process.
    - Replace external authentication/sync collaborators with deterministic test implementations through supported Hilt testing mechanisms.
    - Navigate through existing destinations and dispatch existing deep-link intents.
- ❌ Forbidden:
    - Contact Firebase, Firestore, TMDB, Remote Config, or Google Identity.
    - Add test-only branches to production navigation.
    - Change destination graphs, authentication policy, or back-stack behavior without a reproduced defect and plan amendment.

- [x] Add the minimum Hilt/Compose/navigation test dependencies and runner configuration needed for app-level instrumentation.
- [x] Verify unauthenticated launch selects Login and authenticated launch selects the tab host.
- [x] Verify logout returns to Login and prevents navigating back into authenticated content.
- [x] Verify authenticated movie deep links, `onNewIntent`, single consumption, and malformed/unsupported links.
- [x] Verify representative tab switching preserves the currently intended tab state.
- [x] Verify navigation from Search to See All and Movie Detail and from Settings back to the existing graph.
- [x] Verify lifecycle guards prevent duplicate navigation from repeated callbacks.
- [x] Keep these journeys focused on app assembly; do not duplicate detailed feature UI assertions.

Phase 7 baseline evidence found that manifest routing restricts external deep links to HTTP(S) TMDB movie URLs while `DeepLinkHandler` accepted any scheme and host when an intent was delivered directly. On 2026-08-14 the intended contract was approved before production changes: direct intent parsing must enforce the same HTTP(S) and TMDB-host boundary as the manifest.

Phase 7 adds ten Hilt/Compose app journeys using deterministic in-memory repository replacements and a dedicated `journeyTest` build type that disables AndroidX Startup and Firebase provider initialization only in the test application. The suite covers authenticated and unauthenticated launch routing, logout back-stack removal, launch and `onNewIntent` deep links, single consumption, malformed and unsupported links, tab-state restoration, Search/See All/Detail navigation, Settings return behavior, and rapid duplicate navigation. All ten tests pass on the API 37 Pixel 9a AVD; app JVM debug/release tests, journey-test assembly, Detekt, and ktlint also pass.

### Phase 8 — CI Integration and Final Verification

**Data sources**
- Adopted Gradle tasks and generated JUnit/instrumented reports.
- Existing GitHub Actions workflow and Kover/Codecov configuration.
- Completed traceability table from Phase 1.

**Side effects**
- ✅ Allowed:
    - Add explicit CI commands for new JVM and instrumented suites.
    - Upload non-sensitive failure reports.
    - Update directly affected testing documentation and shared FIP checkboxes.
- ❌ Forbidden:
    - Upload test databases, credentials, tokens, Firebase files, or sensitive logs.
    - Raise/lower coverage thresholds to force a desired outcome.
    - Mark FIP-018 tasks complete unless the implemented work fully satisfies them.

- [x] Run every affected JVM suite from a clean test invocation.
- [ ] Run every affected instrumented suite on CI’s API 35 emulator profile.
- [x] Add `app`, secure-storage, and any other newly instrumented module tasks explicitly to CI.
- [x] Preserve separate reporting for JVM coverage and instrumented behavioral suites.
- [x] Run `./gradlew ktlintCheck detekt test assembleDebug` and all adopted explicit verification tasks.
- [x] Run `./gradlew :koverXmlReport :koverHtmlReport :koverVerify` and record measured results without changing thresholds.
- [x] Confirm reports and logs contain no test token, credential, API key, or sensitive fixture.
- [x] Update §10 with exact commands, environment, results, and any skipped validation.

The clean affected-module invocation ran debug and release JVM tests for `app`, `core/auth`, `core/database`, `core/network`, `core/sync`, `feature/search-movies/impl`, and `library/secure-storage/impl`; all passed. `./gradlew ktlintCheck detekt test assembleDebug` and `./gradlew :koverXmlReport :koverHtmlReport :koverVerify :koverLog` passed. The adopted instrumented command ran app, database, secure-storage, Watchlist, Movie Detail, and Watched Movies suites successfully on the local API 37 Pixel 9a AVD. GitHub Actions now runs the same instrumented tasks on its API 35 Pixel 6 profile, but that CI execution remains pending.

---

## 10. Validation

| What | Result | Notes |
|------|--------|-------|
| Existing baseline suites | ✅ | Affected JVM suites passed; instrumented baseline passed on a local API 37 AVD with 6 database tests and no current secure-storage/app tests |
| Authentication adapter tests | ✅ | `./gradlew :core:auth:testDebugUnitTest` and `:core:auth:testReleaseUnitTest` passed on 2026-08-10 with 31 tests per variant; module `ktlintCheck` and `detekt` passed |
| Secure/DataStore persistence tests | ✅ | 7 secure-storage instrumented tests and 8 DataStore JVM tests passed on 2026-08-07; preference files and DataStore directories are isolated and removed after each test |
| WorkManager scheduler tests | ✅ | Three scheduler tests passed in debug and release variants on 2026-08-14; module Detekt and ktlint passed |
| HTTP contract tests | ✅ | 24 tests passed in debug and release variants on 2026-08-14 using loopback-only MockWebServer traffic; module Detekt and ktlint passed |
| Room migration and DAO tests | ⚠️ | 21 tests passed on API 37: `2→3`, `3→4`, `4→5`, `2→5`, browse-cache, and MovieDao SQL; v1 validation was explicitly skipped because no trustworthy historical schema exists |
| App navigation journeys | ✅ | 10 tests passed with `./gradlew :app:connectedJourneyTestAndroidTest` on the local API 37 Pixel 9a AVD; app JVM debug/release tests, journey-test assembly, Detekt, and ktlint passed |
| Aggregate Kover verification | ✅ | `./gradlew :koverXmlReport :koverHtmlReport :koverVerify :koverLog` passed: 2,658/3,308 lines (80.3507%) and 774/1,046 branches (74.00%); thresholds unchanged |
| Static analysis and build | ✅ | `./gradlew ktlintCheck detekt test assembleDebug` passed on 2026-08-14 |
| Sensitive-output review | ✅ | Generated XML, HTML, text, and log output contains none of the synthetic auth/network credential fixtures |
| Real device smoke test | Skipped | Optional validation was not run; local instrumentation used the API 37 Pixel 9a AVD |

---

## 11. Blockers

| # | Blocker | Raised | Resolved | Impact |
|---|---------|--------|----------|--------|
| 1 | Version-1 Room schema is not checked in and history contains incompatible early/late v1 shapes | 2026-08-06 | 2026-08-14 | Validation explicitly skipped because no trustworthy shipped source schema exists; `MIGRATION_1_2` and `1→5` remain unclaimed and the residual upgrade risk is accepted |
| 2 | No Android device or emulator was connected for the first local baseline attempt | 2026-08-07 | 2026-08-07 | Resolved by starting the available `Pixel_9a` API 37 AVD; CI API 35 execution remains required for final verification |
| 3 | Authorization is duplicated when already present and debug BODY logging exposes the API key | 2026-08-14 | 2026-08-14 | Approved correction: replace the authorization value and redact the header before logging |
| 4 | Existing `language` query-parameter behavior was not defined and currently appends a duplicate value | 2026-08-14 | 2026-08-14 | Approved correction: replace it with the current app locale so exactly one value is sent |
| 5 | Deep-link parser accepted schemes and hosts that the manifest does not support | 2026-08-14 | 2026-08-14 | Approved correction: reject direct intents unless they use HTTP(S) and `themoviedb.org` or `www.themoviedb.org` |

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should external Firebase services be used in these tests? | No. This FIP uses controlled collaborators and local Android integration only |
| 2 | Should newly discovered production defects be fixed immediately? | Only after a failing regression establishes the intended contract and this FIP records the production change |
| 3 | Should aggregate coverage thresholds increase after these tests? | No automatic increase; FIP-018 owns measured threshold policy |
| 4 | Who owns secure-storage and CI tasks duplicated by FIP-018? | The first executed plan owns implementation; the other records the shared evidence and does not duplicate work |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Prioritize Android/SDK boundaries over additional mocked ViewModel tests | Raise aggregate coverage broadly | Boundary failures carry greater product risk and are weakly represented by aggregate JVM coverage |
| 2 | Use no live external services in automated tests | Firebase/TMDB integration environment | Deterministic, secret-free tests must run locally and in forks/CI |
| 3 | Separate scheduler construction tests from worker behavior tests | Treat worker tests as sufficient | A correct worker cannot compensate for a malformed or never-enqueued request |
| 4 | Test app assembly with controlled external collaborators | Full production Firebase stack | The target is navigation/composition behavior, not third-party service availability |
| 5 | Keep coverage thresholds unchanged during implementation | Raise thresholds after each phase | Behavioral confidence, not numerical inflation, is the goal |
| 6 | Use module-local Robolectric with JUnit 4/Vintage for `GoogleSignInHelperTest` | Mock Android `Bundle` parsing under JUnit 5; move the suite to instrumentation | Real SDK request and token parsing behavior is material to this boundary; the exception is limited to one test class and avoids emulator-only feedback |
| 7 | Reject credentials whose declared type is not `GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL` | Parse any credential carrying a compatible data bundle | Credential type is part of the SDK boundary contract; accepting mismatched custom credentials bypasses the intended provider check |
| 8 | Add an internal `CredentialManager` constructor seam while retaining the injected context constructor | Static-mock `CredentialManager.create`; introduce a credential service interface | Static interception failed under Robolectric's classloader before any helper behavior executed; direct manager injection is the smallest deterministic seam and does not change Hilt or runtime behavior |
| 9 | Characterize the current per-movie upload policy as `REPLACE` | Assert FIP-003's stale `APPEND_OR_REPLACE` note; change production scheduling | Current production and its history use `REPLACE`; FIP-019 tests existing behavior and does not silently redesign scheduling policy |
| 10 | Replace caller-provided authorization and language values in their interceptors | Preserve or append caller values | Authorization and app locale are cross-cutting request contracts; exactly one authoritative value prevents ambiguous requests and duplicate credentials |
| 11 | Redact `Authorization` in debug HTTP logs | Disable BODY logging; accept debug-only exposure | Redaction preserves useful request diagnostics without printing API credentials |
| 12 | Use an internal logging-interceptor factory with an injectable OkHttp logger | Reflect into redaction internals; rely on platform log capture | The provider keeps its runtime contract while tests can behaviorally prove redaction without brittle reflection or Android-specific logging |
| 13 | Use `givenX_whenY_thenZ` names for instrumented tests | Backtick names required for JVM tests | D8 rejects spaces in Android test method and coroutine-generated class names for the project's target DEX level; descriptive snake-case names preserve the contract and allow instrumentation packaging |
| 14 | Validate all trustworthy migrations from v2 while retaining the v1 blocker | Hand-author one canonical v1 schema; assume the last v1 source was the only shipped shape | History contains incompatible v1 schemas under one version and distribution provenance is unknown; invented fixtures would provide false migration confidence |
| 15 | Validate scheme and host inside `DeepLinkHandler` as well as in the manifest | Keep parsing host-agnostic | Directly delivered and `onNewIntent` intents bypass manifest resolution; parser validation keeps the navigation trust boundary consistent |
| 16 | Skip version-1 migration validation and retain the production migration unchanged | Invent a canonical v1 fixture; block FIP-019 indefinitely | No trustworthy shipped v1 schema exists, so a synthetic fixture would provide false confidence; the residual upgrade risk is explicitly accepted |

---

## 14. Out of Scope / Follow-ups

- Firebase Auth/Firestore Emulator and repository-owned Firestore security-rule tests.
- Complete account deletion and per-user local-data isolation.
- Sync eventual-delivery and signed-out pending-upload redesign.
- Low priority: introduce a Santoro-owned link domain with verified Android App Links, movie-detail routing, and browser/installation fallback so shared links can open Santoro reliably without depending on TMDB domain ownership.
- Accessibility, performance, macrobenchmark, and baseline-profile initiatives.
- Medium-priority breadth tracked in [FIP-020 — Medium-Priority Test Coverage](./FIP-020-medium-priority-test-coverage.md).

---

## 15. Handover Notes

- Deliver one boundary phase per pull request where practical.
- Re-run the affected module baseline before each phase because FIP-018 and active development may change the same files.
- A test named after an adapter must cover its meaningful branches; avoid broad class names for one-edge-case suites.
- Do not interpret isolated feature instrumentation as full app end-to-end coverage.
- Keep synthetic keys and IDs visibly test-only and never print credential-like values.

---

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-06 | Initial high-risk Android and service-boundary test plan |
| 1.1     | 2026-08-07 | Completed Phase 1 inventory, dependency audit, ownership mapping, and JVM/instrumented baselines |
| 1.2     | 2026-08-07 | Completed Phase 2 secure-storage and recent-search persistence coverage |
| 1.3     | 2026-08-10 | Completed Phase 3 Firebase Auth and Credential Manager adapter coverage, including unsupported credential-type rejection |
| 1.4     | 2026-08-14 | Completed Phase 4 WorkManager request construction and repeated-scheduling coverage |
| 1.5     | 2026-08-14 | Completed Phase 5 HTTP request contracts and corrected duplicate/redacted credential and locale behavior |
| 1.6     | 2026-08-14 | Added Phase 6 migration and real-SQL coverage from v2 through v5; retained the unresolved historical v1 provenance blocker |
| 1.7     | 2026-08-14 | Completed Phase 7 app authentication, deep-link, state-restoration, and navigation journeys; added app and secure-storage instrumented suites to CI |
| 1.8     | 2026-08-14 | Completed local Phase 8 JVM, quality, build, coverage, instrumented, and sensitive-output verification; CI API 35 execution remains pending |
| 1.9     | 2026-08-14 | Explicitly skipped unprovable version-1 migration validation while retaining the production migration and documenting the accepted residual risk |
