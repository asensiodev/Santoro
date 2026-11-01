## 0. Agreement

- [x] 0.1 Confirm the test boundaries listed in design.md with the user before adding tests; record the confirmation in design.md.

## 1. Director data (test first)

- [x] 1.1 Observe a failing migration test for Room 5 → 6 with an existing v5 watched row, then add the nullable `directors` column, entity/mapper changes and the domain `directors` value; verify unresolved vs resolved-empty round trips.
- [x] 1.2 Observe a failing detail-mapping test that saved movies keep crew directors, then store directors on save; verify co-directors and movies without directors.
- [x] 1.3 Observe failing sync mapping tests for `directors` upload and download (absent → unresolved), then implement the Firestore field.
- [x] 1.4 Verify whether an older-client upload removes the `directors` field and record the result and accepted limitation in design.md.

## 2. Removable backfill (test first)

- [x] 2.1 Observe a failing MockWebServer test for `movie/{id}/credits` director extraction, then implement the request in `core/sync`.
- [x] 2.2 Observe failing worker tests for success, empty credits, 404, transient retry, cancellation and nothing remaining, then implement `DirectorBackfillWorker` with the unresolved-watched DAO query and upload scheduling.
- [x] 2.3 Enqueue the unique backfill on app start and after sync download; verify KEEP semantics and the `director_backfill_finished` event in unit tests.
- [x] 2.4 Observe a failing `MovieDetailViewModel` test for the unresolved-watched fallback, then implement it; verify no write for resolved or unwatched movies and unchanged list flags.

## 3. Collection presentation (test first)

- [x] 3.1 Observe failing grouping-function tests for date watched, director, genre, decade, ordering, multi-membership and fallback groups, then implement the pure grouping mapper.
- [x] 3.2 Observe failing ViewModel tests for mode/grouping intents, persisted defaults, read/write failure, search within groupings and the director navigation effect, then implement state, `UserPreferencesRepository` persistence and analytics.
- [x] 3.3 Observe failing plain Compose tests for the toggle, chips, Shelf poster click, director heading click and hidden controls when empty, then build the List rows, three-column Shelf, group headings and controls with design-system tokens, English/Spanish resources and previews.
- [x] 3.4 Record and inspect light/dark Paparazzi baselines for List and Shelf in Director and Decade groupings and Spanish large-font chips; verify independently.

## 4. Navigation and validation

- [x] 4.1 Observe a failing app journey for Watched → director heading → person profile → Back with mode, grouping and scroll preserved, then wire navigation in `app`.
- [x] 4.2 Run affected module tests, `verifyPaparazziDebug`, `:architecture-tests:test` and connected tests, then the aggregate gate; record commands and results in implementation.md.
- [x] 4.3 (after phase 5) Manual checks: upgrade from 1.1.1 with watched movies (online and offline), sign-in restore on a second device, Spanish, light/dark, TalkBack on toggle/chips/posters and on search open/close/clear, search open with system Back, large font. Upgrade from 1.1.1 cannot run on the user's Play-installed device with a debug build; record it as an accepted gap unless a signed build is available.
- [x] 4.4 Update PRD F-31 status, report changed classes, and archive after human review.

## 5. Screen revision (test first)

- [x] 5.0 Confirm the screen revision test boundaries in design.md with the user; record the confirmation there.
- [x] 5.1 Observe failing `WatchedMoviesViewModel` tests for the stats removal, distinct total count, `OpenSearch` / `CloseSearch`, query kept across grouping changes and search closing when the collection becomes empty, then implement the state and intents.
- [x] 5.2 Observe failing plain Compose tests for the header, on-demand search (focus, back action, system Back, clear), title-only Loading/Error/Empty, tonal director headings and absence of stats, then build the header, restyle the chips like the Search genre chips, update the headings and remove `WatchedStatsDashboard`, with English/Spanish resources and previews.
- [x] 5.3 Update `WatchedMoviesFeatureTest` to drop the stats assertions and run it on the emulator.
- [x] 5.4 Delete the stats dashboard screenshot test and golden; record and inspect light/dark Paparazzi captures for Shelf Director, List Date watched, search open, no results while searching, Empty and Spanish large font; verify independently.
- [x] 5.5 Update the PRD: Watched screen description, F-20 (the dashboard leaves the Watched screen and moves to a future statistics screen), a new planned statistics screen entry, F-31 behaviour, and the version history.
- [x] 5.6 Rerun the 4.2 validation set and record commands and results in implementation.md.

## 6. Release date restoration (test first)

- [x] 6.1 Observe failing sync mapping tests for `releaseDate` upload and download (absent/null → unresolved, empty → resolved without date) and `MovieSyncData` ↔ entity mapping, then add the Firestore field and mappings.
- [x] 6.2 Observe failing Room instrumented tests for the unresolved query including date-only unresolved movies and for the conditional `storeResolvedMetadata` (fills only missing values, no change reported when nothing is filled), then implement the DAO, repository and domain contract changes.
- [x] 6.3 Observe a failing MockWebServer test for `movie/{id}?append_to_response=credits` returning directors and release date (missing date → empty, 404 → both empty), then implement `MovieMetadataRemoteDataSource`.
- [x] 6.4 Observe failing worker tests for a date-only unresolved movie, then resolve both values in `DirectorBackfillWorker`; keep existing worker cases green.
- [x] 6.5 Observe failing `MovieDetailViewModel` tests for storing a missing release date on a remote load and no upload on a Room load, then update the fallback.
- [x] 6.6 Update test fakes (journey, watchlist, movie-detail), rerun the 4.2 validation set, verify on the emulator that restored movies leave Undated after the backfill, and record results in implementation.md.

## 7. Offline posters and detail (test first)

- [x] 7.1 Observe a failing Compose test for a Shelf poster that fails to load showing the placeholder with the title, then draw the placeholder under the image in `MovieCard` and the List thumbnail.
- [x] 7.2 Observe a failing Compose test for movie detail without cast hiding the Cast heading, then hide it, use a neutral backdrop base without an error image and a centered poster placeholder; record and inspect the affected movie-detail and watched Paparazzi baselines.
- [x] 7.3 Run affected checks, verifyPaparazziDebug, the aggregate gate and connected watched and movie-detail suites; verify offline on the emulator and record results in implementation.md.

## 8. Offline cold start crash (test first)

Outside the watched collection capability; included in this release at the user's request on 2026-11-01 ("quizá mejor lo metemos en esta misma versión"), because release 1.1.1 crashes on a cold start without network.

- [x] 8.1 Observe a failing `ApiKeyRefresherTest` for a Remote Config fetch failure, then make `ensureKeyUpToDate` keep the local key without throwing while propagating cancellation.
- [x] 8.2 Verify on the emulator that a cold start in airplane mode no longer crashes, run the network module checks and the aggregate gate, and record results in implementation.md.

## 9. Manual-check adjustments (test first)

- [x] 9.1 Observe a failing `WatchedMoviesViewModel` test for `CloseSearch` showing the full collection before the query debounce elapses, then regroup the last full collection on close.
- [x] 9.2 Observe a failing Compose test for the grid returning to the top when search closes, then scroll to the top on that transition.
- [x] 9.3 Switch the selected grouping chip colors to `secondaryContainer` / `onSecondaryContainer`; record and inspect the watched Paparazzi baselines; run affected and aggregate checks and connected watched tests; record results in implementation.md.
