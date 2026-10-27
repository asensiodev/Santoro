## 0. Agreement

- [x] 0.1 Confirm the test boundaries listed in design.md with the user before adding tests; record the confirmation in design.md.

## 1. Director data (test first)

- [ ] 1.1 Observe a failing migration test for Room 5 → 6 with an existing v5 watched row, then add the nullable `directors` column, entity/mapper changes and the domain `directors` value; verify unresolved vs resolved-empty round trips.
- [ ] 1.2 Observe a failing detail-mapping test that saved movies keep crew directors, then store directors on save; verify co-directors and movies without directors.
- [ ] 1.3 Observe failing sync mapping tests for `directors` upload and download (absent → unresolved), then implement the Firestore field.
- [ ] 1.4 Verify whether an older-client upload removes the `directors` field and record the result and accepted limitation in design.md.

## 2. Removable backfill (test first)

- [ ] 2.1 Observe a failing MockWebServer test for `movie/{id}/credits` director extraction, then implement the request in `core/sync`.
- [ ] 2.2 Observe failing worker tests for success, empty credits, 404, transient retry, cancellation and nothing remaining, then implement `DirectorBackfillWorker` with the unresolved-watched DAO query and upload scheduling.
- [ ] 2.3 Enqueue the unique backfill on app start and after sync download; verify KEEP semantics and the `director_backfill_finished` event in unit tests.
- [ ] 2.4 Observe a failing `MovieDetailViewModel` test for the unresolved-watched fallback, then implement it; verify no write for resolved or unwatched movies and unchanged list flags.

## 3. Collection presentation (test first)

- [ ] 3.1 Observe failing grouping-function tests for date watched, director, genre, decade, ordering, multi-membership and fallback groups, then implement the pure grouping mapper.
- [ ] 3.2 Observe failing ViewModel tests for mode/grouping intents, persisted defaults, read/write failure, search within groupings and the director navigation effect, then implement state, DataStore data source and analytics.
- [ ] 3.3 Observe failing plain Compose tests for the toggle, chips, Shelf poster click, director heading click and hidden controls when empty, then build the Shelf grid, sticky headers and controls with design-system tokens, English/Spanish resources and previews.
- [ ] 3.4 Record and inspect light/dark Paparazzi baselines for List and Shelf in Director and Decade groupings and Spanish large-font chips; verify independently.

## 4. Navigation and validation

- [ ] 4.1 Observe a failing app journey for Watched → director heading → person profile → Back with mode, grouping and scroll preserved, then wire navigation in `app`.
- [ ] 4.2 Run affected module tests, `verifyPaparazziDebug`, `:architecture-tests:test` and connected tests, then the aggregate gate; record commands and results in implementation.md.
- [ ] 4.3 Manual checks: upgrade from 1.1.1 with watched movies (online and offline), sign-in restore on a second device, Spanish, light/dark, TalkBack on toggle/chips/posters, large font.
- [ ] 4.4 Update PRD F-31 status, report changed classes, and archive after human review.
