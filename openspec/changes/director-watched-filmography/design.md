## Context

See proposal.md. This change starts after `organize-watched-collection` is implemented: movies carry stored directors and release dates (synced through Firestore and resolved by the backfill), and the Watched screen has a Director grouping whose tonal heading navigates to the person profile. Plan revised on 2026-11-01 against the implemented F-31.

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

A pure function `directedFeatureFilms(credits, watchedByDirector, today)`: crew credits with role `Director`, not `isVideo`, without genre 99 (Documentary), release date ≤ today; union with the user's watched movies having this person among stored directors; dedupe by ID. Short films are not detectable from TMDB credits without one request per movie; a short appears only if TMDB does not flag it as video. This is accepted: shorts are rare in mainstream directors' credits and appear as one extra unwatched film, while per-movie requests would multiply network cost. The follow-up scope option will replace this function's fixed policy with a user-selected scope.

### State and lifecycle

The ViewModel combines the local watched flow (filtered by director ID) with a one-shot filmography request owned by a cancellable job; retry replaces the job (latest wins). Watched state changes recompute marks and the summary without refetching. `CancellationException` propagates and never becomes an error. Show pending is persisted through the existing `UserPreferencesRepository` (secure key-value store, not cleared on sign-out), following F-31's view mode and grouping preferences; a read failure falls back to on, a write failure keeps the in-memory choice for the session.

### Data sources and side effects

- Allowed: TMDB `person/{id}/movie_credits` and `person/{id}` for the header photo (existing), Room reads, `UserPreferencesRepository` preference, navigation to movie detail and person profile.
- Forbidden: writes to movies, list flags or Firestore; persisting filmographies.

### Failure UX

Local watched films render first, ordered chronologically from their stored release dates. Request failure shows an inline error with Retry above the grid; offline shows "X watched" without a total.

### Presentation (revision 2026-11-01)

- Unwatched films show their TMDB poster dimmed (reduced alpha and desaturated) without a watched mark, instead of dashed placeholders: the poster stays recognizable while the mark and emphasis make the gap clear. They are announced as not watched.
- Poster tiles reuse F-31's pattern: the placeholder with the title is drawn under the image, so a missing or failed poster still shows the title, dimmed for unwatched films.
- The grid keeps bottom content padding above the bottom bar, as in F-31.
- Header (revision 2026-11-05): the top bar shows the director's TMDB profile photo in a small circle next to the name, so the screen reads as the director's page and links visually to the person profile. The photo comes from the existing `PersonRepository.getPerson(id)` in a separate request owned by the ViewModel, independent of the filmography: until it loads, or if it fails or the person has no photo, only the name is shown, with no error and no retry. The Director grouping heading keeps opening this screen; the name and photo open the person profile.
- Accepted limitation: a watched movie whose directors are still unresolved (backfill pending, or opened only offline) does not appear in the director's collection until resolved; the TMDB filmography still lists it as unwatched until then.

## Test Boundaries

Confirmed by the user on 2026-11-01 ("si, empieza con F-32"), together with the plan revision against the implemented F-31 (dimmed unwatched posters, `UserPreferencesRepository` preference). `organize-watched-collection` is implemented and archived.

- Person credits HTTP integration test for `video` and `genre_ids` mapping.
- Pure filter tests: director job, video, documentary, unreleased, undated, watched-documentary inclusion, dedupe.
- ViewModel tests: local-first state, success, failure and retry, cancellation, watched change without refetch, pending preference, header photo loaded or failed without affecting the filmography.
- Plain Compose tests: header photo shown when available, summary text, watched mark semantics, not-watched semantics of dimmed posters, failed-poster title fallback, switch, film and header callbacks, inline error retry.
- Paparazzi light/dark: partial collection, pending hidden, loading, error, Spanish large font.
- App journey: Watched → director heading → Director collection → unwatched film detail → Back → Back.

## Risks / Trade-offs

- TMDB job strings are English regardless of request language; the filter matches `Director` exactly, consistent with F-29 data.
- A director with a very large filmography renders a long grid; lazy grid with stable keys keeps it smooth.

## Migration Plan

No schema change. Rollback removes the route and restores F-31's link to the person profile.
