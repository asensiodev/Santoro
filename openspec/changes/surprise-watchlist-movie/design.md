## Context

See proposal.md for motivation. WatchlistMoviesRoute already owns lifecycle-aware state collection and supplies the existing movie-detail callback. WatchlistMoviesScreen is a plain state-driven UI; WatchlistMoviesViewModel loads local movies through one existing latest-query collector. WatchlistMovieItem already supplies the required poster/title/metadata layout. The pinned OpenSpec CLI is 1.12.0.

## Goals / Non-Goals

Keep the feature within the Watchlist presentation boundary and English/Spanish resources, plus the existing design-system icon export pattern for Casino dice. No new dependency, repository, persistent model, migration, random-selection service or API. Existing Watchlist membership remains authoritative, including watched entries deliberately retained there.

## Decisions

- Store the selected MovieUi in immutable WatchlistMoviesUiState. Add select/dismiss intents handled synchronously by the existing ViewModel. Use standard-library randomOrNull after excluding the previous ID when alternatives exist; no injected random wrapper is necessary because one- and two-entry examples are deterministic.
- Show the entry action only for unfiltered Content with at least two movies. Float its text-only elevated tonal button over the bottom end of the list viewport, retaining a 48dp minimum touch target. Use a transparent, inset-free content Scaffold with the button in its bottom-bar slot; feed the Scaffold's measured inner padding into LazyColumn contentPadding, not viewport padding. This lets movies scroll behind the action while providing enough scrollable end clearance for the final movie, including with large text, without measurement state or hardcoded overlay height. Reuse the parent app Scaffold's existing navigation/inset padding; do not duplicate system insets or change app navigation. Keep the existing intent boundary guard for unfiltered populated content, including the short debounce interval after query changes; a retained suggestion may still contain the sole remaining movie.
- Present a Material bottom sheet using an independently previewable plain suggestion content composable. Reuse WatchlistMovieItem and the existing navigation callback. Crossfade between movie identities using the animation target, keeping action callbacks bound to the current state.
- Give the entry action the existing blue accent pair `tertiary` / `onTertiary` on light surfaces and the muted `tertiaryContainer` / `onTertiaryContainer` pair on dark surfaces. Determine the surface brightness from the active Material color scheme rather than the device setting so explicit theme previews and tests agree with the rendered theme. Keep its existing size, shape, 48dp minimum target and 4dp elevation. The primary palette is intentionally monochrome; use the already-defined chromatic accent without changing global theme colors or adding transparency.
- Reconcile a selected movie by ID on local updates, clear it on failure/query changes/dismissal, and dismiss before calling the existing detail callback. A retained ViewModel handles rotation. Do not add process restoration for this temporary choice.
- Data sources: the existing loaded local Watchlist and existing poster image loader only. Allowed side effects: in-memory UI state, normal sheet animation, poster display, existing detail navigation. Forbidden: movie writes, sync scheduling, new movie metadata fetches during selection, account changes.
- Failure UX: reuse existing loading/error/empty states and poster fallback. Random selection adds no asynchronous work, cancellation boundary, retry mechanism or collector.

## Test Boundaries

Confirmed by the user with "dale" on 2026-10-19, before any test edits: ViewModel process/state, plain Compose semantics/callbacks, an app navigation journey, and Paparazzi light/dark presentation. This satisfies docs/guides/GUIDE-testing-and-tdd.md and the tdd skill.

The user confirmed the compact text-only header and single-entry visibility refinement with "dale" on 2026-10-26. Reuse the agreed plain Compose and Paparazzi boundaries; include Spanish and large-font layout review. The decorative sheet heading is unchanged.

Later on 2026-10-26, the user approved replacing the header placement with a fixed bottom-end action for one-handed reach. Reuse those same boundaries to test scrolling/callbacks and inspect a test-only composition with the existing app bottom navigation. The header-placement captures remain historical evidence rather than the current design.

The user then clarified that the bottom action must float over visible scrolling movies, not occupy a reserved strip, and that the final movie must clear it fully. Reuse the agreed plain-screen and screenshot boundaries to check viewport overlap, scroll-end clearance, callbacks and Spanish 200% text. The previous reserved-strip implementation is superseded.

The user subsequently approved improving accent/foreground contrast while retaining size, position, text-only styling and the existing soft shadow. This color-only refinement reuses the agreed screenshot boundary and does not introduce new behavior or claim behavioral TDD.

The user then approved reducing the dark-theme action's prominence with the existing muted blue container and light foreground, while leaving light mode unchanged. Reuse the same visual test boundary and retain all geometry and scrolling contracts.

Use one behavior per red/green slice. A compile or emulator failure is infrastructure evidence, not behavioral red. One/two-entry fixtures avoid nondeterministic expected identities; a mutable local-list stream exercises disappearance, metadata refresh and failure. Existing list behavior remains regression coverage.

## Risks / Trade-offs

- A user may revisit an earlier suggestion after several taps; this is normal random choice. Only consecutive repetition is prevented, avoiding persisted history for negligible harm.
- A user may kill the process while the sheet is open; the temporary choice is lost and can be recreated with one tap. Persisting it adds lifecycle work without protecting saved data.
- Offline selection works from loaded movies, while uncached posters use existing placeholder rendering.
- A sync update can remove the displayed entry; closing the sheet prevents opening a stale suggestion. Retained entries update metadata in place.

## Migration Plan

No migration or remote configuration is required. Rollback removes presentation and resource additions; saved lists remain unaffected. Keep the change unarchived until its required validation and human review are complete.
