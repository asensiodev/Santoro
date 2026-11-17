## Context

See proposal.md for motivation and specs/watched-collection/spec.md for the required behavior.

Current state (F-31 and F-32):

- `WatchedGrouping` (`core/domain`) has `DATE_WATCHED`, `DIRECTOR`, `GENRE`, `DECADE`. The choice is stored on the device by `DefaultUserPreferencesRepository` as the enum name through the encrypted key-value store; an unknown stored name falls back to the default.
- `WatchedGroupMapper.toWatchedGroups(grouping)` turns the watched movies into `WatchedGroupUi(key, label, movies, directorId)`. Date watched groups by `MovieUi.watchedDate`, a "MMMM yyyy" label formatted from `watchedAt`.
- Room already returns watched movies, and search matches, ordered by `watchedAt` descending.
- `WatchedMoviesViewModel` keeps the last collection and matches, regroups on `ChangeGrouping`, persists mode and grouping, and records analytics with `grouping.name.lowercase()`.
- `WatchedMovieList` renders every group as a full-span heading item followed by its movies in one `LazyVerticalGrid`, in Shelf or List. The director heading is a single button with a trailing chevron that opens the Director collection.
- Main tabs navigate with `saveState`/`restoreState`, so the Watched ViewModel survives tab switches, configuration changes and visits to the Director collection or a movie detail.

## Goals / Non-Goals

**Goals:**

- Keep the mapper a pure function of movies and grouping; keep collapse state in the ViewModel, outside the mapper.
- One list implementation for all groupings and both modes; Recent is a group without a heading, not a separate screen.
- Stable lazy keys so expanding, collapsing and regrouping keep the scroll position.

**Non-Goals:**

- No database or sync changes: `watchedAt` keeps its meaning and is only read for ordering.
- No SavedStateHandle for collapse state: it is session state, see Decisions.

## Agreement and test boundaries

Confirmed with the user on 2026-11-17: chip order Recent, A–Z, Director, Genre, Decade with Recent as default; A–Z ignores case and accents (Ñ under N), "#" last and announced as numbers and symbols, no article skipping; director heading with a leading expand control and the name button opening the Director collection; groups collapsed at app start, kept for the session, expanded while searching; analytics values `recent` and `alphabetical`.

Test boundaries:

| Behavior | Boundary and test |
|---|---|
| Saved grouping, default and legacy Date watched | `DefaultUserPreferencesRepository` with a mocked `SecureKeyValueStore` (`DefaultUserPreferencesRepositoryTest`) |
| Recent and A–Z grouping rules | Pure mapper `toWatchedGroups` (`WatchedGroupMapperTest`) |
| Collapse state, search expansion, analytics values | `WatchedMoviesViewModel.process(intent)` and `uiState` with mocked use cases and preferences (`WatchedMoviesViewModelTest`) |
| Headings, "#" semantics, chips, expand control, callbacks | Plain state-driven Compose tests (`WatchedCollectionScreenTest`) |
| Layout in Shelf, List, light, dark, Spanish large font | Paparazzi (`WatchedMoviesScreenScreenshotTest`) |
| Expanded state across movie detail, Director collection and Back | App journey (`MainActivityWatchedCollectionJourneyTest`) |

## Decisions

### Enum and saved preference

Replace `DATE_WATCHED` with `RECENT` and add `ALPHABETICAL`; enum order is the chip order (Recent, A–Z, Director, Genre, Decade). The default becomes `RECENT` in the repository and in `WatchedMoviesUiState`.

The repository maps the stored legacy name `DATE_WATCHED` to `RECENT` explicitly instead of relying on the unknown-name fallback, so the migration is visible and tested even if the default changes later. No rewrite of the stored value is needed: the next grouping change stores the new name.

Alternative considered: keep `DATE_WATCHED` as the enum name and only change its behavior. Rejected because the name would no longer describe the behavior and analytics would keep reporting `date_watched`.

Rollback: Play cannot install an older version over a newer one. If a build with this change were reverted in source, an older build would read `RECENT` or `ALPHABETICAL` as unknown and fall back to its own default, Date watched. No data loss.

### Group model

`WatchedGroupUi.label` becomes nullable; Recent produces one group with key `recent`, label `null` and every movie in repository order. `WatchedMovieList` skips the heading item when the label is null. This keeps one rendering path and the `group.key + movie id` item keys.

Two fields are added for the Director grouping: `isCollapsible` (true for director groups, including Unknown director) and `isExpanded`. The mapper sets `isCollapsible` and leaves `isExpanded = true`; the ViewModel decides expansion (next section). The screen renders only the heading of a collapsed group.

`MovieUi.watchedDate`, its formatter and `WatchedGroupLabel.UnknownDate` are removed, so the month of the stored timestamp can no longer reach the UI.

### A–Z grouping

- Heading letter: trim leading whitespace, take the first character, decompose with `java.text.Normalizer` (NFD), drop combining marks and upper-case with `Locale.ROOT`. If the result is `A`–`Z` it is the heading; otherwise the movie goes to the "#" group. So "Érase" → E, "Ñu" → N, "12 Angry Men" → #, "¡Átame!" → #, and non-Latin titles → #.
- Group order: A to Z, then "#" last, as in the Android contacts app.
- Within a group: `java.text.Collator` for `Locale.ROOT` with primary strength (ignores case and accents), then release date. `Locale.ROOT` keeps tests deterministic regardless of device language; Spanish-specific ordering (ñ after n) only differs for rare titles within the N group.
- Labels: letters are `WatchedGroupLabel.Text("A")`; the "#" group is a new `WatchedGroupLabel.OtherTitles` rendered from a string resource ("#") with a spoken description ("Numbers and symbols" / "Números y símbolos"), because TalkBack reads "#" as "hash".
- Leading articles are not skipped (PRD decision): language detection for titles is unreliable and users know "The Matrix" under T from other apps.

### Collapse state

`WatchedMoviesUiState` gains `expandedGroupKeys: Set<String>` (empty at start, so every director group starts collapsed) and a new intent `ToggleGroup(key)`. The ViewModel applies expansion when it publishes groups: a collapsible group is expanded when its key is in the set, or when a non-blank query is active. A new director group starts collapsed because its key is not in the set. The set is kept when switching grouping away and back within the session.

The state lives in the ViewModel, which already survives configuration changes, tab switches (`saveState`/`restoreState`) and the back stack. It is not written to SavedStateHandle or preferences, so after process death or an app restart every director starts collapsed, matching "not saved across app restarts". Restoring collapsed state after process death would cost a SavedStateHandle field for a state the spec does not require.

While a query is active, the screen hides the expand/collapse control and every group with matches is expanded; the set is not modified, so clearing or closing the search restores the user's expanded groups.

### Director heading layout

The heading becomes a row with a leading expand/collapse icon button (48dp, `KeyboardArrowDown` rotated when expanded) and the existing director button (name, count, trailing chevron right) filling the rest. The toggle uses `Role.Button`, a content description with the director name ("Expand Christopher Nolan" / "Collapse Christopher Nolan") and a state description (expanded / collapsed). Unknown director gets the toggle but no director button. Placing the toggle first reads as a tree and keeps the trailing chevron meaning "open".

Alternative considered: a trailing expand icon next to the chevron. Rejected: two chevrons side by side are easy to mistake for each other.

### Analytics

No new events. The existing grouping parameter now reports `recent` and `alphabetical` (enum name lowercased). Expanding or collapsing a director is not tracked; it is frequent and adds no product decision at this stage.

## Risks / Trade-offs

- [Users who liked monthly headings lose them] → Intended by the 2026-11-01 decision: the month is when the movie was added, not watched. Recent keeps the same order without the misleading labels.
- [Titles in non-Latin scripts land in "#"] → TMDB titles come in the app language (English or Spanish), so these are rare; they are still listed and searchable.
- [Collapsed directors hide movies from users who expect a full list] → Recent and A–Z show the full list; the Director grouping header still shows each count, and search expands matches.
- [Collapse state resets after process death while the app is in the background] → Low harm: the user sees directors collapsed, the documented starting state.
- [Expanding near the end of a long list moves content] → Lazy items use stable keys, so the expanded group's heading stays in place and its movies insert below it.

## Migration Plan

1. Ship the enum change with the explicit `DATE_WATCHED` → `RECENT` mapping and its repository test.
2. Users who never chose a grouping start on Recent; users who chose Director, Genre or Decade keep their choice.
3. Rollback is a normal later release; see "Enum and saved preference".
