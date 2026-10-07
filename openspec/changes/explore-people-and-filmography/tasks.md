## 1. Person data and feature boundary

- [x] 1.1 Add person-detail api/impl modules and app wiring using existing conventions; verify Gradle configuration and module compilation.
- [x] 1.2 Add pure person/credit models, repository contract, TMDB details/credits service, DTO mapping, and Hilt bindings; verify mapping tests for null fields, person/movie identity, and language requests.
- [x] 1.3 Implement per-section movie grouping and release-date ordering; verify multiple characters/jobs, actor-and-crew overlap, tie-breakers, empty lists, and undated movies with focused tests.

## 2. Profile state and presentation

- [x] 2.1 Implement MVI profile loading, separately retryable credits, and cancellation-safe request ownership; verify success, details failure, credits failure, retry, cancellation, and duplicate initialization with ViewModel tests.
- [x] 2.2 Build portrait/header/facts, expandable biography, and filmography lazy rows with existing tokens and English/Spanish resources; verify missing fields, localized labels, stable keys, semantics, and previews.
- [x] 2.3 Add light/dark Paparazzi coverage for content, missing biography, and credits error; verify snapshots and existing movie-detail screenshots remain valid.

## 3. Connected movie and person navigation

- [x] 3.1 Preserve crew person ID through UI mapping and make cast/crew entries clickable; verify mapper tests and actor/director accessible click actions.
- [x] 3.2 Register person detail in the main host and connect filmography to existing movie detail with RESUMED guards; verify movie A → person P → movie B → Back → Back, scroll restoration, and repeated-tap behavior in journey/UI tests.
- [ ] 3.3 Verify rotation and process recreation preserve route identity and recover profile content; record device/emulator evidence without treating cancelled loads as errors.

## 4. Validation and pilot completion

- [x] 4.1 Run affected domain/person-detail/movie-detail/app checks first, then test detekt ktlintCheck koverVerify assembleDebug assembleRelease; record exact commands/results and keep failed or unavailable gates open.
- [ ] 4.2 Record manual actor/director exploration, Spanish missing biography, unavailable portrait, empty credits, offline details, credits retry, light/dark, accessibility, and rapid-tap checks; document any accepted limitations.
- [x] 4.3 Reconcile F-29 status and report changed production/test classes with validation evidence; keep FIP-023 and the external release gates tracked in the [unified release guide](../../../docs/guides/GUIDE-release-preparation.md) unchanged.
- [ ] 4.4 After human review and completed validation, sync/archive this pilot and record its planning/resume usefulness in the change completion evidence; verify the resulting main spec and archive with OpenSpec validation.

## 5. Approved profile polish (test first)

- [x] 5.1 Verify the person specialty crosses the HTTP boundary with an observed failing test, then implement mapping.
- [x] 5.2 Verify specialty-first ordering and bounded poster previews with Compose tests, then implement count and See all callbacks.
- [x] 5.3 Verify full filmography navigation, movie identity, Back restoration and reuse of loaded credits with an app journey, then implement the shared-data destination.
- [x] 5.4 Record and inspect intended light/dark snapshots, verify independently, run affected and aggregate checks, and report every changed class and remaining gates.

## 6. Approved chronological year headings (test first)

- [x] 6.1 Observe a failing plain-screen assertion for one accessible heading per year, then implement grouped full-list rendering without repeated card years.
- [x] 6.2 Verify singleton crew years and undated entries, keeping callbacks and profile-preview years intact.
- [x] 6.3 Inspect updated complete-list light/dark snapshots, verify independently, and run affected behavior and static checks; record evidence and remaining gates.

## 7. Cast scroll restoration regression

- [x] 7.1 Reproduce the confirmed cast → actor → Back journey with a horizontally scrolled cast and a delayed detail response, then preserve the cast scroll through Loading; record observed red/green evidence in design.md.
- [x] 7.2 Run all person exploration journeys and affected movie-detail unit, screenshot, and app/movie-detail static checks.
