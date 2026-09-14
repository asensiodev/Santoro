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
- Do not hardcode user-visible strings; add English and Spanish string resources.
- Use design-system spacing, size, weight, and typography tokens instead of raw `dp` or `sp` values.
- Prefer immutable values and never expose mutable flows publicly.
- Avoid `!!`, business logic in Composables, and non-exhaustive sealed-class `when` statements.
- Use `LaunchedEffect(viewModel)` for ViewModel initialization.
- Keep changes minimal; do not add abstractions without a concrete reuse or boundary.

## UI And Tests

- Every screen handles Loading, Content, Empty, and Error where applicable.
- Add `@PreviewLightDark` previews for new public or internal Composables.
- Keep touch targets at least 48dp and use stable keys/content types in lazy layouts.
- Follow existing JUnit 5, Kluent, MockK, Turbine, Paparazzi, and journey-test patterns.
- Cover success, failure, cancellation, empty data, and concurrency boundaries relevant to the change.
- Run affected tests first, then `test detekt ktlintCheck koverVerify assembleDebug assembleRelease` before release-ready claims.

## Planning

- Read the relevant PRD, brief, FIP, and guide before implementation.
- Make technical decisions with product judgment: prioritize realistic user flows and proportional risk, and do not add complexity for theoretically possible cases without credible product impact.
- Before hardening a corner case, state how a real user reaches it, how likely it is, and what user-visible harm it causes; prefer an explicit accepted limitation when the mitigation costs more than the risk.
- Execute FIPs phase by phase; check tasks only after implementation and validation.
- Stop and ask when requirements, data sources, side effects, migration behavior, or failure UX are ambiguous.
- Planning-only work modifies documentation, not production code.
- Do not commit or push unless the user explicitly requests it.
- Never modify unrelated user changes or `.DS_Store` files.

## Implementation Summaries

- After completing an implementation, explain every changed production and test class, what changed in it, and why the change was necessary.
- Group supporting resource, build, and documentation files when appropriate, but still explain their purpose.
- Report the validation commands and results, plus any remaining manual checks, risks, or incomplete work.
