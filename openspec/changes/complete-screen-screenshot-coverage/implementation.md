# Screen screenshot coverage implementation

This change adds regression coverage for the ten existing plain screen composables. It introduces no production classes, repository fakes, ViewModels, dependencies or runtime behavior changes. Existing behavioral Compose and journey tests remain responsible for callbacks and navigation. Future features and behavior fixes still follow the documented test-first workflow.

## Changed test classes

All nine new classes parameterize fixed screen states by light/dark theme, render inside a full-size Material Surface under SantoroTheme, and use Pixel 3 with system UI hidden. They set Locale.US and restore the previous locale. Fixed content and null remote-image paths avoid live requests; the login background is a bundled resource. Surface supplies the background and inherited content color provided by the real app shell.

| Added class | Purpose and snapshots |
|---|---|
| `LoginScreenScreenshotTest` | Protect the bundled background, authentication buttons, loading overlay and sign-in banner: idle/loading/error, six captures. |
| `SearchMoviesScreenScreenshotTest` | Protect the dashboard, search results, query/filter area, suggestions and loading/empty/error presentations, twelve captures. |
| `SeeAllMoviesScreenScreenshotTest` | Protect list title, grid, retry, empty state and saved-content warning: content/loading/empty/error/stale, ten captures. |
| `ProfileScreenScreenshotTest` | Protect guest linking prompt, signed-in identity and linking error banner, six captures. |
| `SettingsScreenScreenshotTest` | Protect guest versus account actions, appearance/language entries and fixed version/footer, four captures. |
| `MovieDetailScreenScreenshotTest` | Protect movie content, actions, facts, overview, loading and error, six captures. |
| `WatchedMoviesScreenScreenshotTest` | Protect grouped movie content, collection empty state, unmatched search, loading and error, ten captures. |
| `WatchlistMoviesScreenScreenshotTest` | Protect list header and movie item, collection empty state, unmatched search, loading and error, ten captures. |
| `PersonScreenStatesScreenshotTest` | Complete person profile loading/error/empty-credits and filmography loading/error/empty coverage. Filmography loading/error retains the loaded person name while credits load independently, twelve captures. |

Existing `PersonDetailScreenScreenshotTest` retains its twelve reviewed actor/director, missing-biography, credits-error, large-preview and complete-list captures. `PersonDetailScreenTest` receives one test-method name shortening so the existing crew-year/undated-credit case meets the Kotlin line-length rule; its assertions and behavior are unchanged.

## Supporting files

- `.github/workflows/ci.yml`: replace the person-only screenshot command with root `verifyPaparazziDebug`, including all seven feature implementation modules and the design system. Use `screenshot-test-reports` separately from `unit-test-reports` and include Paparazzi report/failure paths.
- Feature `src/test/snapshots/images`: add 76 reviewed screen captures. Login and settings gain their previously missing baseline directories. Together with the existing person captures, there are 88 screen captures.
- Eight existing component golden images are refreshed after reviewing every expected/actual delta: design-system Banner, BottomNavigationBar, NoResultsContent and QueryTextField; movie-detail GenreChip; watched MovieCard and WatchedStatsDashboard; watchlist WatchlistMovieItem. All eight last originated at commit `37dc373` with Paparazzi 1.3.5/compileSdk 35; the current pinned toolchain uses Paparazzi 2.0.0-alpha02/compileSdk 36. Text and geometry remain the same; rasterized text, icon/rounded edges and subtle fill differences were accepted. No tolerance was increased.
- `docs/guides/GUIDE-testing-and-tdd.md`: add screen/state inventory, Surface/locale determinism, explicit root verification and safely scoped recording instructions.
- Prior person OpenSpec task 2.3 and the F-29 PRD status: resolve the previously blocked GenreChip screenshot gate while retaining manual/external requirements.
- This OpenSpec change: proposal, design, requirement delta, tasks, implementation summary and validation evidence make scope and remaining limitations reviewable.

## Recording and review corrections

The first recording attached a trailing Gradle `--tests` option only to the final module task, unintentionally rewriting some existing components. The unchanged HeroMovieCard golden was restored exactly from HEAD. Only the eight explicitly reviewed legacy mismatches are retained. Subsequent recording scopes the filter to each requested task.

Initial visual inspection also caught black transparent backgrounds and incorrect inherited text colors when plain screens were rendered without the app shell. The fixture was corrected with themed Surface, captures regenerated, and reviewed again. These are harness corrections, not production defects or TDD red evidence.

## Validation and limits

Independent all-module screenshot verification passed all 112 cases. The full repository `test detekt ktlintCheck koverVerify assembleDebug assembleRelease` gate also passed. YAML/matrix checks, strict OpenSpec validation and diff whitespace checks passed. See [validation.md](validation.md) for every quoted verification question, exact managed Gradle command, bounded answer and workflow cleanup result. Snapshot recording alone does not prove verification. The suite covers initial Pixel 3 English viewports, not every scrolled section, dialog, locale or device. Existing component snapshots and behavioral tests provide complementary coverage. Visual review also records the existing white movie navigation icons against the light loading/error background as a UI follow-up; this regression-only change preserves that behavior. Initial loading captures show the deterministic first animation frame, not the full spinner animation. Hosted Ubuntu CI must still run externally; local macOS verification does not establish platform parity. No commit or push is part of this change.
