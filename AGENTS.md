# Santoro Repository Instructions

Android app using Kotlin, Jetpack Compose, Clean Architecture, pragmatic MVI, Hilt, Room, Firebase, and multi-module Gradle.

## Architecture

- Dependencies flow `Presentation -> Domain <- Data`; Domain stays pure Kotlin.
- Feature and library modules use public `api` and internal `impl` modules.
- Domain defines repository interfaces; Data implements them without leaking DTOs or Room entities.
- `app` is the composition root for navigation and feature wiring.
- Use injected `DispatcherProvider`; never `GlobalScope` or direct `Dispatchers.IO`.

## MVI And Coroutines

- Screens send sealed intents through `ViewModel.process(intent)` and collect immutable `StateFlow` with `collectAsStateWithLifecycle()`.
- Use `StateFlow` for durable renderable state and outcomes that require acknowledgement.
- Use non-replay `SharedFlow` for transient effects that may be dropped while the destination is inactive.
- Collect navigation effects in `RESUMED`; collect other transient effects in `STARTED` through `CollectEffectWithLifecycle`.
- Use `Channel` only when a tested single-consumer queue is an explicit requirement.
- Preserve `CancellationException`; never convert cancellation into a normal `Result.failure` or user error.
- Prevent duplicate long-lived collectors and define latest-wins behavior explicitly where applicable.

## Code Rules

- Do not add comments in production code or tests.
- Do not hardcode user-visible strings; add English and Spanish entries to `core/string-resources/src/main/res/values{,-es}/strings.xml`.
- Use design-system spacing, size, weight, and typography tokens instead of raw `dp` or `sp` values.
- Prefer immutable values and never expose mutable flows publicly.
- Avoid `!!`, business logic in Composables, and non-exhaustive sealed-class `when` statements.
- Use `LaunchedEffect(viewModel)` for ViewModel initialization.
- Keep changes minimal; do not add abstractions without a concrete reuse or boundary.

## UI And Tests

- Use test-first development for new features and behavior fixes. Before implementation, read `docs/guides/GUIDE-testing-and-tdd.md`; follow its boundary agreement, red/green evidence, and Compose test selection workflow.
- Write documentation, test names, and engineering reports in English. Keep user-facing resources localized in English and Spanish.

- Every screen handles Loading, Content, Empty, and Error where applicable.
- Add `@PreviewLightDark` previews for new public or internal Composables.
- Keep touch targets at least 48dp and use stable keys/content types in lazy layouts.
- Follow existing patterns: JUnit 5 with Kluent, MockK, and Turbine for JVM unit tests; JUnit 4 for Paparazzi screenshot tests and instrumented/journey tests.
- Cover success, failure, cancellation, empty data, and concurrency boundaries relevant to the change.
- Run affected tests first. Before release-ready claims, run the aggregate gate plus `verifyPaparazziDebug`, `:architecture-tests:test`, and the applicable connected tests; before distributing a release candidate, also run the release startup check.

## Commands

Local builds need JDK 21, the Android SDK, and uncommitted Firebase config files (`google-services.json` per build type). Pass `-PenableFirebase=false` to build and test without them.

```sh
./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistMoviesViewModelTest*'
./gradlew ktlintCheck detekt
./gradlew ktlintFormat
./gradlew :architecture-tests:test
./gradlew :koverHtmlReport :koverVerify
./gradlew verifyPaparazziDebug
./gradlew :feature:login:impl:recordPaparazziDebug --tests '*LoginScreenScreenshotTest*'
./gradlew :app:connectedJourneyTestAndroidTest
./gradlew :feature:watchlist:impl:connectedDebugAndroidTest
./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease
python3 tools/check-release-startup.py --serial <device-serial>
```

- Gradle options attach to the preceding task; one trailing `--tests` does not filter earlier tasks in a multi-task command.
- Do not run `ktlintFormat` and `ktlintCheck` in the same invocation.
- `test` does not compare Paparazzi goldens. Record one module and test class at a time, inspect every changed image, then verify separately; never commit unrelated golden changes.
- App journey tests use the `journeyTest` build type (`app/src/journeyTest`) with fake repositories through Hilt.
- The release startup check requires the signed, minified Release APK installed on an emulator; Debug and journey builds do not exercise R8.
- Kover enforces aggregate floors of 75% line and 71% branch coverage.
- The pre-commit hook (`hooks/pre-commit`, installed by `./gradlew copyGitHooks`) runs Detekt, ktlint, and Konsist when Kotlin files are staged.

## Repository Map

- `app`: `MainActivity`, navigation graph, DI wiring, and `HiltTestRunner`.
- `feature/<name>/api`: public routes and contracts. `feature/<name>/impl`: internal `presentation`, `domain`, `data`, `navigation`, and `di`; only `app` depends on `impl`.
- `core`: `domain` (pure Kotlin, including `DispatcherProvider`), `data`, `database` (Room), `network` (Retrofit/TMDB), `sync` (WorkManager and Firestore), `auth`, `ui` (`CollectEffectWithLifecycle`, `UiText`), `design-system`, `string-resources`, and `testing` (shared dispatcher and MockK extensions).
- `library/<name>/{api,impl}`: observability, remote config, and secure storage.
- `build-logic/convention`: Gradle convention plugins for Android, Compose, Hilt, Room, Paparazzi, and tests; dependencies live in the `gradle/` version catalog.
- `architecture-tests`: Konsist boundary and convention checks.
- `docs/prd/PRD.md`, `docs/plan/FIP-*.md`, `docs/guides/`, `openspec/`: product requirements, legacy feature plans, guides, and OpenSpec specs and changes.
- The TMDB API key is delivered through Firebase Remote Config, not stored in the repository.

## Planning

- Read the relevant PRD, guide, and active plan before implementation. New OpenSpec pilot changes use proposal, specs, design, and tasks under `openspec/changes/`; existing FIPs retain their original workflow. When setting up or archiving an OpenSpec change, read the OpenSpec section in `docs/README.md`.
- Make technical decisions with product judgment: prioritize realistic user flows and proportional risk, and do not add complexity for theoretically possible cases without credible product impact.
- Before hardening a corner case, state how a real user reaches it, how likely it is, and what user-visible harm it causes; prefer an explicit accepted limitation when the mitigation costs more than the risk.
- Execute the active FIP or OpenSpec task list phase by phase; check tasks only after implementation and validation. Keep one plan per change.
- Stop and ask when requirements, data sources, side effects, migration behavior, or failure UX are ambiguous.
- Planning-only work modifies documentation, not production code.
- Do not commit or push unless the user explicitly requests it.
- Never modify unrelated user changes or `.DS_Store` files.

## Implementation Summaries

- After completing an implementation, explain every changed production and test class, what changed in it, and why the change was necessary.
- Group supporting resource, build, and documentation files when appropriate, but still explain their purpose.
- Report the validation commands and results, plus any remaining manual checks, risks, or incomplete work.
