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

## 4. Navigation and validation

- [x] 4.1 Observe a failing app journey for Watched → director heading → Director collection → film detail → Back → Back, then add the route and replace F-31's person-profile link.
- [ ] 4.2 Run affected tests, `verifyPaparazziDebug`, `:architecture-tests:test`, connected tests and the aggregate gate; record results in implementation.md.
- [ ] 4.3 Manual checks: offline open, slow network, Spanish, light/dark, TalkBack on marks and dimmed posters, large font.
- [ ] 4.4 Update PRD F-32 status, report changed classes, and archive after human review.
