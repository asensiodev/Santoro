## Why

Watched movies are a growing personal collection, but the Watched screen only offers one chronological list. When cinema comes up with friends, a user wants to answer "which Scorsese films have I seen?" or "what science fiction have I watched?" in seconds. PRD F-31 turns the Watched screen into a browsable collection with a simple and a visual view, grouped by the dimensions people use when talking about films.

## What Changes

- Add two view modes on the Watched screen: Shelf (the current poster grid, now three fixed columns, default) and List (new rows with year and director). The user switches with an icon toggle; the choice persists on the device.
- Add Group by chips: Date watched (the current monthly grouping), Director, Genre and Decade. The choice persists on the device. Each group shows a heading with its movie count.
- Director grouping opens the existing person profile (F-29) from its heading. The collection-aware director filmography follows in F-32.
- Store each movie's directors and release date locally and in Firestore (revision 2026-11-01: restored movies lost their release date and fell under Undated), captured when a movie is saved from detail. Resolve directors and missing release dates for already-watched movies once through a removable background backfill, plus a fallback when a watched movie's detail is opened.
- Redesign the screen header (revision 2026-11-01): the title "Watched" with the total movie count, a search button that opens search on demand, and the view toggle beside it. Grouping chips use the rounded style of the Search screen genre chips, and resolved director headings become tonal rows that read as tappable. Search keeps working in every mode and grouping.
- Show the poster placeholder with the title when a Watched poster fails to load, and polish movie detail without remote data: neutral header instead of a stretched placeholder, and no empty Cast heading (revision 2026-11-01).
- Remove the stats dashboard from the Watched screen. The stats domain logic stays for a future dedicated statistics screen, recorded as a PRD follow-up.
- Record lightweight analytics for mode and grouping changes and for backfill progress, so adoption and backfill removal can be decided with data.

## Capabilities

### New Capabilities

- `watched-collection`: View modes, grouping and director data for the watched collection.

### Modified Capabilities

None. Watched behavior predates OpenSpec; this delta documents only the added collection behavior.

## Impact

- `feature/watched-movies/impl`: UI state, intents, ViewModel grouping, List rows, chips, collection header with on-demand search, stats dashboard removal and navigation to person detail.
- `core/domain` / `core/data`: view mode and grouping preferences in the existing `UserPreferencesRepository`.
- `core/domain`: director identity on `Movie` (unresolved vs resolved-empty).
- `core/database`: Room migration 5 → 6 adding a nullable directors column, mapper changes and a query for watched movies with unresolved directors.
- `core/sync`: Firestore `directors` and `releaseDate` fields on upload and download; a unique one-time backfill worker resolving directors and release dates with TMDB `movie/{id}?append_to_response=credits`.
- `feature/movie-detail/impl`: persist directors and release dates when saving, the open-detail fallback for unresolved watched movies, and offline header and cast polish.
- `app`: navigation from the Watched screen to the existing person profile.
- English/Spanish string resources and Paparazzi baselines for the Watched screen.

## Non-Goals

- Director filmography with seen/unseen marks and completion (F-32, separate change).
- Custom user folders or lists; personal ratings (F-19 remains discarded).
- Grouping by country, cast or rating; syncing view preferences across devices.
- Changing Watchlist, search or detail behavior beyond saving directors.
- Completion progress in Watched group headings; F-32 shows completion on the Director collection screen only.
- The dedicated statistics screen (future PRD item); this change only removes the dashboard from the Watched screen.
