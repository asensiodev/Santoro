## 0. Agreement

- [x] 0.1 Review proposal, spec and design with the user (chip order, "#" last, leading expand control, collapse state per session) and record the confirmation and test boundaries in design.md.

## 1. Grouping model and saved preference (test first)

- [x] 1.1 Observe a failing `DefaultUserPreferencesRepositoryTest` case where a stored `DATE_WATCHED` reads as `RECENT` and an unset value defaults to `RECENT`, then replace `DATE_WATCHED` with `RECENT`, add `ALPHABETICAL` and map the legacy name; verify `:core:data:testDebugUnitTest` and update every compile-time reference (ViewModel defaults, journey fakes, feature tests).
- [x] 1.2 Observe failing `WatchedGroupMapperTest` cases for Recent (one group, no label, repository order kept), then implement it and remove `MovieUi.watchedDate`, its formatter and `WatchedGroupLabel.UnknownDate`; verify `MovieUiMapperTest` is updated and the module's unit tests pass.
- [x] 1.3 Observe failing `WatchedGroupMapperTest` cases for A–Z (case and accents ignored, "Érase" under E, digits and symbols under a final "#", "The Matrix" under T, blank title under "#", order within a letter by title then release date), then implement the A–Z grouping; verify the module's unit tests pass.

## 2. Collapse state (test first)

- [ ] 2.1 Observe failing `WatchedMoviesViewModelTest` cases: director groups start collapsed and collapsible, `ToggleGroup` expands and collapses, a new director group starts collapsed, an active query expands matches without changing the expanded set, clearing the query restores it, and the set survives switching grouping away and back; then implement `expandedGroupKeys` and `ToggleGroup`.
- [ ] 2.2 Observe a failing analytics case for the `recent` and `alphabetical` grouping values, then confirm the existing tracking reports them; record whether this was test-first or regression coverage.

## 3. UI (test first)

- [ ] 3.1 Observe failing `WatchedCollectionScreenTest` cases: Recent shows movies without headings in Shelf and List; A–Z shows letter headings and a "#" heading announced as numbers and symbols; chips appear in the order Recent, A–Z, Director, Genre, Decade; then build them with English and Spanish strings.
- [ ] 3.2 Observe failing `WatchedCollectionScreenTest` cases for the director heading: a collapsed heading shows the count and no movies, the expand control is a 48dp button announcing the director and expanded/collapsed state, tapping it does not open the Director collection, tapping the name still does, Unknown director has the control but no director button, and no control is shown while searching or in other groupings; then build the heading with design-system tokens and previews.
- [ ] 3.3 Record and inspect light and dark Paparazzi baselines for Recent (Shelf and List), A–Z, Director collapsed and partly expanded, and Spanish large font; delete baselines for the removed Date watched states; verify with `verifyPaparazziDebug`.

## 4. Navigation and validation

- [ ] 4.1 Update the watched collection journey: Director grouping starts collapsed, expand a director, open one of its movies, Back keeps it expanded, open the Director collection from the name, Back keeps the state; verify `:app:connectedJourneyTestAndroidTest` on `emulator-5554`.
- [ ] 4.2 Run affected tests, `verifyPaparazziDebug`, `:architecture-tests:test`, connected tests for watched-movies and the app journeys on `emulator-5554`, and the aggregate gate; record results in implementation.md.
- [ ] 4.3 Manual checks: upgrade from 1.3.0 with Date watched saved, Recent and A–Z in Shelf and List with a real collection, collapse and search in Director, Spanish, light and dark, TalkBack on headings and expand controls, large font.
- [ ] 4.4 Update the PRD F-34 status and the watched-collection spec Purpose (it still mentions grouping by date watched), report changed classes, and archive after human review.
