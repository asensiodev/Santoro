## Why

People save movies and then struggle to choose one for tonight. Deliver PRD F-30 within F-04 (Watchlist) with a small, playful action that suggests a saved movie using data already available on the screen.

## What Changes

- Add a compact text-only Surprise me / Sorpréndeme action floating over the bottom end of the unfiltered Watchlist when at least two movies exist, above app navigation. Movies scroll behind it; measured scrollable end padding lets the final movie clear the action completely.
- Show a dismissible bottom sheet with the selected movie's poster, title and existing metadata, plus View details / Ver ficha and Pick another / Elegir otra.
- Avoid immediately repeating a suggestion when alternatives exist. With one movie, hide the entry action; an already-open suggestion for that remaining movie omits Pick another.
- Keep suggestions read-only and refresh them against local list updates.
- Use the existing theme accent and its matching foreground for the entry action, without changing its compact size or introducing a new palette.

## Capabilities

### New Capabilities

- `watchlist-surprise`: Choose and preview a random saved movie, choose another and open its existing detail.

### Modified Capabilities

None.

## Impact

Watchlist implementation and tests, the shared Casino dice icon export, English/Spanish string resources, and an app navigation journey. Reuse the existing movie row and detail callback. No API, schema, persistence, cloud, dependency, ranking, duration filter or account changes. Entries are the existing Watchlist entries, including any deliberately retained watched titles; this change does not redefine membership.
