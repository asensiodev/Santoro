# F-29 Implementation Evidence

Date: 2026-10-04. Change: `explore-people-and-filmography`. Implementation and aggregate gates are complete; the existing movie screenshot mismatch, manual checks, archive and release readiness remain pending.

## Production changes

| File / class or function | Change and reason |
|---|---|
| `core/domain/model/Person.kt` — `Person` | Pure person identity, biography, portrait path and optional personal facts; keeps DTOs out of the domain. |
| `core/domain/model/PersonFilmography.kt` — `PersonFilmography`, `PersonMovieCredit` | Separate acting/crew movie credits with movie identity, release date and distinct roles; supports multi-role people and movie navigation. |
| `core/domain/repository/PersonRepository.kt` — `PersonRepository` | Two suspending read contracts for details and movie credits, owned and cancelled by their caller. |
| `feature/person-detail/api/.../PersonDetailRoute.kt` — `PersonDetailRoute` | Serializable person ID route; each destination uses its own profile identity. |
| `feature/person-detail/impl/.../data/PersonApiModels.kt` — `PersonApiModel`, `PersonCreditsApiModel`, `PersonCreditApiModel` | TMDB response DTOs with nullable optional fields and explicit wire names; remain at the data boundary. |
| `feature/person-detail/impl/.../data/PersonApiService.kt` — `PersonApiService` | Read-only person and movie-credits endpoints using the shared Retrofit client and language interceptor. |
| `feature/person-detail/impl/.../data/PersonApiMapper.kt` | Normalizes absent/invalid optional dates, rejects invalid profile identity, groups distinct roles per movie per section, and orders dates descending with undated movies last and ID tie-breakers. |
| `feature/person-detail/impl/.../data/DefaultPersonRepository.kt` — `DefaultPersonRepository` | Maps HTTP reads on injected IO dispatcher, returns ordinary failures and preserves cancellation; rejects a response for the wrong person. |
| `feature/person-detail/impl/.../di/PersonDetailModule.kt` — `PersonDetailModule` | Hilt composition of repository, shared Retrofit, and dispatcher provider. |
| `feature/person-detail/impl/.../presentation/PersonDetailUiState.kt` — `PersonDetailUiState`, `PersonLoadState`, `PersonDetailIntent` | Immutable profile/filmography state with independent loading/error outcomes and explicit initialization/retry intents. |
| `feature/person-detail/impl/.../presentation/PersonDetailViewModel.kt` — `PersonDetailViewModel` | Loads details before credits, keeps biography during credits failure, prevents duplicate initialization/retry jobs, and guards against obsolete results after cancellation. |
| `feature/person-detail/impl/.../presentation/PersonDetailScreen.kt` | Lifecycle-aware route wiring plus previewable screen; portrait/facts, saveable expandable biography, lazy filmography, localized empty/errors, theme-aware image placeholders, and accessible movie clicks. |
| `feature/person-detail/impl/.../presentation/navigation/PersonDetailNavigation.kt` | Registers the shared profile destination and guards person navigation by RESUMED lifecycle to prevent repeated taps. |
| `feature/movie-detail/impl/.../presentation/model/CrewMemberUi.kt` — `CrewMemberUi` | Retains domain person ID so directors and other displayed crew open their own profile. |
| `feature/movie-detail/impl/.../presentation/mapper/MovieUiMapper.kt` | Preserves crew IDs and deduplicates by identity/role instead of name/role; distinct people with the same name remain navigable. |
| `feature/movie-detail/impl/.../presentation/MovieDetailScreen.kt` | Threads person callbacks through credits, adds clickable cast/crew with 48dp minimum targets, supplies cast content types and follows ViewModel-keyed initialization. Existing saveable movie scroll state is retained. |
| `feature/movie-detail/impl/.../presentation/navigation/MovieDetailNavigation.kt` | Connects the movie screen's person clicks to the app-owned navigation callback. |
| `app/.../MainActivity.kt` — `SantoroApp` | Wires movie → person → movie in the main host, preserves the back stack and guards Back/movie transitions while the source is RESUMED. |

## Test changes

Additional regression coverage and the future TDD workflow are tracked in [testing.md](testing.md). Original validation below is historical; current test counts and follow-up results belong to that report.

| Class | What it verifies or supports |
|---|---|
| `PersonApiMapperTest` | Missing/full profile data, invalid movie rows/dates, distinct roles, stable ordering, acting/crew overlap and empty credits. |
| `DefaultPersonRepositoryTest` | Successful reads, network failures, invalid/mismatched profiles and cancellation at both endpoints. |
| `PersonApiServiceTest` | Actual Retrofit paths and a single Spanish language parameter on both requests using the shared language interceptor and local HTTP server. |
| `PersonDetailViewModelTest` | Success/empty data, initialization once, independent profile/credits recovery, duplicate retries and ViewModel/cancellation ownership. |
| `PersonDetailScreenScreenshotTest` | Six deterministic light/dark captures for content, missing biography and credits error; uses absent image paths rather than remote images. |
| `PersonDetailScreenTest` | Accessible movie click delivers ID, long biography expands/collapses, and credits retry retains biography. |
| `MovieUiMapperTest` | New regression for same-name crew identities, distinct roles and duplicate credit removal. |
| `MainActivityPersonExplorationJourneyTest` | Actor/director navigation, movie round trip and scroll restoration, repeated actor/movie taps and activity recreation. |
| `FakePersonRepository` | Deterministic person/filmography reads and recorded identities for app journeys; no TMDB access. |
| `AppJourneyTestData` | Adds stable cast/crew fixtures to existing journey movies so the exploration paths are exercised. |
| `BaseAppJourneyTest` | Injects and resets the new fake between journeys to keep tests isolated. |
| `AppJourneyTestModule` | Replaces production person Hilt binding with the fake for instrumentation. |

## Supporting files

- New person api/impl build files, `settings.gradle.kts` and app dependencies register the feature. Root Kover dependencies include both new modules. No new runtime library is introduced.
- English and Spanish string resources provide profile labels, biography controls, missing data messages and retryable errors.
- `app/proguard-rules.pro` preserves the new Gson DTO classes and fields during release optimization, consistent with existing API-model rules. Every DTO field declares its wire name.
- Six Paparazzi baseline PNGs establish the new visual contracts.
- OpenSpec tasks and PRD track delivery and outstanding verification. Existing FIP-023 and external release gates remain open as recorded; release preparation is now tracked in the [unified release guide](../../../docs/guides/GUIDE-release-preparation.md).

## Validation

All Gradle invocations use the managed `gradle-run.py` wrapper and workflow `e8ebcea8d39130fdb233e89622b19258`. The build uses the already-installed Azul JDK 21 at `/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home` via `-Dorg.gradle.java.home`; `-Pkotlin.compiler.execution.strategy=in-process` avoids an incompatible existing Kotlin daemon.

- Initial sandbox cache access and Java 25/17 configuration attempts did not compile project sources. JDK 21 resolved the environment issue without installing software or changing project toolchain settings.
- Targeted compilation of person-detail, movie-detail and app: passed.
- Targeted unit checks: person-detail 23/23, movie-detail 109/109, domain 5/5, network 46/46, architecture 7/7; no failures/errors/skips in their test-result XML.
- Android screen/journey source compilation: passed.
- Six final Paparazzi captures: rendered and visually inspected in light/dark themes after correcting placeholder contrast. Independent person verification passed all six captures.
- Pixel_9a API 37 initial instrumentation: person screen 3/3, movie detail 1/1, new app exploration journeys 5/5; no failures/errors/skips. Final person repeat passed 3/3. Full app journey regression passed 15/15: authentication 3, deep links 3, existing navigation 4, person exploration 5.
- Existing movie `GenreChipScreenshotTest` has a 2.386627% raster difference. Its component, screenshot test, theme and baseline have no source changes. The delta shows text rasterization differences with matching layout/border. Isolated verification reproduced the same difference without recording in the same invocation. The cause is not established; this is an unresolved existing-component snapshot mismatch, not proof of a prior failure. The existing baseline is preserved.
- Aggregate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease --continue`: passed (run0017, exit 0). `test` does not replace explicit Paparazzi verification; the separate existing GenreChip mismatch remains open. Final `:feature:person-detail:impl:testDebugUnitTest :feature:person-detail:impl:ktlintCheck :app:assembleRelease` passed after DTO annotations and R8 keep rules (run0018, exit 0). Manual checks remain pending.

Exact verification questions, bounded answers and nested Gradle commands are recorded in [gradle-evidence.md](gradle-evidence.md). The managed wrapper workflow was finished successfully and removed only its own temporary logs. OpenSpec strict validation and `git diff --check` pass.

## Remaining checks and limitations

- Fresh process restoration and a live TMDB profile with real photos/biographies require device/manual evidence beyond activity recreation and deterministic fixtures.
- No person disk cache is added; first offline visits show retryable errors. Missing biographies in the requested language show a localized empty message without another language request.
- No Room migration, account/Firebase mutation, or list modification occurs merely by exploring a person.
- Archive requires completed validation and human review per the OpenSpec workflow in `docs/README.md`. No commit or push had been performed at this validation stage.

## Profile polish follow-up

See [polish.md](polish.md) for the approved specialty-first preview/full-list behavior, every changed production/test class, test-double decisions and observed TDD evidence. Current polish validation supersedes earlier layout results; historical validation remains evidence for the earlier implementation.

## Screenshot gate follow-up (2026-10-04)

The subsequent [complete-screen-screenshot-coverage change](../complete-screen-screenshot-coverage/implementation.md) diagnosed and reviewed the old renderer baselines, including GenreChip, and independently verified all 112 active screenshots across eight modules. Task 2.3 is now complete. The earlier mismatch evidence above remains historical; process-death, manual/live-data/accessibility and hosted CI requirements remain open.

## Cast portrait placeholder follow-up (2026-10-07)

The user approved showing the existing person profile icon for loading, missing and failed cast photos, with tests of that visible behavior. The agreed boundary is the state-driven movie-detail content, with controlled image responses rather than a ViewModel or repository fixture.

- `MovieDetailScreen.kt`: `CastMemberItem` draws the theme-aware, 48dp profile icon behind its circular 64dp photo. The existing `AsyncImage` covers it on success; removing the app-logo error painter keeps the profile icon visible on failure or absent data. This preserves the lightweight image component used by the lazy cast row.
- `CastPortraitTest.kt`: four visual behavior tests run in both themes. A real Coil loader with a controlled interceptor holds requests, returns failures, or supplies a solid-color image. Movie artwork completes separately, and portrait completion is observed before assertions. Tests restore the previous singleton loader and shut down their own loader. They compare the avatar interior exactly with an independently rendered profile icon; two outer pixels are excluded because the circular antialiasing blends with different parent backgrounds. The successful-load test checks photo pixels where the icon's head and body would appear. No production test tags, new dependencies, repository fakes or screenshot baseline updates are needed.

All Gradle commands used `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow a46e94dd2137435e5a8bfbbf12383857 --scope targeted --question <question> -- ./gradlew <tasks>`, with JDK 21 selected through `-Dorg.gradle.java.home`, `-Pkotlin.compiler.execution.strategy=in-process` and `-PenableFirebase=false`.

Focused command: `:feature:movie-detail:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.moviedetail.impl.presentation.CastPortraitTest`.

| Runs | Verification question | Bounded answer |
|---|---|---|
| 0001–0002 | Does the cast portrait show the profile icon while its image request is still loading? | Initial behavioral red: 6,799 pixels differed with the existing blank avatar. After adding the icon, 72 outer-edge pixels differed because of parent-background blending; the test was narrowed to the avatar interior. |
| 0003 | Does the loading avatar match the profile icon inside its circular image bounds? | Green: the loading assertion passed. |
| 0004 | Does a failed cast photo retain the profile icon rather than the app logo? | Behavioral red: 2,561 interior pixels differed while the app-logo error painter remained. |
| 0005 | Do loading and failed cast photos display the same profile icon? | Green: both passed after removing the app-logo error painter. |
| 0006 | Do cast portraits show the profile icon for loading, missing and failed photos, and replace it on success, in both themes? | Green: 8/8 passed. Missing-photo, success and additional-theme cases are regression coverage. |
| 0007 | Do movie-detail unit tests, existing screenshot baselines, formatting and static analysis pass after the portrait change? | Unit/screenshot/static checks passed; new Android test formatting failed. This is a formatting failure, not behavioral red. |
| 0008 | Is the new cast portrait test formatted according to repository rules? | `:feature:movie-detail:impl:ktlintAndroidTestSourceSetFormat` passed. |
| 0009 | Do all movie-detail instrumented tests, unit tests, screenshot baselines, ktlint and detekt pass with the final portrait fixture? | Green: 9 instrumented tests and 61 JVM tests, including screenshot verification, passed with no failures, errors or skips; ktlint and detekt passed. |

Run 0007 used `:feature:movie-detail:impl:testDebugUnitTest :feature:movie-detail:impl:verifyPaparazziDebug :feature:movie-detail:impl:ktlintCheck :feature:movie-detail:impl:detekt --continue`. Run 0009 added unfiltered `:feature:movie-detail:impl:connectedDebugAndroidTest` to those tasks after isolating movie-artwork completion from portrait completion. Device: Pixel_9a API 37. The existing CI instrumented job already runs this module and will discover the new tests.

This follow-up validates the affected module, not a fresh repository-wide release gate or live TMDB downloads. The managed workflow was finished and removed only wrapper-owned temporary logs. No commit or push was performed for this follow-up.

## CI portrait fixture correction (2026-10-07)

[CI run 37660736127](https://github.com/asensiodev/Santoro/actions/runs/37660736127) passed static analysis, unit/coverage, screenshots and the Debug build, but all eight portrait cases failed their pixel comparison on API 35. The reference avatar was placed at the bottom of an edge-to-edge test window, where three-button navigation overlays a scrim. Compose captures a window crop, so the system overlay changed reference pixels while the scrolled cast avatar remained unobstructed.

The local API 37 emulator originally used gesture navigation. Switching only its navigation overlay to three-button mode reproduced all eight failures on the unchanged test: 17,140 differing pixels per case, consistent with the CI failures of 17,178 light and 17,167 dark pixels. Adding `WindowInsets.safeDrawing` padding to the test fixture's root `Surface` made all eight cases pass with three-button navigation still enabled. This changes only `CastPortraitTest`; production rendering and strict interior-pixel assertions are preserved. The reference now occupies unobstructed app content rather than navigation-bar pixels.

The correction uses managed Gradle workflow `ece473c9576a601769b8241a1484efff`, the same JDK 21/options documented above, and the existing focused instrumented command. Navigation mode is restored after the comparison. Hosted API 35 verification is required before treating the CI failure as resolved.

| Verification question | Bounded answer |
|---|---|
| Do all eight CastPortraitTest cases pass on the unchanged implementation with three-button navigation? | Red: 8 failures, 17,140 differing pixels per case. |
| Do all eight CastPortraitTest cases pass with three-button navigation after the test fixture respects safe drawing insets? | Green: 8/8 passed. |
| Do all movie-detail instrumented tests and module ktlintCheck/detekt pass after the safe drawing test fixture fix with three-button navigation? | Green: unfiltered `:feature:movie-detail:impl:connectedDebugAndroidTest` passed 9/9; `:feature:movie-detail:impl:ktlintCheck :feature:movie-detail:impl:detekt` passed. |
| Do all eight CastPortraitTest cases still pass after the fixture fix with the original gesture navigation restored? | Green: 8/8 passed; original `navigation_mode=2` confirmed. |

The managed workflow was finished successfully, removing only wrapper-owned logs. These runs used API 37 because no local API 35 image is installed; hosted CI remains the check for that platform.

### Rendering-stable portrait assertions

[Run 37663288307](https://github.com/asensiodev/Santoro/actions/runs/37663288307) confirmed the safe-area correction reduced the API 35 mismatch to 370 light and 297 dark pixels, but exact equality still failed in all eight cases. The other four CI jobs passed. Local SDK metadata gives Pixel 6 the same 420 dpi as the local Pixel 9a; additional 480 and 450 dpi runs passed the original exact comparison, so density did not reproduce the remaining hosted difference.

`CastPortraitTest` now checks exact colors in every uniform 3×3 reference region within the avatar, including the solid profile glyph and surrounding background. It requires solid profile pixels to exist. This excludes antialiased vector boundaries rather than permitting a percentage of incorrect pixels or a color tolerance; a blank avatar or wrong icon still differs in the checked glyph regions. Photo replacement remains separately checked with a controlled opaque image. Production code is unchanged. Rendering at icon edges remains the working explanation until hosted API 35 verification confirms the revised visual contract.

Managed workflow `553928d63af15e41b55b348aabbc52e4` used the same wrapper, JDK 21/options and focused instrumented command recorded above:

| Verification question | Bounded answer |
|---|---|
| Do the eight exact-pixel CastPortraitTest cases reproduce the remaining CI mismatch at emulator density 480? | 8/8 passed; mismatch not reproduced. |
| Do the eight exact-pixel CastPortraitTest cases reproduce the remaining CI mismatch at fractional-layout emulator density 450? | 8/8 passed; mismatch not reproduced. |
| Do all eight CastPortraitTest cases pass at density 450 when comparing exact solid glyph and background pixels? | Green: 8/8 passed. |
| Do all movie-detail instrumented tests and module ktlintCheck/detekt pass with the exact solid-pixel portrait assertions at density 450? | Green: unfiltered `:feature:movie-detail:impl:connectedDebugAndroidTest` passed 9/9; module `ktlintCheck` and `detekt` passed. |
| Do all eight CastPortraitTest cases pass with exact solid-pixel assertions after restoring the original density 420 and gesture navigation? | Green: 8/8 passed. |

Every report had zero failures, errors or skipped cases. The emulator's original physical 420 dpi with no override and gesture navigation mode 2 were restored. The managed workflow finished successfully and removed only wrapper-owned logs.

Hosted confirmation: [CI run 37665400562](https://github.com/asensiodev/Santoro/actions/runs/37665400562) passed all five jobs for corrective commit `fb24337`: API 35 instrumented boundary/journey tests, static analysis, screenshot verification, unit tests/coverage and Debug assembly. The hosted portrait assertions passed with exact solid-region colors, confirming that the remaining exact-raster failure was confined to the excluded vector-edge regions. The CI portrait failure is resolved; the separate manual/live-TMDB and process-death checks above remain historical outstanding gates.

## Minified Release startup regression (2026-10-07)

The maintainer reported an immediate crash opening Play Internal version `1.1.0 (43)` on a physical Pixel 9a. The signed Release APK reproduced the same startup symptom repeatedly on the API 37 Pixel 9a emulator. Android Navigation failed while creating the initial graph because it could not find `com.asensiodev.feature.persondetail.api.navigation.FilmographySection`. R8 had renamed that enum to `d4.b`, while the serialized route retained its original class name. Debug-derived journey tests and successful Release compilation did not exercise this runtime failure.

The existing public app-navigation boundary now has an executable minified-Release startup regression in `tools/check-release-startup.py`. It requires a non-debuggable app, cold-launches the installed Release activity, rejects fresh package-specific crash-buffer entries, and checks process survival and a resumed activity. It preserves app data and does not print device logs. Checking only the PID would miss crashes while Android retains the process to show its crash dialog.

Command: `python3 tools/check-release-startup.py --adb /Users/angelasensio/Library/Android/sdk/platform-tools/adb --serial emulator-5554`.

- Red on the original signed APK: exit 1, `FAIL: The Release app reported a startup crash.`
- Fix: `app/proguard-rules.pro` preserves only the filmography navigation enum and its members, avoiding a new Android dependency in the pure JVM API module.
- Green after managed `:app:assembleRelease` and installing the corrected APK over the original: exit 0, `PASS: The Release app stayed alive and reached a resumed activity.` The mapping preserves the enum's fully qualified name.

The testing and release preparation guides now require this check before distributing a candidate. The maintainer authorized replacement version `1.1.1 (44)`. Its version bump and fix must be committed and pushed before generating the replacement AAB. Full candidate checks, bundle verification, physical-device confirmation and Play upgrade testing remain pending at this point.
