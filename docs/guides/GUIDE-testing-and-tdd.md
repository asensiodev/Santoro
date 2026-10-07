# Testing and Test-First Development

## Required workflow

Apply this workflow to new features and behavior fixes. Expand existing coverage through regression tests; report that work accurately rather than claiming retrospective TDD.

1. Read the active OpenSpec change or FIP and identify the user-visible behavior. List the public boundaries under test and obtain agreement before adding tests. Reuse an agreement already recorded in the current task; ask only when the boundary or observable contract changes.
2. Select one behavior and write one focused test using independent expected outcomes from the specification or a known example.
3. Run that test before changing production code. Record the command and the expected behavioral assertion failure. A compiler, fixture, environment, or dependency failure does not establish red.
4. Implement only the behavior needed to pass the test. Run the same focused test and record green. Repeat one behavior at a time, rather than writing every test and then every implementation.
5. Review simplification after behavior is green and retain regression coverage through any refactor. Run affected checks, then the repository gates when making a release-ready claim.

If an existing behavior passes its new test immediately, classify it as regression coverage. Do not temporarily damage production code or manufacture a failing assertion to claim TDD. For a reproduced defect, keep the failing regression test and fix the defect through the same boundary.

## Test selection

| Observable contract | Smallest sufficient test |
|---|---|
| Screen text, loading/content/empty/error states, conditional controls, callbacks | Plain state-driven Compose test with controlled state and callbacks |
| ViewModel intents, durable state, retries, request ownership and cancellation | Unit test through `process(intent)` and exposed state, with controlled external-boundary responses |
| HTTP decoding, request paths/language, response failures | Repository integration test with the real Retrofit/Gson/mapper stack and a local HTTP server |
| Movie/person destination identity, Back, restored scroll, lifecycle or DI | App journey/instrumentation test |
| Layout, image composition, typography, light/dark contrast | Deterministic Paparazzi screenshot and human baseline inspection |

Compose screenshots complement behavioral assertions; they do not prove clicks, retries or navigation. Previews are development aids, not executed tests.

## Determinism and meaningful assertions

- Follow the existing JUnit, Kluent, Turbine, MockK, Compose, Paparazzi and journey conventions. Reuse installed tools and test fixtures.
- Assert visible semantics and callback outcomes before adding test tags. Test tags are appropriate for nodes without stable accessible text.
- Render plain screen composables for UI state contracts. Use ViewModels, navigation and DI only when their integration is the behavior under test.
- Control locale, time, coroutine scheduling and HTTP/image responses. Restore changed global state in teardown. Use Compose synchronization instead of sleeps.
- Keep success, error recovery, empty/missing information and relevant cancellation/concurrency cases. Protect a credible user outcome rather than aiming for a test per function or a large test count.
- Prefer the project's MockK convention for isolated unit-test boundary responses. Reuse existing fake repositories for full app journeys that need deterministic data through Hilt; a fake is not mandatory for every test. Use MockWebServer with the real networking stack for HTTP integration.
- Use fake responses at external/system boundaries. Assert output through the tested interface; collaborator call counts are justified only when duplicate requests or request authority are themselves the contract.
- Record new screenshot baselines separately from verification, inspect them, then verify independently. Preserve unrelated baselines when investigating a mismatch.
- Keep test and documentation language English; exercise existing Spanish resources as localized product behavior.

## Screen screenshot coverage

Run `./gradlew verifyPaparazziDebug` from the repository root to verify every Paparazzi module. CI runs this as a dedicated screenshot job, with separate failure artifacts from unit tests. `test` alone does not enable golden comparison.

Screen baselines use Pixel 3, English locale and both themes. Render each plain screen inside `SantoroTheme` and a full-size Material `Surface` so the background and inherited content colors match the app shell. Use fixed content, version values and null remote image paths. Restore locale after each test. Keep loading captures at the deterministic initial animation frame.

| Screen | Captured states in each theme |
|---|---|
| Login | Idle, loading, sign-in error |
| Search | Dashboard, results, loading, empty results, error, suggestions |
| See all movies | Content, loading, empty, error, stale saved content |
| Movie detail | Content, loading, error |
| Watchlist | Content, loading, empty collection, no search matches, error |
| Watched movies | Content, loading, empty collection, no search matches, error |
| Profile | Guest, signed-in account, link error |
| Settings | Guest and account actions |
| Person detail | Content, missing biography, credits error, director, large credits preview, profile loading/error, empty credits |
| Person filmography | Full credits, loading, error, empty credits |

The screen suite has 88 captures: 76 added in the screen-coverage change and 12 existing person captures. Component snapshots remain separate. This inventory covers initial screen viewports; it does not claim every dialog, scrolled region, device size or locale. Use Compose/journey tests for interactions and add focused visual variants when a feature changes those regions.

To record intentional baselines, target one module and test class at a time, for example `./gradlew :feature:login:impl:recordPaparazziDebug --tests '*LoginScreenScreenshotTest*'`. Gradle task options attach to the preceding task: one trailing `--tests` filter does not filter every earlier task in a multi-task command. Review every changed image, then run independent root verification. Never commit unrelated golden changes, widen pixel tolerances to hide drift, or use recording as verification.

## OpenSpec and completion evidence

Before distributing a release candidate, install the signed, minified Release APK on a test emulator and run `python3 tools/check-release-startup.py --serial <device-serial>` from the repository root. Pass `--adb <path>` if ADB is not on the command path. This regression check requires a non-debuggable app, performs a cold launch, rejects fresh package-specific crashes, and verifies process survival and a resumed activity. It preserves app data. A passing Debug or journey-test build does not validate Release navigation after R8 shrinking; this startup check complements the Play upgrade and fresh-install checks.

For each new change, record the agreed boundaries in `design.md` and use vertical test-first tasks. Keep red and green commands/results in the change evidence. A checked task means both its behavior and its validation passed. Document fixture/compiler/environment failures separately from behavioral red.

For regression-only work, record the previously unprotected scenario, the test added and the passing result. Report every modified production/test class and supporting document. Keep manual, screenshot, process-restoration and external gates visibly pending until verified; a normal JVM `test` task does not substitute for explicit Paparazzi or instrumented execution.

Run Gradle through the managed `gradle-run` wrapper. Run affected unit/UI/journey checks first. The repository aggregate gate is `test detekt ktlintCheck koverVerify assembleDebug assembleRelease`; additionally invoke the applicable Paparazzi and connected tests explicitly. Inspect their reports and preserve failed gate evidence.
