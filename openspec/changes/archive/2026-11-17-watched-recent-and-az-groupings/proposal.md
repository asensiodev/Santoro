## Why

The Watched screen's default grouping splits the collection by the month a movie was marked as watched. That date is only when the movie was added to the app, not when the user saw it, so headings like "March 2026" are misleading for anyone who imported their history in a few sittings. Users want to see their whole collection simply, either in the order they added movies or alphabetically. In the Director grouping, a large collection becomes a very long scroll; collapsing directors lets the screen read as a list of directors. PRD F-34 covers this, for release together with F-32 or right after it.

Depends on `organize-watched-collection` (F-31, archived) for the groupings, and on `director-watched-filmography` (F-32, archived) for the director heading that opens the Director collection.

## What Changes

- Replace the Date watched grouping with **Recent**, the new default: every watched movie in one section with no group headings, most recently added first.
- Add an **A–Z** grouping with letter headings. Letters ignore case and accents; titles starting with a number or symbol go under "#"; leading articles ("The", "El", "La") are not skipped.
- A saved Date watched preference opens as Recent.
- In the **Director** grouping, each heading gets a separate expand/collapse control. Directors start collapsed so the screen reads as a list of directors with counts. Tapping the director's name still opens the Director collection. While a search query is active, every director with matches is shown expanded. Collapsed state lives for the screen session and is not saved.
- Recent and A–Z work in Shelf and List.
- Remove the month-of-watch label from the Watched presentation; the stored timestamp only orders Recent and is never shown as a watch date.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `watched-collection`: the Group the collection requirement changes (Recent and A–Z replace Date watched; Recent becomes the default and a saved Date watched preference opens as Recent), and a new Collapse director groups requirement adds the expand/collapse control in the Director grouping.

## Impact

- `core/domain`: `WatchedGrouping` replaces `DATE_WATCHED` with `RECENT` and adds `ALPHABETICAL`.
- `core/data`: `DefaultUserPreferencesRepository` reads a saved `DATE_WATCHED` as `RECENT`.
- `feature/watched-movies/impl`: grouping mapper, group model (optional heading), ViewModel collapse state and intent, Watched screen heading and grouping chips; `MovieUi` drops the month label.
- `app`: journey fakes and the watched collection journey.
- English and Spanish strings (chip labels, "#" heading, expand/collapse descriptions), Paparazzi baselines.
- Analytics: the grouping parameter reports `recent` and `alphabetical` instead of `date_watched`.

## Non-Goals

- Recording, showing or editing the date a movie was watched (decision 2026-11-01): no watch-date picker, no "watched before" state, no monthly activity.
- A new view mode for collapsed groups, or collapsing in Genre, Decade or A–Z.
- An alphabetical side index (contacts-style scrubber); considered and discarded for complexity.
- Skipping leading articles when sorting A–Z.
- Saving collapsed directors across app restarts or devices.
