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
- OpenSpec tasks and PRD track delivery and outstanding verification. Existing FIP-023/FIP-024 state is retained.

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
