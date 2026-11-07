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

A pure function `directedFeatureFilms(credits, watchedByDirector, today)`: crew credits with role `Director`, not `isVideo`, without genre 99 (Documentary), release date ≤ today; union with the user's watched movies having this person among stored directors; dedupe by ID. Short films are handled separately (see Short films). The follow-up scope option will replace this function's fixed policy with a user-selected scope.

### Short films (revision 2026-11-05)

The manual demo showed Christopher Nolan's three early shorts counted as unwatched feature films ("4 of 16" instead of 13 features), and early shorts are common among directors. Shorts are therefore split out instead of removed, because some users want to see them:

- A film is a short when its known runtime is 1 to 40 minutes (the Academy definition). An unknown runtime (missing, zero or a failed request) keeps the film among feature films, so a network error never hides a film.
- Runtimes of watched films come from the stored `Movie.runtime`; the others come from a new `MovieRuntimeRepository` in `core/domain`, implemented in person-detail impl next to the person TMDB service, calling `movie/{id}` with up to 6 requests in parallel and caching results in memory for the process lifetime. Failed lookups are not cached.
- The ViewModel requests runtimes only after the credits succeed, inside the same replaceable filmography job, and publishes the complete filmography once both are resolved, so tiles never jump between sections. Until then the local watched films show (watched shorts already in the Shorts section when their stored runtime says so).
- The main summary counts feature films only. A Shorts section below the feature grid shows a heading with "X of Y watched" and the same tiles; Show pending applies to it, and the section is hidden when it has no visible films.
- Cost: one small request per unwatched candidate film the first time a director is opened in a session (about 12 for Christopher Nolan); accepted for a correct count.

### State and lifecycle

The ViewModel combines the local watched flow (filtered by director ID) with a one-shot filmography request owned by a cancellable job; retry replaces the job (latest wins). Watched state changes recompute marks and the summary without refetching. `CancellationException` propagates and never becomes an error. Show pending is persisted through the existing `UserPreferencesRepository` (secure key-value store, not cleared on sign-out), following F-31's view mode and grouping preferences; a read failure falls back to on, a write failure keeps the in-memory choice for the session.

### Data sources and side effects

- Allowed: TMDB `person/{id}/movie_credits`, `person/{id}` for the header photo and `movie/{id}` for runtimes, Room reads, `UserPreferencesRepository` preference, navigation to movie detail and person profile.
- Forbidden: writes to movies, list flags or Firestore; persisting filmographies.

### Failure UX

Local watched films render first, ordered chronologically from their stored release dates. Request failure shows an inline error with Retry above the grid; offline shows "X watched" without a total.

### Presentation (revision 2026-11-01)

- Unwatched films show their TMDB poster dimmed (reduced alpha and desaturated) without a watched mark, instead of dashed placeholders: the poster stays recognizable while the mark and emphasis make the gap clear. They are announced as not watched.
- Revision 2026-11-05 (status marks): the screen reuses the app's existing library status language instead of its own check badge. `MovieStatusRibbon` (corner ribbon: check on `secondaryContainer` for watched, bookmark on `tertiaryContainer` for watchlist) moves from search-movies to `core/design-system` with a design-system status enum, so Search and the Director collection render it identically. Watched films show the watched ribbon; unwatched films already in the user's Watchlist show the watchlist ribbon, so a pending film the user planned to watch is told apart from one missing entirely; watched wins over watchlist. The Watchlist membership comes from the local watchlist flow and updates live. Dimming is softened (about 60% opacity and partial desaturation instead of 45% and full grayscale) so pending posters stay inviting. Accessibility: the card state reads Watched or Not watched, and an unwatched film in the Watchlist adds "In Watchlist".
- Poster tiles reuse F-31's pattern: the placeholder with the title is drawn under the image, so a missing or failed poster still shows the title, dimmed for unwatched films.
- The list keeps bottom content padding above the bottom bar, as in F-31.
- Revision 2026-11-06 (list layout): the screen is a single-column list instead of a three-column poster grid. Users open a director they already know to see which films they are missing and when they came out; a poster grid hides titles (stylized posters, shorts without recognizable art) and years, and shows about six films per screen. Each row follows F-31's `WatchedMovieRow` (48dp 2:3 thumbnail, title, year) so Watched and Director collection read alike. Pending rows dim and desaturate the thumbnail and use a secondary title color; the title stays readable. The corner ribbon does not read on a 48dp thumbnail. A trailing filled check badge was tried and rejected: it looked like a checkbox the user could tap and outweighed the title. Status is instead a text `MovieStatusLabel` after the year ("2000 · ✓ Watched", "2010 · 🔖 In Watchlist"); right-aligned labels were tried and rejected because "Watched" and "In Watchlist" differ in width and the column looked ragged. The year separator is hidden from accessibility services. It comes from `core/design-system`, sharing the ribbon's icons and texts with an accent tint (secondary for watched, tertiary for watchlist). Pending films show only the year; the text label keeps status perceivable without relying on dimming alone. The summary keeps its original form (progress text, percentage at `labelLarge` and the thin Material linear indicator drawn as one continuous rounded bar, without the default gap between progress and track or the stop dot; a card with a headline percentage and a thicker bar was tried and rejected as too large) and adds a small line with the films left ("9 films to go") or "You've seen all their feature films" at 100% (shorts are counted apart, so the message names feature films). The remaining count is `total - watched`, never negative, and absent while the total is unknown. No layout toggle: one layout for a management screen; reusing Watched's Shelf/List preference is a possible follow-up if users ask for posters.
- Header (revision 2026-11-05): the top bar shows the director's TMDB profile photo in a small circle next to the name, so the screen reads as the director's page and links visually to the person profile. The photo comes from the existing `PersonRepository.getPerson(id)` in a separate request owned by the ViewModel, independent of the filmography: until it loads, or if it fails or the person has no photo, only the name is shown, with no error and no retry. The Director grouping heading keeps opening this screen; the name and photo open the person profile.
- Accepted limitation: a watched movie whose directors are still unresolved (backfill pending, or opened only offline) does not appear in the director's collection until resolved; the TMDB filmography still lists it as unwatched until then.

## Test Boundaries

Confirmed by the user on 2026-11-01 ("si, empieza con F-32"), together with the plan revision against the implemented F-31 (dimmed unwatched posters, `UserPreferencesRepository` preference). `organize-watched-collection` is implemented and archived.

- Person credits HTTP integration test for `video` and `genre_ids` mapping; runtime HTTP test for parsing, parallel lookup, in-memory cache and failed lookups left unknown.
- Pure filter tests: director job, video, documentary, unreleased, undated, watched-documentary inclusion, dedupe; short classification at 40 and 41 minutes and unknown runtime.
- ViewModel tests: local-first state, success, failure and retry, cancellation, watched change without refetch, pending preference, header photo loaded or failed without affecting the filmography.
- Plain Compose tests: header photo shown when available, summary text, watched mark semantics, not-watched semantics of dimmed posters, failed-poster title fallback, switch, film and header callbacks, inline error retry.
- Paparazzi light/dark: partial collection, pending hidden, loading, error, Spanish large font.
- App journey: Watched → director heading → Director collection → unwatched film detail → Back → Back.

## Risks / Trade-offs

- TMDB job strings are English regardless of request language; the filter matches `Director` exactly, consistent with F-29 data.
- A director with a very large filmography renders a long list; a lazy list with stable keys keeps it smooth.

## Migration Plan

No schema change. Rollback removes the route and restores F-31's link to the person profile.
