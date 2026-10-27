## Why

Collectors enjoy seeing what is missing as much as what they own. When a director comes up in conversation, a user wants to see that director's films with the ones they have watched clearly marked, and how far they are from completing the filmography. PRD F-32 builds on F-31's director grouping with a collection-aware director filmography.

Depends on `organize-watched-collection` (F-31) for stored directors and the Director grouping.

## What Changes

- Add a Director collection screen opened from a director heading in the Watched Director grouping, replacing F-31's link to the generic person profile.
- Show the director's feature films chronologically: watched films at full emphasis with a watched mark, unwatched films as dashed placeholders, and a "Watched X of Y" progress summary.
- Add a Show pending switch, on by default and persisted on the device, to hide unwatched films.
- Open the existing movie detail from any film and the existing person profile from the screen header.
- Count only feature films directed by the person. A watched film by that director always counts, even if the filter would exclude it.

## Capabilities

### New Capabilities

- `director-collection`: Collection-aware director filmography with watched marks, completion and pending visibility.

### Modified Capabilities

- `watched-collection`: A director heading opens the Director collection screen instead of the person profile.

## Impact

- `feature/watched-movies/api`: new `DirectorCollectionRoute(personId, name)`.
- `feature/watched-movies/impl`: screen, ViewModel, feature-film filter and preference.
- `core/domain` / `feature/person-detail/impl`: expose `video` and genre IDs on `PersonMovieCredit` so the filter can exclude videos and documentaries; existing person screens unchanged.
- `app`: navigation wiring.
- English/Spanish resources, Paparazzi baselines, journey test.

## Non-Goals and Follow-ups

- **Follow-up: user-selectable filmography scope.** This change implements feature films only. A later change will let the user choose what to include (feature films only or everything: documentaries, shorts and other credits). The filter is a single function so a scope option can be added without redesigning the screen.
- **Follow-up: mark short films.** TMDB person credits carry no runtime, so shorts cannot be told apart without one detail request per film. This change accepts that an unflagged short may appear as a normal film. A later change can label shorts (for example a "Short" tag on the tile) using runtime from movie detail responses cached as users open them, or a batched lookup, and the scope option above can then exclude them.
- **Follow-up: quick add to Watchlist from an unwatched placeholder.** Saving a movie stores a complete `Movie` (overview, genres, runtime, directors), but a filmography credit only has ID, title, poster and date. Saving from the credit alone would store incomplete rows that later screens and sync rely on. In this change, tapping a placeholder opens the existing movie detail, where the user adds it to the Watchlist with complete data. A later change can add a one-tap bookmark that loads the movie detail in the background, saves it, and shows the saved state on the tile.
- Completion figures in Watched group headings (would require one filmography request per director on screen).
- Acting filmographies or other crew roles; cross-device sync of the Show pending preference.
