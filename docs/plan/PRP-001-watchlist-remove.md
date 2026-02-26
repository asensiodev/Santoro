# PRP — Remove from Watchlist

| Field           | Value                                          |
|-----------------|------------------------------------------------|
| **PRP ID**      | PRP-001                                        |
| **Version**     | 1.0                                            |
| **Status**      | ✅ Done                                        |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-04               |
| **Feature**     | Remove a movie from the Watchlist              |
| **Date**        | 2026-02-25                                     |
| **Author**      | @asensiodev                                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The Watchlist screen already renders a `DeleteMovieButton` (trash icon) and the `onRemoveMovie` callback exists all the way up to `WatchlistMoviesRoute` — but it's wired to a `/* TODO */`. No use case, no repository method, and no DAO operation exist for removing a movie from the watchlist.

The fix is purely additive on the data layer and a one-line wire-up on the presentation layer.

---

## 2. Goals

- A user can remove a movie from their Watchlist by **swiping the item** left or right.
- The list updates immediately (Room Flow re-emits).
- No accidental deletions: a confirmation dialog appears before removing.
- The `DeleteMovieButton` (trash icon) is removed — swipe is the sole removal trigger.
- The movie record is **not deleted** from the DB — only `isInWatchlist` is set to `false` (consistent with how `UpdateMovieStateUseCase` works in movie-detail).

---

## 3. Non-Goals

- Undo / Snackbar after removal (future enhancement).
- Removing from Watched list (separate feature).

---

## 4. User Story

| ID    | As a…       | I want to…                              | So that…                              | Acceptance Criteria                                                                 |
|-------|-------------|-----------------------------------------|---------------------------------------|-------------------------------------------------------------------------------------|
| US-01 | app user    | remove a movie from my Watchlist        | my list stays relevant                | Swiping an item reveals a red background with a trash icon. Releasing triggers a confirmation dialog. Confirming removes the movie instantly. Cancelling restores the item. |

---

## 5. UX / Flow

```
WatchlistMoviesScreen
  └── WatchlistMovieItem [swiped left or right]
        ├── Background: red with centered trash icon (revealed during swipe)
        └── On release → ConfirmRemoveDialog (modal)
              ├── Confirm → removeFromWatchlist(movieId) → list refreshes
              └── Cancel  → item animates back, no change
```

**Dialog content:**
- Title: "Remove from Watchlist"
- Body: "«{movie title}» will be removed from your Watchlist."
- Buttons: "Remove" (destructive) · "Cancel"

---

## 8. Modules Affected

- `core/database` — DAO + Repository interface + impl
- `feature/watchlist/impl` — use case, DI module, ViewModel, UiState, Screen

No schema migration needed. `isInWatchlist` column already exists in `MovieEntity`. The DAO update sets it to `0` — the movie row stays in the DB (it may still be `isWatched = true`).

Why a dialog instead of direct delete: accidental taps on a list item's icon are common. A single confirmation step is the minimum safety net without being intrusive. Matches the pattern used in most top-tier apps (Letterboxd, Goodreads).

---

## 9. Phases & Tasks

### Phase 1 — Data layer

- [x] Add `removeFromWatchlist(movieId: Int)` to `MovieDao` (`@Query UPDATE SET isInWatchlist = 0 WHERE id = :movieId`)
- [x] Add `removeFromWatchlist(movieId: Int): Result<Boolean>` to `DatabaseRepository` interface
- [x] Implement it in `RoomDatabaseRepository`

### Phase 2 — Domain layer

- [x] Create `RemoveFromWatchlistUseCase` in `feature/watchlist/impl/domain/usecase/`
- [x] Provide it in `WatchlistMoviesModule`

### Phase 3 — Presentation layer

- [x] Add `movieToRemove: MovieUi?` to `WatchlistMoviesUiState` (drives dialog visibility; needs title for dialog body)
- [x] Add `onRemoveMovieClicked(movie: MovieUi)` to `WatchlistMoviesViewModel` (sets `movieToRemove`)
- [x] Add `onRemoveConfirmed()` to `WatchlistMoviesViewModel` (calls use case, clears `movieToRemove`)
- [x] Add `onRemoveDismissed()` to `WatchlistMoviesViewModel` (clears `movieToRemove`)
- [x] Add `SwipeToRemoveContainer` composable wrapping each `WatchlistMovieItem` — red background + trash icon revealed on swipe (`SwipeToDismissBox` from M3)
- [x] Remove `DeleteMovieButton` and `onRemoveClick` param from `WatchlistMovieItem`
- [x] Add `ConfirmRemoveDialog` composable inside `WatchlistMoviesScreen.kt`
- [x] Wire `onRemoveMovie` in `WatchlistMoviesRoute` to `viewModel::onRemoveMovieClicked`
- [x] Show `ConfirmRemoveDialog` when `uiState.movieToRemove != null`
- [x] Add string resources: `watchlist_remove_dialog_title`, `watchlist_remove_dialog_body`, `watchlist_remove_dialog_confirm`, `watchlist_remove_dialog_cancel`

### Phase 4 — Tests

- [x] Unit: `RemoveFromWatchlistUseCase` — success + error paths
- [x] Unit: `WatchlistMoviesViewModel` — `onRemoveMovieClicked` sets `movieToRemove` · `onRemoveConfirmed` calls use case + clears state · `onRemoveDismissed` clears state
- [x] Unit: `RoomDatabaseRepository` — `removeFromWatchlist` delegates to DAO

---

## 10. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should we also auto-remove from Watchlist when a movie is marked as Watched? | Out of scope for this PRP — separate decision |

---

## 11. Out of Scope / Follow-ups

- Snackbar with "Undo" after removal
- Bulk remove / select mode

---

## 12. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-25 | Initial draft |
