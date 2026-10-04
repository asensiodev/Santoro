# Person profile polish

Approved scope: specialty-first sections; eight horizontal movie previews per section; counts and See all for larger sections; complete lazy movie list with existing movie navigation, restored scroll and shared profile data. No new runtime dependencies or fake classes.

## TDD boundaries and observed results

The user approved repository HTTP, plain screen UI, app navigation and screenshot seams. Person.knownForDepartment was declared with a null default before the HTTP test so that the assertion compiled; this contract declaration is not behavioral implementation.

- HTTP red: real repository decoding returned null instead of Directing. The filtered run failed Android native initialization and is classified as setup failure. The established full module task produced the behavioral red.
- HTTP green: all 35 person JVM cases pass after decoding and trimming known_for_department.
- App journey red: See All is absent when a prolific person is opened from real app navigation.
- Preview red: the actual screen exposes no See All action for nine movies.
- Director ordering red: the actual screen placed Behind the camera below Acting, failing the position assertion.

## Test doubles

MockK remains the unit-test convention. Existing deterministic Hilt app journeys use FakePersonRepository; this change extends that fixture with extra movies and request tracking for the shared-data navigation contract. Plain UI tests use values and captured callbacks. HTTP integration uses MockWebServer with real Retrofit/Gson/mapping. No new fake class is necessary.

TMDB specialty and credits contract verified against the [official OpenAPI schema](https://developer.themoviedb.org/openapi/tmdb-api.json): details include known_for_department, and movie credits accept person identity and language with no page parameter.

## Changed production classes and functions

- Person: optional knownForDepartment with a null default preserves existing callers and supports profession ordering.
- PersonApiModel and PersonApiMapper: decode known_for_department and trim blanks at the HTTP boundary.
- PersonFilmographyRoute and FilmographySection: serializable person identity and selected acting/crew section for the complete list.
- PersonDetailUiState: derives section order, selected movies and complete-list loading state from existing immutable data; no additional request owner.
- PersonDetailScreen / PersonDetailRoute: specialty-first horizontal poster previews, eight-movie cap, count and See all callback. FilmographyMovie is shared with the complete list and has a light/dark preview.
- PersonFilmographyScreen: plain state-driven complete list, count, poster/title/year/roles, movie callback and Back; loading, empty and retryable error branches; light/dark preview.
- PersonDetailNavigation: registers the complete-list route and obtains the originating profile entry's Hilt ViewModel; uses RESUMED navigation guards and initializes only through existing guarded MVI intents.
- MainActivity: registers both exploration destinations in the main host and connects guarded Back/movie callbacks.

## Changed test classes

- PersonRepositoryHttpTest: verifies specialty survives actual HTTP decoding and normalization.
- PersonDetailScreenTest: director-first ordering; nine-movie See all callback and eight-card limit; no See all for eight; complete-list movie identity, loading, empty/Back and error/retry. Existing biography, locale and profile-state tests remain.
- MainActivityPersonExplorationJourneyTest: complete list → movie → Back → profile restores full-list vertical and preview horizontal scroll while making only one profile/credits request.
- FakePersonRepository: extends the existing journey fixture with optional extra movies and resettable credit-request tracking; no new fake class.
- PersonDetailScreenScreenshotTest: retains existing scenarios and adds director-first, large preview and complete-list light/dark cases.

## Supporting files

English/Spanish plural resources provide localized movie counts and reuse the existing See All label. OpenSpec proposal/design/spec/tasks describe the approved scope and testing seams. The testing guide records when to use MockK, the existing journey fake and MockWebServer. PRD F-29 and this report track implementation and remaining validation.

## Behavior verification

The specialty HTTP test passed after mapping, and the director order, nine-movie preview and complete-list app journey all passed after implementation. A combined instrumentation invocation accidentally applied both modules' class filters to each module; each real test passed but Android reported an additional missing-class initialization error. This is an invocation failure, and unfiltered module runs are required for clean final evidence.

Four complete-list state/callback tests were added as regression coverage over the existing loading/error/empty/callback patterns; they are not claimed as separate observed red/green cycles. Screenshot baselines are intentionally updated for the new layout and verified independently after visual inspection.

Clean unfiltered connected verification passes all 21 person Compose cases and all 16 app journeys with zero failures, errors or skips. The full-list journey proves both scroll restoration and a single profile/credits request. FilmographyMovie retains its existing default layout and now accepts a caller modifier at its root for its new shared component boundary.

## Visual inspection

The first intended director, large-preview and complete-list captures were inspected. The large preview exposed uneven card bottoms caused by different title line counts. The preview now reserves two title lines, one year line and two role lines (blank for missing values) so neighboring cards align. This was a visual inspection finding, not an automated behavioral red claim. Re-recording, a second inspection and independent verification follow this change.

The aligned layout passes a fresh unfiltered run of all 21 person Compose tests and 16 app journeys. All 12 current PNGs were inspected individually in both themes: content, missing biography, credits error, director-first, large preview and complete filmography. Card bottoms now align, text is legible, the count and See All action are visible for nine movies, and the complete list remains readable. Network photos, TalkBack, real process death and hosted CI remain manual/external gates.

Independent Paparazzi verification and the complete person JVM suite passed all 12 and 41 cases respectively. Static analysis identified only preview numeric fixture IDs and an oversized screenshot capture helper; preview IDs now use the existing simple sample convention, and the identical filmography fixture is extracted within the screenshot test class. No runtime behavior or screenshot values changed in that cleanup.

## Final validation

All current affected checks pass: 41 person JVM cases (including 12 screenshot cases), independent verification of all 12 snapshots, 21 person Compose cases and all 16 app journeys on Pixel_9a API 37. Detekt and ktlint checks pass for the changed modules. The repository aggregate gate `test detekt ktlintCheck koverVerify assembleDebug assembleRelease --continue` passes. See [polish-validation.md](polish-validation.md) for every wrapper question, exact command and bounded result, including setup/invocation failures and actual red/green assertions.

The current OpenSpec change has 13 of 17 tasks complete. Tasks 2.3, 3.3, 4.2 and 4.4 stay open: the pre-existing GenreChip movie screenshot mismatch is unresolved, actual process-death and manual live-TMDB/accessibility checks remain, and archive requires human review. No unrelated movie snapshot baseline, account workflow, global skill installation or user changes were modified by this polish. Hosted CI was not run.

## Screenshot gate follow-up (2026-10-04)

The subsequent [complete-screen-screenshot-coverage change](../complete-screen-screenshot-coverage/implementation.md) diagnosed and reviewed the old renderer baselines, including GenreChip, and independently verified all 112 active screenshots across eight modules. Task 2.3 is now complete. The earlier mismatch evidence above remains historical; process-death, manual/live-data/accessibility and hosted CI requirements remain open.
