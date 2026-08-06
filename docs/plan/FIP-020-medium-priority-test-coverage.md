# FIP — Medium-Priority Test Coverage

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                    |
|------------------------|--------------------------------------------------------------------------|
| **FIP ID**             | FIP-020                                                                  |
| **Version**            | 1.0                                                                      |
| **Status**             | 🟡 Draft                                                                 |
| **PRD ref**            | Internal future quality initiative — no PRD feature                      |
| **Feature**            | Broader behavioral, visual, accessibility, and architecture coverage    |
| **Date**               | 2026-08-06                                                               |
| **Author**             | @asensiodev                                                              |
| **Definition of Done** | All checkboxes are marked `[x]`, adopted quality gates run in CI, and §10 is complete |

---

> **Execution rule:** This is a deliberately deferred draft requested for future planning. Do not begin it until its prerequisites are met and its scope is revalidated against the then-current repository.
> Work phase by phase and stop to ask if current behavior, ownership, or expected assertions are ambiguous.

---

## 0. Prerequisites

- Complete [FIP-019 — High-Risk Test Coverage](./FIP-019-high-risk-test-coverage.md) or explicitly resolve every remaining high-risk blocker.
- Read [FIP-018 — Architecture Reliability Refactor](./FIP-018-architecture-reliability-refactor.md), especially architecture, coverage, Paparazzi, accessibility, and observability tasks.
- Re-audit production/test counts and remove tasks already completed elsewhere before moving this FIP to In Progress.
- Establish a green baseline for affected JVM, Paparazzi, architecture, instrumented, lint, and build tasks.
- Assign one owning FIP for every outcome shared with FIP-018; do not implement equivalent work twice.

---

## 1. Context & Motivation

After the highest-risk SDK and persistence boundaries are covered, Santoro can improve breadth and maintainability in lower-risk areas. The current suite has strong ViewModel coverage and several component screenshots, but direct coverage remains uneven across preference use cases, catalogue use cases, observability normalization, complete screen states, accessibility semantics, and architecture policies.

This plan is intentionally secondary. It must not displace authentication, persistence, migration, scheduling, networking, or app-navigation regressions tracked by FIP-019.

---

## 2. Goals

- Complete meaningful direct coverage for executable preference and catalogue use-case behavior.
- Verify the Firebase observability adapter’s normalization, truncation, user, error, and privacy boundaries.
- Expand visual regression coverage from isolated components to representative complete screen states.
- Add focused feature interaction tests for Login, Search, and Settings.
- Add automated accessibility semantics checks for custom interactive components and critical journeys.
- Strengthen architecture tests around actual dependency, visibility, model-boundary, and MVI conventions.
- Make Paparazzi, architecture, lint, and affected instrumented suites explicit and visible in CI.
- Remove stale/example test artifacts that do not protect product behavior.

---

## 3. Non-Goals

- No new product functionality or visual redesign.
- No attempt to snapshot every composable, device, locale, font scale, orientation, or state.
- No claim of WCAG AA compliance based only on automated tests.
- No strict reducer/store MVI conversion or additional feature-domain modules.
- No tests for trivial one-line delegation unless the class owns dispatcher, mapping, fallback, cancellation, or policy behavior.
- No generated-code, DI, route-object, constant, or data-class tests solely for coverage.
- No replacement of Firebase observability, Paparazzi, Konsist, Detekt, ktlint, Android Lint, Kover, or Codecov.
- No performance, macrobenchmark, baseline-profile, security-scanning, or continuous-delivery initiative.
- No automatic increase to aggregate or patch coverage thresholds.

---

## 4. User Stories

| ID    | As a… | I want to… | So that… | Acceptance Criteria |
|-------|-------|------------|----------|---------------------|
| US-01 | developer | trust domain policy tests | untested branches do not hide behind mocked ViewModels | Every selected use case has direct tests for its meaningful policy, dispatcher, result, and cancellation behavior |
| US-02 | developer | detect visual state regressions | Loading, Content, Empty, Error, and destructive dialogs remain reviewable | A bounded representative screen matrix is verified by Paparazzi |
| US-03 | user | receive accessible custom controls and state feedback | critical actions remain understandable with assistive technology | Selected custom controls expose role, label, selected/state semantics, and minimum touch targets with automated assertions |
| US-04 | maintainer | detect architectural drift | package conventions remain enforceable as the project grows | New semantic Konsist rules fail against representative prohibited examples and pass current intended tradeoffs |
| US-05 | maintainer | trust telemetry shape and privacy boundaries | analytics failures or sensitive parameters are caught before release | Observability adapter behavior and approved parameter policy are directly tested |

---

## 6. Architecture

```text
Behavioral breadth
    ├── Domain policy tests
    ├── Observability adapter tests
    ├── Complete-screen Paparazzi tests
    ├── Focused Compose interaction/accessibility tests
    └── Semantic architecture tests

Each layer remains a separate quality signal; none substitutes for another.
```

---

## 8. Modules Affected

- `architecture-tests`
- `core/data`
- `core/design-system`
- `core/domain`
- `feature/login/impl`
- `feature/search-movies/impl`
- `feature/settings/impl`
- `feature/movie-detail/impl`
- `feature/watchlist/impl`
- `feature/watched-movies/impl`
- `library/observability/impl`
- `build-logic/convention` only for common adopted test configuration
- `gradle/libs.versions.toml` only for required test libraries
- `.github/workflows/ci.yml`
- `AGENTS.md` and `README.md` only when measured policy or commands change
- `docs/plan/FIP-018-architecture-reliability-refactor.md` only for genuinely shared completed work

No new Gradle module is planned. The orphaned `core/android-testing` directory must be removed or separately justified; this FIP does not activate it by default.

---

## 9. Phases & Tasks

### Phase 1 — Rebaseline and Select Meaningful Coverage

**Data sources**
- Current production classes and branches.
- Existing test, Kover, Paparazzi, architecture, lint, and CI reports.
- FIP-018 and FIP-019 completion evidence.

**Side effects**
- ✅ Allowed:
    - Run read-only inventory and verification tasks.
    - Remove stale scope from this draft before execution.
- ❌ Forbidden:
    - Select classes because they are easy coverage wins.
    - weaken assertions, exclusions, or thresholds to establish the baseline.

- [ ] Recalculate active test files/methods, screenshot cases/baselines, use-case coverage, and architecture-rule count.
- [ ] Generate module/class Kover reports and identify executable uncovered policy rather than trivial declarations.
- [ ] Define the bounded screen-state and accessibility test matrix in §13 before adding screenshots.
- [ ] Reconcile every shared FIP-018 task and record its owning plan.
- [ ] Delete or replace the inactive `core/android-testing` example test without counting it as a quality improvement.
- [ ] Identify orphaned Paparazzi baselines and either restore their source tests or remove the stale images.

### Phase 2 — Preference and Domain Policy Tests

**Data sources**
- In-memory fakes/mocks of existing repository interfaces.
- Temporary test preference storage where repository behavior itself is tested.
- Existing use-case inputs and outputs.

**Side effects**
- ✅ Allowed:
    - Invoke existing repository/use-case APIs with deterministic values and coroutine dispatchers.
    - Read/write isolated temporary preference state.
- ❌ Forbidden:
    - Add new preference keys, use cases, repository methods, network calls, or product behavior.
    - Test pure delegation that has no meaningful policy solely to satisfy a percentage target.

- [ ] Complete `DefaultUserPreferencesRepositoryTest` for detail-tooltip state, theme fallback, writes, expected failures, and cancellation where supported.
- [ ] Add direct tests for `ObserveHasSeenDetailTooltipUseCase`, `SetDetailTooltipSeenUseCase`, `ObserveThemeUseCase`, and `SetThemeUseCase` only where dispatcher/result behavior is meaningful.
- [ ] Add direct tests for `GetTopRatedMoviesUseCase`, `GetTrendingMoviesUseCase`, `GetUpcomingMoviesUseCase`, `GetNowPlayingMoviesUseCase`, and `GetMoviesByGenreUseCase`.
- [ ] Cover cache/refresh policy, dispatcher use, result propagation, and cancellation for each selected catalogue use case.
- [ ] Review untested auth use cases after FIP-019 and add direct tests only for behavior not already guaranteed at repository/adapter boundaries.
- [ ] Record any intentionally untested trivial delegation with rationale instead of forcing 100% class-count coverage.

### Phase 3 — Observability Contract and Privacy Tests

**Data sources**
- Mocked Firebase Analytics and Crashlytics SDK collaborators.
- Synthetic event, screen, parameter, user, and Throwable values containing no real user data.
- The telemetry allowlist adopted in this phase.

**Side effects**
- ✅ Allowed:
    - Invoke the existing observability API against mocked SDK collaborators.
    - Add a static allowlist/normalization contract through the existing observability boundary if required by approved tests.
- ❌ Forbidden:
    - Send events to Firebase.
    - Include credentials, tokens, email addresses, raw search text, or private user/movie data in fixtures.
    - Add new production events or user-identification behavior.

- [ ] Test event-name normalization, invalid/blank fallback, length limits, parameter-key normalization, and value truncation.
- [ ] Test screen/action/error forwarding and exact Analytics/Crashlytics interactions.
- [ ] Test registered, anonymous, and cleared user identity/property behavior.
- [ ] Test Throwable recording and custom-key handling, including stale metadata boundaries.
- [ ] Define and test an approved telemetry parameter allowlist.
- [ ] Confirm tests and failure output cannot expose synthetic credential-like values.

### Phase 4 — Complete-Screen Visual Regression Matrix

**Data sources**
- Deterministic `UiState` fixtures and callbacks.
- English and Spanish string resources already shipped by the app.
- Existing `SantoroTheme`, previews, design tokens, and Paparazzi convention.

**Side effects**
- ✅ Allowed:
    - Render stateless screen composables with deterministic state.
    - Record and verify approved Paparazzi baselines.
- ❌ Forbidden:
    - Call ViewModels, repositories, navigation controllers, network, database, or Firebase from screenshot tests.
    - Redesign UI to make snapshots easier.
    - Generate an unbounded Cartesian product of themes, locales, devices, and states.

- [ ] Define one representative device and explicit exceptions before recording baselines.
- [ ] Add complete-screen Loading, Content, Empty/NoResults, and Error coverage where each state exists.
- [ ] Include destructive dialogs and blocking loading states for Login/Settings/Profile where user risk justifies them.
- [ ] Include a bounded light/dark matrix for critical screens and a bounded Spanish/long-text case for layout risk.
- [ ] Include one large-font case for the highest-risk screen without treating it as full accessibility validation.
- [ ] Review baseline images for deterministic content, clipping, contrast regressions, and accidental sensitive data.
- [ ] Keep component snapshots that protect reusable behavior; remove only proven duplication or orphaned artifacts.

### Phase 5 — Missing Feature Interaction Tests

**Data sources**
- Existing screen state, intents, effects, and deterministic fake dependencies.
- Existing Compose semantics tree and string resources.

**Side effects**
- ✅ Allowed:
    - Render existing feature routes/screens with controlled dependencies.
    - Perform taps, text input, scrolling, retry, confirmation, and navigation callbacks.
- ❌ Forbidden:
    - Contact live services or duplicate app-level navigation journeys from FIP-019.
    - Add test-only production branches or change UX behavior without a reproduced defect and plan amendment.

- [ ] Add Login feature tests for anonymous sign-in, Google action dispatch, loading lockout, and error feedback.
- [ ] Add Search feature tests for query entry, debounce-visible outcomes, recent/trending suggestions, retry, pagination, and navigation callbacks.
- [ ] Add Settings/Profile tests for theme selection, account-link confirmation, logout, delete-account confirmation, loading, and error feedback.
- [ ] Extend existing Watchlist, Watched Movies, and Movie Detail feature tests only for unprotected Empty/Error/dialog/retry behavior.
- [ ] Verify tests assert user-observable behavior or semantics rather than implementation details.

### Phase 6 — Accessibility Semantics and Interaction Quality

**Data sources**
- Compose semantics tree.
- Existing localized labels and design-system tokens.
- Android accessibility scanner/manual TalkBack observations recorded during this phase.

**Side effects**
- ✅ Allowed:
    - Add or correct role, label, selected/state, heading, live-region, and merged semantics for existing UI behavior.
    - Adjust touch-target layout using existing design tokens without redesigning flows.
- ❌ Forbidden:
    - Claim WCAG AA compliance from automated assertions alone.
    - Add hardcoded strings or raw dimensions.
    - Change product copy or visual hierarchy without approval.

- [ ] Audit custom bottom navigation, icon-only actions, posters, dialogs, loading/error feedback, and swipe actions.
- [ ] Add semantics tests for labels, roles, selected state, disabled/loading state, and actionable controls.
- [ ] Add minimum-touch-target assertions for selected custom controls where Compose test APIs can verify bounds reliably.
- [ ] Verify decorative images remain excluded while informative images have localized descriptions.
- [ ] Add one TalkBack journey each for authentication, search-to-detail, and a destructive Settings action.
- [ ] Test 200% font scale on critical screens and record clipping/truncation observations.
- [ ] Perform a contrast-tool check for changed/critical color pairs and record evidence without overstating full-app compliance.

### Phase 7 — Architecture Enforcement

**Data sources**
- `settings.gradle.kts`, module build files, production packages, and visibility modifiers.
- Existing Konsist rules and explicitly accepted architecture tradeoffs in FIP-018.

**Side effects**
- ✅ Allowed:
    - Add semantic Konsist rules and focused test fixtures where supported.
    - Document intentional exceptions narrowly.
- ❌ Forbidden:
    - Force new modules, strict reducer MVI, or architecture changes rejected by FIP-018.
    - Use broad substring checks where semantic inspection is available.
    - Add blanket exceptions that make a rule non-actionable.

- [ ] Enforce that shared domain production code remains Android-free without limiting the check to one hardcoded path accidentally.
- [ ] Add rules for forbidden feature-to-feature `impl` dependencies and direct presentation-to-data imports.
- [ ] Add rules preventing Room entities and API DTOs from leaking outside approved data packages.
- [ ] Add rules for private mutable/public immutable `StateFlow` exposure and lifecycle-aware collection.
- [ ] Add rules for feature implementation visibility and API route placement where current intended exceptions are explicit.
- [ ] Add rules for repository interface/implementation placement only where the current pragmatic architecture consistently supports them.
- [ ] Remove duplicated textual `GlobalScope` enforcement after equivalent semantic coverage exists.
- [ ] Add negative fixture tests or another deterministic proof that each new rule fails on its prohibited pattern.

### Phase 8 — CI Visibility and Final Verification

**Data sources**
- Adopted Gradle task graph and generated reports.
- Existing CI, Kover, Codecov, Detekt, ktlint, Android Lint, Paparazzi, and emulator configuration.

**Side effects**
- ✅ Allowed:
    - Add explicit CI tasks/jobs and non-sensitive artifacts for adopted quality signals.
    - Update measured documentation and shared FIP completion evidence.
- ❌ Forbidden:
    - Add deployment/CD behavior.
    - Upload screenshots or reports containing sensitive data.
    - Make Codecov or Kover stricter without a measured, separately approved threshold decision.

- [ ] Add explicit Paparazzi verification so visual failures are identifiable independently from generic JVM tests.
- [ ] Add Android Lint to CI and publish non-sensitive reports on failure.
- [ ] Run architecture tests explicitly and keep their result visible.
- [ ] Run all newly affected instrumented feature modules on the CI emulator.
- [ ] Run `./gradlew ktlintCheck detekt test assembleDebug` plus explicit lint, Paparazzi, architecture, Kover, and instrumented tasks.
- [ ] Record current aggregate and changed-line coverage without using it as the sole acceptance criterion.
- [ ] Update README quality claims to match actual screen/component, architecture, accessibility, and CI scope.
- [ ] Complete §10 with exact results and explain every skipped manual check.

---

## 10. Validation

| What | Result | Notes |
|------|--------|-------|
| Preference/domain policy tests | ⏳ | Record selected nontrivial contracts and tasks |
| Observability tests | ⏳ | Record SDK interactions and privacy review |
| Paparazzi verification | ⏳ | Record case/baseline count and task |
| Feature interaction tests | ⏳ | Record modules and emulator/API level |
| Accessibility semantics tests | ⏳ | Record automated checks |
| TalkBack and font-scale checks | ⏳ | Record device/API and journeys |
| Contrast review | ⏳ | Record tool and tested color pairs |
| Architecture tests | ⏳ | Record rules and negative-rule evidence |
| Android Lint, Detekt, and ktlint | ⏳ | Record exact commands |
| Kover/Codecov reporting | ⏳ | Record measured values without unsupported claims |
| Full debug build | ⏳ | Record exact command and result |

---

## 11. Blockers

| # | Blocker | Raised | Resolved | Impact |
|---|---------|--------|----------|--------|
| 1 | FIP-019 high-risk coverage is not complete | 2026-08-06 | Open | This FIP must remain Draft unless the owner explicitly reprioritizes unresolved high-risk work |

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should every use case have a dedicated test class? | No. Test meaningful policy; document trivial delegation rather than optimizing class counts |
| 2 | Should every screen state be rendered in every theme and locale? | No. Adopt a bounded risk-based matrix in Phase 1 |
| 3 | Can automated semantics and screenshots prove accessibility compliance? | No. They are regression signals and require manual TalkBack/font/contrast evidence |
| 4 | Should architecture tests enforce textbook Clean Architecture? | No. They enforce Santoro’s documented pragmatic boundaries and accepted FIP-018 decisions |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Keep this FIP Draft until high-risk work is resolved | Execute broad quality work immediately | Authentication, persistence, scheduling, networking, migration, and navigation boundaries carry higher regression risk |
| 2 | Select tests by behavior and risk | Enforce one test class per production class | Class-count targets reward trivial tests and do not prove useful behavior |
| 3 | Use a bounded visual matrix | Snapshot every permutation | Review cost and flaky baseline volume must remain sustainable |
| 4 | Combine automated and manual accessibility evidence | Claim compliance from semantics tests | Accessibility requires assistive-technology, font, and contrast validation beyond unit/UI assertions |
| 5 | Enforce documented pragmatic architecture | Enforce strict Clean Architecture/reducer MVI | Tests should protect intentional design, not force an unapproved rewrite |

---

## 14. Out of Scope / Follow-ups

- Performance benchmarks, baseline profiles, startup tracing, and macrobenchmarks.
- Firebase Emulator, Firestore rules, App Check, and backend security verification.
- Dependency/secret scanning, dependency verification, and supply-chain hardening unless separately planned.
- Release signing, Play delivery, and continuous deployment.
- Full-device, full-locale, full-orientation, or exhaustive accessibility certification matrices.

---

## 15. Handover Notes

- This document intentionally exists as a future draft at the repository owner’s request despite the normal preference for keeping distant ideas as follow-ups.
- Re-audit before execution; counts, gaps, FIP-018 ownership, and screen contracts may have changed.
- Prefer improving a meaningful existing test over adding a new shallow test class.
- Keep screenshot fixtures deterministic and free of network image loading, clocks, random values, and sensitive content.
- Never convert documented limitations into CV claims until implementation and validation evidence exist.

---

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-06 | Initial deferred medium-priority quality and test plan |
