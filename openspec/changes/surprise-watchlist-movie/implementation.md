# Watchlist Surprise implementation evidence

Publication update, 2026-10-26: the user explicitly authorized committing and pushing the implementation after accepting the UI refinements. Earlier documentation-only/uncommitted statements below describe historical scope, not this latest authorization. The change remains unarchived and retains its manual/external gates.

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

## Compact header follow-up — 2026-10-26

The user approved the compact text-only entry action beside the count, wrapping below it when necessary, and hiding it for a single movie. The sheet's decorative heading icon is unchanged. No commit, push or archive was requested or performed.

Changed classes in this follow-up:

- `WatchlistMoviesScreen`: moves the action into the existing lazy-list header using `FlowRow`, removes its decorative icon, preserves a minimum 48dp height and existing intent callback, and shows it only for unfiltered content with at least two movies. The existing sheet and ViewModel behavior remain unchanged.
- `WatchlistSurpriseScreenTest`: adds a single-entry absence test, adjusts the action-click and search fixtures to two entries so they exercise the intended guards, and resolves expected labels from localized resources so the same assertions work on English and Spanish devices. The explicit Spanish translation assertions remain literal.
- `WatchlistMoviesScreenScreenshotTest`: keeps single-entry captures and adds two-entry and Spanish 200% font-scale scenarios in both themes. Six new or changed captures were inspected; the compact header aligns normally, wraps at large font size and has no clipped action text. Independent module screenshot verification passed.

Supporting files: the PRD and existing OpenSpec proposal, specification, design and task list reflect the approved refinement. No string, dependency, persistence or navigation changes were needed.

Managed workflow: `42cfd9910cd4450bfe8f1a009d03ef19`, using `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow <id> --scope targeted --question "<question>" -- <command>`. All commands use the JDK, in-process compiler and Firebase-disabled suffix documented above.

| Run | Question | Command before common suffix | Result |
|---|---|---|---|
| 1 | Is the Surprise me action absent for a single saved movie? | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest#singleMovieWatchlistDoesNotOfferSurpriseAction -Pandroid.testInstrumentationRunnerArguments.deviceSerial=emulator-5554` | Behavioral red on emulator: `assertDoesNotExist` found the action. The runner serial argument did not restrict device selection. |
| 2 | Is the Surprise me action absent for a single saved movie? | Same as run 1 | Green after the visibility guard; exit 0. |
| 3 | Do the compact header callbacks and visibility pass the Watchlist Compose tests? | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` | Emulator 9/9; Spanish physical device 4/9 because existing English literal assertions did not match localized UI. |
| 4 | Do Watchlist header screenshots record and formatting pass? | `./gradlew :feature:watchlist:impl:ktlintFormat :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*'` | Exit 0; recording only, followed by image inspection. |
| 5 | Do affected unit tests, static checks and independent Watchlist screenshots pass? | `./gradlew :feature:watchlist:impl:testDebugUnitTest :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:verifyPaparazziDebug` | Yes; exit 0. |
| 6 | Do the nine Watchlist Compose tests pass on the English test emulator? | Run 3 command plus `-Pandroid.injected.device.serial=emulator-5554` | Emulator 9/9, but the property also failed to exclude the Spanish physical device; aggregate exit 1. No production change was inferred from these locale failures. |
| 7 | Does the localized Compose test source format successfully? | `./gradlew :feature:watchlist:impl:ktlintFormat` | Yes; exit 0. |
| 8 | Do the localized Watchlist Compose tests and affected static checks pass on both connected devices? | `./gradlew :feature:watchlist:impl:connectedDebugAndroidTest :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` | Yes; exit 0 after replacing locale-dependent English assertions with resource lookups. |
| 9 | Do the localized Watchlist Compose tests and affected static checks pass on both connected devices? | Same as run 8, after strengthening the filtered fixture to two results | Emulator 9/9; physical device failed seven tests with `No compose hierarchies found in the app`; aggregate exit 1. Stopped device reruns rather than changing production code for a test-Activity availability failure. Latest physical-device validation remains unresolved. |

The full release aggregate, root screenshots, navigation journeys and manual sheet/rotation checks were not rerun for this focused layout refinement. Earlier gate results remain historical, not fresh release-readiness evidence. Human acceptance and external release gates remain pending.

## Reachable bottom action follow-up — 2026-10-26

The user approved moving the compact text-only entry action from the header to the bottom end for one-handed reach. This supersedes the preceding header placement, not its single-entry or search visibility rules. No commit, push or archive was requested or performed.

### Changed classes and supporting files

- `WatchlistMoviesScreen`: restores the count-only list header and removes its action plumbing and `FlowRow`. The movie list takes remaining height with `Modifier.weight(1f)`; a non-scrolling tonal button follows it, aligned to the end, with a 48dp minimum height. The existing screen spacing reserves the button's actual measured height, so no fixed overlay clearance or new abstraction is needed. The parent app Scaffold already supplies navigation/system inset padding. The button keeps its existing intent and availability guards; sheet, navigation and ViewModel logic are unchanged.
- `WatchlistSurpriseScreenTest`: adds a 20-movie scrolling contract. After reaching the last movie, the action must remain visible, dispatch `SuggestMovie` when clicked and lie below the last movie's semantics bounds. This failed before implementation because the header action was scrolled out of view and passed after pinning it outside the list.
- `WatchlistMoviesScreenScreenshotTest`: updates multi-entry and Spanish 200% font-scale captures and adds both variants with a test-only Scaffold using the real design-system `BottomNavigationBar`, matching app tab icons, labels and selection. Eight new or changed captures were inspected in light/dark; the action remains above navigation without clipping or overlap. This is visual shell composition coverage, not a replacement for a full-app navigation journey.
- PRD and existing OpenSpec proposal/specification/design/tasks now describe the bottom placement. Earlier header evidence and baseline decisions remain explicitly historical.

### Validation questions and commands

Managed workflow: `86668e22040ee72fc8872c7dc3a24228`, invoked with `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow <id> --scope targeted --question "<question>" -- <command>`.

Connected tests use a temporary `gradlew-santoro-emulator` launcher outside the repository. It exports `ANDROID_SERIAL=emulator-5554` and execs the repository's Gradle wrapper, restricting execution to the agent-started Pixel_9a API 37 emulator without touching the connected physical phone. An initial preflight rejected `env ANDROID_SERIAL=... ./gradlew` because the managed wrapper requires a Gradle launcher; no Gradle command ran during that rejection.

All commands retain the documented JDK 21, in-process Kotlin compiler, Firebase-disabled and console suffixes above.

| Run | Question | Tasks/options before common suffix | Outcome |
|---|---|---|---|
| 1 | Does the suggestion action stay reachable without covering the last movie after scrolling? | `:feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest#surpriseActionRemainsReachableAfterScrollingToTheLastMovie` | Behavioral red: the scrolled-away header action was not displayed; exit 1. |
| 2 | Same as run 1 | Same as run 1 | Green after bottom placement; exit 0. |
| 3 | Do the bottom-action screen and navigation-shell captures record successfully? | `:feature:watchlist:impl:ktlintFormat :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*'` | Formatter reported screenshot-source wrapping/line length; inspected its source and dedicated ktlint report before fixing. Not behavioral red. |
| 4 | Same as run 3 | Same as run 3 | Formatter reported an overlong Android test assertion; split the bounds read into a local value after inspecting source/report. Not behavioral red. |
| 5 | Same as run 3 | Same as run 3 | Yes; exit 0. Recorded 18 screen captures, then inspected all eight new/changed images. Recording is not verification. |
| 6 | Do all Watchlist Compose and unit tests, static checks and independent screenshots pass? | `:feature:watchlist:impl:connectedDebugAndroidTest :feature:watchlist:impl:testDebugUnitTest :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:verifyPaparazziDebug -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` | Yes for the selected scope: 10/10 surprise Compose tests on emulator, module unit tests, detekt, ktlint and independent module screenshots; exit 0. Other connected test classes were not included. |

OpenSpec strict validation and `git diff --check` are completion checks for this follow-up. The managed workflow cleanup removes only wrapper-owned logs. The temporary launcher is removed and the agent-started emulator stopped after validation.

Remaining gates: human one-handed usability review on a physical device, manual sheet/rotation checks, full-app journeys, root screenshot verification and release aggregates were not rerun for this focused layout-only follow-up. No fresh release-ready or physical-device validation claim is made. Keep the change unarchived for human acceptance.

## Floating overlay correction — 2026-10-26

The user clarified that movies must scroll behind the action rather than stop at a reserved strip, and that the final movie must remain fully reachable. This supersedes the reserved-strip layout above. Reused the previously agreed plain-screen and Paparazzi boundaries; no commit, push or archive was requested.

### Changed classes

- `WatchlistMoviesScreen`: uses a transparent content Scaffold with zero additional window insets. Its bottom-bar slot contains only the end-aligned, elevated text button and spacing; no opaque full-width background. The list fills the viewport behind it and receives the Scaffold's measured padding as scrollable `contentPadding`, not external viewport padding. Material measures the button, including large text, so end clearance requires no hardcoded overlay height, measurement state or custom layout. Existing availability, callbacks, navigation and suggestion state are unchanged.
- `WatchlistSurpriseScreenTest`: strengthens the scrolling contract to assert that the list viewport extends behind the action. It then scrolls to the final item index, verifies its merged card bounds end above the action, and clicks the action to assert `SuggestMovie`. The contract also runs with Spanish resources at 100% and 200% font scale. The initial overlap assertion failed before implementation; the large-font extension passed as regression coverage.
- `WatchlistMoviesScreenScreenshotTest`: adds 20-movie initial viewports with the real design-system navigation shell, in English and Spanish 200% text, both themes. Inspected the four new long-list captures and eight existing multi-entry captures: movies visibly extend behind/beside the button, and labels/navigation remain readable. Simplified duplicated content fixtures after detekt reported the capture method exceeded 60 lines; baseline verification confirmed the existing variants remained unchanged by that simplification.

Supporting files: PRD and the existing OpenSpec proposal/specification/design/tasks describe the floating overlay and scrollable end clearance. No resource, dependency, persistence, app-shell or ViewModel changes were required.

### Validation

Managed workflow `c65966ecd448f32d1419e69f3c634ba5` used the same external emulator-only launcher, JDK/Firebase/compiler flags and standalone `gradle_run.py` lifecycle documented above. Only the agent-started emulator was targeted; the physical phone was not used.

| Run | Question | Nested tasks/options | Result |
|---|---|---|---|
| 1 | Does the action float over the list while allowing the last movie to clear it? | `:feature:watchlist:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest#surpriseActionRemainsReachableAfterScrollingToTheLastMovie` | Behavioral red: expected viewport overlap was false; exit 1. |
| 2 | Same as run 1 | Same as run 1 | Green after overlay and end-padding implementation; exit 0. |
| 3 | Do the floating-action UI checks and screenshots pass? | `:feature:watchlist:impl:ktlintFormat :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*'` | Recording/formatting passed; 22 screen captures recorded. Inspected all 12 new or affected multi-entry captures; independent verification still required. |
| 4 | Same as run 3 | `:feature:watchlist:impl:connectedDebugAndroidTest :feature:watchlist:impl:testDebugUnitTest :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:verifyPaparazziDebug -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.watchlist.impl.presentation.WatchlistSurpriseScreenTest` | Exit 1: detekt reported `capture` length 66 versus maximum 60. Read its dedicated report and source before simplifying fixtures. This was not behavioral red. |
| 5 | Same as run 3 | `:feature:watchlist:impl:ktlintFormat` | Exit 0 after fixture simplification. |
| 6 | Same as run 3 | Same as run 4 | Yes for the selected scope: 10/10 surprise Compose tests, including normal/large Spanish scroll clearance; module unit tests, detekt, ktlint and independent module Paparazzi verification passed; exit 0. |

OpenSpec strict validation and `git diff --check` are final completion checks. The managed workflow removes only its own logs; the temporary launcher is removed and the agent-started emulator stopped afterward. Physical one-handed usability, manual sheet/rotation checks, full-app journeys, root screenshot verification and release aggregate/external gates remain pending for this follow-up. No fresh release-readiness claim is made.

## Accent color refinement — 2026-10-26

The user approved improving color contrast while keeping the current compact size, position, text-only appearance and soft shadow. `WatchlistMoviesScreen` now supplies `tertiary` / `onTertiary` through standard button colors. Santoro's primary pair is monochrome; its existing tertiary pair provides the blue accent, using dark blue/white in light mode and light blue/dark blue in dark mode. No new color literals, global theme edits, size, shape, elevation, placement, scroll, callback or state changes were made.

No test classes changed. The existing screen screenshot suite recorded 22 variants; all 12 affected multi-entry captures were inspected, including long lists, app navigation, both themes and Spanish 200% text. These intentional visual baselines are not behavioral red/green evidence. The PRD and existing OpenSpec artifacts record the approved styling.

Calculated foreground/background contrast from the existing theme values using WCAG relative luminance: 8.98:1 in light mode (`#FFFFFF` on `#114D74`) and 7.77:1 in dark mode (`#003351` on `#9ACCF9`). This evaluates the normal enabled text pair, not every interaction state or arbitrary poster color.

Question: "Do the accent-color screenshots and affected static checks pass?" Yes. Managed workflow `aa0238a4db9b3c4492e37dd03ed33178` used standalone `gradle_run.py` calls with the documented JDK 21, in-process compiler, Firebase-disabled and console suffixes:

| Run | Nested command before common suffix | Result |
|---|---|---|
| 1 | `./gradlew :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*'` | Exit 0; recorded intentional captures, then inspected all affected images. |
| 2 | `./gradlew :feature:watchlist:impl:verifyPaparazziDebug :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck` | Exit 0; independent module screenshot verification and static checks passed. |

OpenSpec strict validation and `git diff --check` are final completion checks. Workflow finish removes only wrapper-owned logs. No connected tests, full release aggregate, physical-device review or full-app journeys were rerun for this color-only refinement; previous scrolling test evidence remains historical. No commit, push or archive was requested or performed.

## Muted dark-theme accent — 2026-10-26

The user approved reducing the action's prominence only in dark mode. `WatchlistMoviesScreen` now uses the existing `tertiaryContainer` / `onTertiaryContainer` pair on dark surfaces and retains `tertiary` / `onTertiary` on light surfaces. Surface brightness is derived from the active Material scheme, not the device preference, so explicit light/dark previews and screenshots render consistently. There are no global theme, opacity, geometry, shadow, scroll, callback or state changes.

No test classes changed. All six affected dark screen captures were inspected, including long lists, navigation and Spanish 200% text. SHA-256 comparisons confirmed all 11 light screen captures are byte-identical to their pre-change versions. The existing muted dark pair (`#FFFFFF` on `#346891`) has calculated WCAG text contrast 5.94:1 in its normal enabled state. The PRD and active OpenSpec design/specification/tasks record the decision; prior brighter dark-accent evidence remains historical.

Question: "Do muted dark-theme screenshots and affected static checks pass?" Yes. Managed workflow `6431585cbbdfc1aae0db378f95af4cff` used standalone `gradle_run.py` calls with the documented JDK/compiler/Firebase/console suffixes:

| Run | Nested command before common suffix | Result |
|---|---|---|
| 1 | `./gradlew :feature:watchlist:impl:ktlintFormat :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*'` | Exit 1: formatter reported import ordering and overlong color expressions. Read the dedicated ktlint report and affected source before expanding the branches. Not behavioral red. |
| 2 | Same as run 1 | Exit 0; recorded 22 screen captures, inspected the six affected dark images and verified all light hashes remained unchanged. |
| 3 | `./gradlew :feature:watchlist:impl:verifyPaparazziDebug :feature:watchlist:impl:detekt :feature:watchlist:impl:ktlintCheck` | Exit 0; independent module screenshot verification and static checks passed. |

OpenSpec strict validation and `git diff --check` remain completion checks. Finish removes only wrapper-owned logs. This is a visual-only refinement, not a new behavioral TDD cycle or a fresh connected/release-readiness claim. Physical-device acceptance and the previously listed manual/external gates remain pending. No commit, push or archive was requested or performed.

## Publication preparation and journey correction — 2026-10-26

The user requested the implementation push. Pre-commit inspection found that `MainActivityNavigationJourneyTest` still assumed the original single-entry action, which is now intentionally hidden. The selected-detail/Back journey was updated to mark the existing watched fixture as also retained in Watchlist through the existing fake repository API, giving two valid candidates without changing shared fixtures or production behavior. The test reads the movie actually displayed in the sheet and asserts that its ID opens in detail, rather than assuming a random fixed choice. This reuses the already-agreed app journey boundary.

Question: "Does the suggestion detail-and-Back journey pass with the current action visibility?" Yes after fixture correction. Managed workflow `93ce5a06069e5ef34646a87a927536e7` used the temporary emulator-only launcher and documented JDK/compiler/Firebase flags.

| Run | Tasks/options | Outcome |
|---|---|---|
| 1 | `:app:connectedJourneyTestAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.santoro.MainActivityNavigationJourneyTest#givenWatchlistSuggestion_whenOpeningDetailAndReturning_thenPanelStaysClosed` | Device-offline installer failure, zero tests; infrastructure only. ADB subsequently showed the emulator ready. |
| 2 | Same as run 1 | Reproduced outdated fixture: no Surprise me node with one saved movie; exit 1. |
| 3 | `:app:ktlintFormat` | Exit 0. |
| 4 | Run 1 tasks plus `:app:detekt :app:ktlintCheck` | Compiler rejected an unnecessary `onAllNodes` import; removed it because the rule exposes the member directly. Not behavioral red. |
| 5 | Same as run 4 | Selected-detail/Back journey and app static checks passed; exit 0. |

Publication scope is the Watchlist implementation, its tests/baselines, relevant shared icon/string additions, PRD and existing OpenSpec change. `.DS_Store` files and `opencode.json` are unrelated and excluded. Earlier screenshot and focused feature-test evidence remains recorded above. The full repository release aggregate was not rerun for this push; no release-ready or archive claim is made. The emulator and temporary launcher are cleaned up after validation, and workflow finish removes only managed logs.
