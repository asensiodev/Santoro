# Watchlist Surprise implementation evidence

Date: 2026-10-19. Change: `surprise-watchlist-movie`. PRD: F-30 within F-04.

The initial implementation passed focused validation and all repository aggregate gates. The subsequent dice artwork change passed affected static checks and independent screenshot verification, as recorded below. Manual and external review remain pending. Publication scope: documentation only. Production code, tests and image baselines remain uncommitted in the local working tree; this documentation does not publish the implementation. The change remains unarchived for human review.

## Behavior and boundaries

The user confirmed ViewModel intents/state, plain Compose callbacks, app navigation and light/dark screenshots with “dale” before any tests were added. Suggestions read the loaded Watchlist, use standard-library randomness, avoid immediate repetition, and never write movie state or schedule sync. The existing local-list collector owns updates; selection starts no coroutine. The existing image loader handles posters and missing images. Process-death restoration of a temporary choice remains an accepted limitation.

## Changed production classes

| Class or component | Change and reason |
|---|---|
| WatchlistIntent | Adds SuggestMovie and DismissSuggestion so actions follow the existing MVI entry point. |
| WatchlistMoviesUiState | Adds nullable suggestedMovie to keep the visible choice in retained immutable screen state. |
| WatchlistMoviesViewModel | Selects only from unfiltered Content, excludes the immediately previous ID when alternatives exist, dismisses explicitly/on query or failure, and reconciles selection against local updates. This keeps the sheet consistent without persistence, extra collectors or services. |
| WatchlistMoviesScreen | Shows the action only for populated unfiltered content; renders a standard modal sheet and dismisses before invoking the existing detail callback. |
| WatchlistSurpriseContent | Adds plain scrollable content with the existing movie row, crossfade, details action and conditional another action, plus a light/dark preview. Uses existing typography, spacing and size tokens, including 48dp action targets. |
| AppIcons | Exports the Material Casino dice icon through the existing design-system pattern, reusing the dependency already installed there. The dice replaces the initial shuffle artwork after user feedback. No dependency changes. |

## Changed test classes

| Class | Purpose |
|---|---|
| WatchlistSurpriseViewModelTest | Nine tests cover single choice, repeated two-entry requests, no writes/sync, dismissal, immediate search guarding, failure guarding, removal, failure while open, metadata refresh and loading/empty data. |
| WatchlistSurpriseScreenTest | Eight plain UI tests cover action callbacks and visibility, detail callback order, another selection and rendered transition, single-entry controls, Back dismissal, unavailable states, and Spanish action strings. |
| MainActivityNavigationJourneyTest | Adds one full-app journey proving the selected ID opens in detail and Back returns to a closed panel. Existing navigation journeys remain included. |
| WatchlistSurpriseContentScreenshotTest | Adds four deterministic Pixel 3 captures: one/multiple entries in both themes, with null remote posters and restored locale. |

The existing WatchlistMoviesScreenScreenshotTest source is unchanged. Its two Content baselines were intentionally updated for the new action. The four new component baselines and both changed screen baselines were visually inspected: text, controls, alignment and contrast have no visible clipping. Component captures exercise plain content rather than the platform sheet window or its backdrop.

## Supporting files

English and Spanish string resources add the action, heading, another choice and detail labels. PRD F-30 records the feature. The OpenSpec proposal, delta specification, design, tasks and this evidence keep scope, decisions, validation and pending review together. No schema, repository, account, cloud or build configuration changes are needed.

## Test-first evidence

Each row represents an observed behavioral failure followed by a minimal implementation and a passing rerun. Compiling no-op intent scaffolding enabled the first assertion failure; production code was not deliberately broken to manufacture red.

| Behavior | Red run | Green run |
|---|---:|---:|
| Suggest the single saved movie; title initially null rather than Arrival | 2 | 3 |
| Avoid repeating the previous movie under rapid requests; ID 1 observed instead of 2 | 5 | 6 |
| Dismiss the current suggestion | 7 | 8 |
| Clear and block suggestions immediately when a search query is entered | 9 | 10 |
| Prevent suggesting stale content after a local failure | 11 | 12 |
| Close when the chosen ID disappears from the local stream | 13 | 14 |
| Close an open suggestion on local failure | 15 | 16 |
| Render and dispatch the populated Watchlist action | 17 | 19 |
| Hide the action while searching; assertDoesNotExist found the button before the fix | 20 | 21 |
| Render the suggestion panel and dismiss before the detail callback; heading was not displayed before the fix | 22 | 23 |

Metadata refresh, loading/empty guards and the remaining UI/journey checks passed when added after the relevant implementation. They are additional regression/integration coverage, not retrospectively claimed as TDD.

Runs 1 and 4 were fixture compiler failures (missing isWatched and a MockK DSL reference); run 18 identified the need to export Shuffle from the design system. Run 24 exposed duplicate title nodes in the test fixture and an invalid cross-APK class filter; the assertion was scoped to the dialog and each APK ran its own full suite. These are not behavioral red evidence. Run 26 failed detekt on the preview rating literal; a named preview constant fixed it before run 27. A wrapper CLI preflight rejected scope “aggregate” before starting Gradle; the correct scope is “broad”.

## Validation questions

- Q1: "Does selecting the only saved movie expose its title without changing the list?" Yes: run 3 passed the single-entry state contract.
- Q2: "Do rapid requests avoid repeating the previous movie without writes or sync?" Yes: run 6 passed twenty consecutive two-entry requests and confirmed no mutation/sync interactions.
- Q3: "Does Watchlist surprise meet the tested behavior?" Yes: nine state tests, ten connected Watchlist tests and eighteen app journeys passed, including the new UI and selected-detail journey.
- Q4: "Do the affected checks and intentional Watchlist screenshot recordings pass?" Yes: run 27 passed affected detekt/ktlint and fourteen intentional screen/component captures; six baseline images changed and all six were inspected.
- Q5: "Do all repository gates and independent screenshot verification pass?" Yes: run 28 exited 0 in 150.439 seconds for test, detekt, ktlintCheck, koverVerify, assembleDebug, assembleRelease and root verifyPaparazziDebug.

Focused outcome: 9/9 new ViewModel tests, 10/10 Watchlist connected tests (8 new and 2 existing), and 18/18 app journeys passed on Pixel_9a API 37. Run 27 passed affected detekt/ktlint and intentional screenshot recording. Run 28 independently verified all repository screenshots and passed every aggregate gate. Firebase was disabled through the established test flag; no live Firebase or Play validation is claimed. Remaining native-access, Gradle deprecation and existing API deprecation warnings did not fail the gates.

## Reproducible command ledger

Managed wrapper: `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow 177b895e99ce4d54e265be035d07dd70 --scope <scope> --question "<question>" -- <nested command>`.

Every nested command below includes this common suffix, omitted from table rows only for readability:

```text
-Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan
```

| Run | Scope | Question | Exit | Nested command before common suffix |
|---:|---|---|---:|---|
| 1 | targeted | Q1 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.single saved movie*'` |
| 2 | targeted | Q1 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.single saved movie*'` |
| 3 | targeted | Q1 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.single saved movie*'` |
| 4 | targeted | Q2 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.another suggestion*'` |
| 5 | targeted | Q2 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.another suggestion*'` |
| 6 | targeted | Q2 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 7 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.dismissing*'` |
| 8 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 9 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.entering*'` |
| 10 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 11 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.failed content*'` |
| 12 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 13 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.removing*'` |
| 14 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 15 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest.a local failure*'` |
| 16 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:testDebugUnitTest --tests '*WatchlistSurpriseViewModelTest*'` |
| 17 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:testDebugUnitTest :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` |
| 18 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` |
| 19 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` |
| 20 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest#filteredWatchlistDoesNotOfferSurpriseAction` |
| 21 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` |
| 22 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest#suggestedMovieOpensItsDetailAfterDismissingThePanel` |
| 23 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` |
| 24 | targeted | Q3 | 1 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest :app:connectedJourneyTestAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest,com.asensiodev.santoro.MainActivityNavigationJourneyTest` |
| 25 | targeted | Q3 | 0 | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest :app:connectedJourneyTestAndroidTest` |
| 26 | targeted | Q4 | 1 | `./gradlew :feature:watchlist:impl:ktlintFormat :app:ktlintFormat :core:design-system:ktlintFormat :feature:watchlist:impl:testDebugUnitTest :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*' --tests '*WatchlistSurpriseContentScreenshotTest*'` |
| 27 | targeted | Q4 | 0 | `./gradlew :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*' --tests '*WatchlistSurpriseContentScreenshotTest*'` |

| 28 | broad | Q5 | 0 | `./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease verifyPaparazziDebug` |

OpenSpec strict validation and git diff --check also passed. The agent-created headless emulator was stopped after the connected tests.

The managed workflow finished successfully. Its cleanup was restricted to wrapper-owned logs; repository evidence and build reports were preserved.

## Pending review

- All automated gates are complete; the OpenSpec change remains unarchived for human review.
- Manual sheet swipe/outside-tap dismissal, rotation, physical-device and large-font review remain pending. Back dismissal and the choice transition have automated coverage.
- Signed minified Release startup, Internal/Play artifact checks, publication and human review have not been performed. Building Release does not substitute for these checks.
- Keep the change unarchived until required human review is complete.

## Icon follow-up

The user requested a documentation-only push and a better visual for Surprise me. The initial Shuffle arrows were replaced locally with the standard Material Casino dice in AppIcons, WatchlistMoviesScreen and WatchlistSurpriseContent. This changes decorative artwork only; labels, callbacks, state and accessibility text are unchanged. All six changed captures were inspected again. Affected detekt/ktlint checks and independent Watchlist/design-system Paparazzi verification passed. The previous aggregate gate is evidence for the pre-icon-change implementation, not a fresh aggregate claim for this follow-up.

Question: "Do the dice icon captures and affected static checks pass?" Yes: follow-up runs 2 and 3 exited 0. Run 1 reported import ordering while formatting and checking were scheduled together; the formatted imports were inspected, and subsequent checks ran without concurrent format tasks. No new behavioral TDD claim applies to this artwork-only update.

Managed wrapper: `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow 7a5158511c4f07447af8ce4cd4a47250 --scope targeted --question "Do the dice icon captures and affected static checks pass?" -- <nested command>`. The common JDK/Firebase/console suffix above applies.

| Follow-up run | Exit | Seconds | Nested command before common suffix |
|---:|---:|---:|---|
| 1 | 1 | 5.566 | `./gradlew :core:design-system:ktlintFormat :feature:watchlist:impl:ktlintFormat :core:design-system:detekt :feature:watchlist:impl:detekt :core:design-system:ktlintCheck :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*' --tests '*WatchlistSurpriseContentScreenshotTest*'` |
| 2 | 0 | 17.914 | `./gradlew :core:design-system:detekt :feature:watchlist:impl:detekt :core:design-system:ktlintCheck :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*' --tests '*WatchlistSurpriseContentScreenshotTest*'` |
| 3 | 0 | 7.79 | `./gradlew :feature:watchlist:impl:verifyPaparazziDebug :core:design-system:verifyPaparazziDebug` |
