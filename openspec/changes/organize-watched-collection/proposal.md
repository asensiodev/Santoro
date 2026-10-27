## Why

Watched movies are a growing personal collection, but the Watched screen only offers one chronological list. When cinema comes up with friends, a user wants to answer "which Scorsese films have I seen?" or "what science fiction have I watched?" in seconds. PRD F-31 turns the Watched screen into a browsable collection with a simple and a visual view, grouped by the dimensions people use when talking about films.

## What Changes

- Add two view modes on the Watched screen: List (the current rows) and Shelf (a three-column poster grid). The user switches with an icon toggle; the choice persists on the device.
- Add Group by chips: Date watched (the current monthly grouping), Director, Genre and Decade. The choice persists on the device. Each group shows a sticky heading with its movie count.
- Director grouping opens the existing person profile (F-29) from its heading. The collection-aware director filmography follows in F-32.
- Store each movie's directors locally and in Firestore, captured when a movie is saved from detail. Resolve directors for already-watched movies once through a removable background backfill, plus a fallback when a watched movie's detail is opened.
- Keep search and the stats dashboard working in every mode and grouping.
- Record lightweight analytics for mode and grouping changes and for backfill progress, so adoption and backfill removal can be decided with data.

## Capabilities

### New Capabilities

- `watched-collection`: View modes, grouping and director data for the watched collection.

### Modified Capabilities

None. Watched behavior predates OpenSpec; this delta documents only the added collection behavior.

## Impact

- `feature/watched-movies/impl`: UI state, intents, ViewModel grouping, Shelf grid, chips, DataStore preferences and navigation to person detail.
- `core/domain`: director identity on `Movie` (unresolved vs resolved-empty).
- `core/database`: Room migration 5 → 6 adding a nullable directors column, mapper changes and a query for watched movies with unresolved directors.
- `core/sync`: Firestore `directors` field on upload and download; a unique one-time director backfill worker using TMDB `movie/{id}/credits`.
- `feature/movie-detail/impl`: persist directors when saving, and the open-detail fallback for unresolved watched movies.
- `app`: navigation from the Watched screen to the existing person profile.
- English/Spanish string resources and Paparazzi baselines for the Watched screen.

## Non-Goals

- Director filmography with seen/unseen marks and completion (F-32, separate change).
- Custom user folders or lists; personal ratings (F-19 remains discarded).
- Grouping by country, cast or rating; syncing view preferences across devices.
- Changing Watchlist, search or detail behavior beyond saving directors.
