## Context

See proposal.md. This change starts after `organize-watched-collection` is implemented: movies carry stored directors and the Watched screen has a Director grouping whose heading navigates to the person profile.

Relevant observed code:

- `PersonRepository.getMovieCredits(id)` (core/domain, implemented in person-detail impl) returns `PersonFilmography(acting, crew)` with `PersonMovieCredit(movieId, title, posterPath, releaseDate, roles)`; roles hold TMDB job strings. `video` and `genre_ids` from TMDB are not mapped.
- The Watched feature reads watched movies from Room as a flow.

## Goals / Non-Goals

**Goals:** One screen combining a remote filmography with local watched state; a single, replaceable feature-film filter; offline-tolerant UX.

**Non-Goals:** User-selectable scope (follow-up), quick add to Watchlist, filmography caching across sessions, completion in group headings.

## Decisions

### Route and ownership

`DirectorCollectionRoute(personId: Int, name: String)` in `feature/watched-movies/api`; screen and ViewModel in watched-movies impl. The name is passed so the header renders before any request. The watched feature depends on the `PersonRepository` contract in `core/domain`, not on person-detail impl.

Alternative: extend the person filmography screen with watched marks. It mixes a discovery screen with collection semantics and couples person-detail to watched data.

### Credit fields

Add `isVideo: Boolean` and `genreIds: List<Int>` to `PersonMovieCredit`, mapped in `PersonApiMapper`. Existing person screens ignore them.

### Feature-film filter

A pure function `directedFeatureFilms(credits, watchedByDirector, today)`: crew credits with role `Director`, not `isVideo`, without genre 99 (Documentary), release date ≤ today; union with the user's watched movies having this person among stored directors; dedupe by ID. Short films are not detectable from TMDB credits without one request per movie; a short appears only if TMDB does not flag it as video. This is accepted: shorts are rare in mainstream directors' credits and appear as one extra placeholder, while per-movie requests would multiply network cost. The follow-up scope option will replace this function's fixed policy with a user-selected scope.

### State and lifecycle

The ViewModel combines the local watched flow (filtered by director ID) with a one-shot filmography request owned by a cancellable job; retry replaces the job (latest wins). Watched state changes recompute marks and the summary without refetching. `CancellationException` propagates and never becomes an error. Show pending is a DataStore preference shared with F-31's preferences data source.

### Data sources and side effects

- Allowed: TMDB `person/{id}/movie_credits` (existing), Room reads, DataStore preference, navigation to movie detail and person profile.
- Forbidden: writes to movies, list flags or Firestore; persisting filmographies.

### Failure UX

Local watched films render first. Request failure shows an inline error with Retry above the grid; offline shows "X watched" without a total. Missing posters use existing placeholders.

## Test Boundaries (pending user agreement)

- Person credits HTTP integration test for `video` and `genre_ids` mapping.
- Pure filter tests: director job, video, documentary, unreleased, undated, watched-documentary inclusion, dedupe.
- ViewModel tests: local-first state, success, failure and retry, cancellation, watched change without refetch, pending preference.
- Plain Compose tests: summary text, watched mark semantics, placeholder semantics, switch, film and header callbacks, inline error retry.
- Paparazzi light/dark: partial collection, pending hidden, loading, error, Spanish large font.
- App journey: Watched → director heading → Director collection → unwatched film detail → Back → Back.

## Risks / Trade-offs

- TMDB job strings are English regardless of request language; the filter matches `Director` exactly, consistent with F-29 data.
- A director with a very large filmography renders a long grid; lazy grid with stable keys keeps it smooth.

## Migration Plan

No schema change. Rollback removes the route and restores F-31's link to the person profile.
