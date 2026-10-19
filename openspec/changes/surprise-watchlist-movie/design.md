## Context

See proposal.md for motivation. WatchlistMoviesRoute already owns lifecycle-aware state collection and supplies the existing movie-detail callback. WatchlistMoviesScreen is a plain state-driven UI; WatchlistMoviesViewModel loads local movies through one existing latest-query collector. WatchlistMovieItem already supplies the required poster/title/metadata layout. The pinned OpenSpec CLI is 1.12.0.

## Goals / Non-Goals

Keep the feature within the Watchlist presentation boundary and English/Spanish resources, plus the existing design-system icon export pattern for Casino dice. No new dependency, repository, persistent model, migration, random-selection service or API. Existing Watchlist membership remains authoritative, including watched entries deliberately retained there.

## Decisions

- Store the selected MovieUi in immutable WatchlistMoviesUiState. Add select/dismiss intents handled synchronously by the existing ViewModel. Use standard-library randomOrNull after excluding the previous ID when alternatives exist; no injected random wrapper is necessary because one- and two-entry examples are deterministic.
- Show the action only for unfiltered Content with movies. This avoids ambiguity between selecting from search matches and the whole collection. Guard the same condition at the intent boundary, including the short debounce interval after query changes.
- Present a Material bottom sheet using an independently previewable plain suggestion content composable. Reuse WatchlistMovieItem and the existing navigation callback. Crossfade between movie identities using the animation target, keeping action callbacks bound to the current state.
- Reconcile a selected movie by ID on local updates, clear it on failure/query changes/dismissal, and dismiss before calling the existing detail callback. A retained ViewModel handles rotation. Do not add process restoration for this temporary choice.
- Data sources: the existing loaded local Watchlist and existing poster image loader only. Allowed side effects: in-memory UI state, normal sheet animation, poster display, existing detail navigation. Forbidden: movie writes, sync scheduling, new movie metadata fetches during selection, account changes.
- Failure UX: reuse existing loading/error/empty states and poster fallback. Random selection adds no asynchronous work, cancellation boundary, retry mechanism or collector.

## Test Boundaries

Confirmed by the user with "dale" on 2026-10-19, before any test edits: ViewModel process/state, plain Compose semantics/callbacks, an app navigation journey, and Paparazzi light/dark presentation. This satisfies docs/guides/GUIDE-testing-and-tdd.md and the tdd skill.

Use one behavior per red/green slice. A compile or emulator failure is infrastructure evidence, not behavioral red. One/two-entry fixtures avoid nondeterministic expected identities; a mutable local-list stream exercises disappearance, metadata refresh and failure. Existing list behavior remains regression coverage.

## Risks / Trade-offs

- A user may revisit an earlier suggestion after several taps; this is normal random choice. Only consecutive repetition is prevented, avoiding persisted history for negligible harm.
- A user may kill the process while the sheet is open; the temporary choice is lost and can be recreated with one tap. Persisting it adds lifecycle work without protecting saved data.
- Offline selection works from loaded movies, while uncached posters use existing placeholder rendering.
- A sync update can remove the displayed entry; closing the sheet prevents opening a stale suggestion. Retained entries update metadata in place.

## Migration Plan

No migration or remote configuration is required. Rollback removes presentation and resource additions; saved lists remain unaffected. Keep the change unarchived until its required validation and human review are complete.
