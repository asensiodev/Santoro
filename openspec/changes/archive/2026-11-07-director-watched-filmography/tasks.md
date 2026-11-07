## 0. Agreement

- [x] 0.1 Confirm `organize-watched-collection` is implemented and the test boundaries in design.md with the user; record the confirmation in design.md.

## 1. Data and filter (test first)

- [x] 1.1 Observe a failing person credits HTTP test for `video` and `genre_ids`, then map them onto `PersonMovieCredit`; verify existing person tests stay green.
- [x] 1.2 Observe failing filter tests for director job, video, documentary, unreleased, undated, watched-documentary inclusion and dedupe, then implement `directedFeatureFilms`.

## 2. Screen state (test first)

- [x] 2.1 Observe failing ViewModel tests for local-first state, success, failure, retry, cancellation and watched change without refetch, then implement the ViewModel.
- [x] 2.2 Observe a failing test for the persisted Show pending preference, then implement it in `UserPreferencesRepository` alongside F-31's view preferences.

## 3. UI (test first)

- [x] 3.1 Observe failing plain Compose tests for summary, watched/unwatched semantics, failed-poster title fallback, switch, callbacks and inline retry, then build the screen (dimmed unwatched posters, bottom grid padding) with design-system tokens, English/Spanish resources and previews.
- [x] 3.2 Record and inspect light/dark Paparazzi baselines for partial, pending hidden, loading, error and Spanish large font; verify independently.

- [x] 3.3 Observe failing ViewModel and Compose tests for the header photo (loaded, missing, failed without affecting the filmography), then load it through `PersonRepository.getPerson` and show it next to the name; verify the Paparazzi baselines.

## 5. Short films (revision 2026-11-05)

- [x] 5.1 Observe a failing HTTP test for `MovieRuntimeRepository` (runtime parsing, cache, failed lookup unknown), then implement it in person-detail impl with bounded parallel requests.
- [x] 5.2 Observe failing filter tests for short classification (40 and 41 minutes, unknown runtime, watched short), then split the filmography into feature films and shorts.
- [x] 5.3 Observe failing ViewModel tests for summary over feature films only, shorts count, local watched shorts and runtime lookup only after credits, then implement.
- [x] 5.4 Observe failing Compose tests for the Shorts section (heading, count, hidden with pending off and no watched shorts), then build it; record and inspect Paparazzi baselines.

## 6. Consistent status marks (revision 2026-11-05)

- [x] 6.1 Move `MovieStatusRibbon` to `core/design-system` with a design-system status enum; Search keeps identical rendering (existing Paparazzi goldens and status semantics tests unchanged).
- [x] 6.2 Observe failing ViewModel tests for Watchlist membership on unwatched films (live updates, watched wins), then observe the local watchlist flow.
- [x] 6.3 Observe failing Compose tests for the watched and watchlist ribbons and their announcements, then use the shared ribbon with softer dimming; re-record and inspect the Director collection baselines.

## 7. List layout (revision 2026-11-06)

- [x] 7.1 Share the ribbon's icon, colors and descriptions in `core/design-system` for a list status mark; Search rendering unchanged.
- [x] 7.2 Observe failing Compose tests for title and release year in each row (undated shows the title only), then replace the poster grid with a list of `DirectorFilmRow`; re-record and inspect the Director collection baselines.
- [x] 7.3 Observe failing UiState and Compose tests for the films-left count, completion message and visible status labels, then replace the trailing badge with an inline `MovieStatusLabel` and rebuild the summary as a card; re-record and inspect the baselines.

## 4. Navigation and validation

- [x] 4.1 Observe a failing app journey for Watched → director heading → Director collection → film detail → Back → Back, then add the route and replace F-31's person-profile link.
- [x] 4.2 Run affected tests, `verifyPaparazziDebug`, `:architecture-tests:test`, connected tests and the aggregate gate; record results in implementation.md.
- [x] 4.3 Manual checks: offline open, slow network, Spanish, light/dark, TalkBack on status labels and dimmed rows, large font.
- [x] 4.4 Update PRD F-32 status, report changed classes, and archive after human review.
