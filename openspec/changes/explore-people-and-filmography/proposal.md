## Why

Santoro's movie detail presents cast and crew but stops discovery at their names. PRD F-29 will let a user follow an actor or director to their biography and filmography, then open another movie without leaving the app. This is the first bounded OpenSpec product pilot.

## What Changes

- Make existing cast and crew entries open a shared person detail screen.
- Show a portrait, name, biography, available birth/death information, and birthplace.
- Prioritize the main profession: Acting first for actors, Behind the camera first for other departments; keep both non-empty sections.
- Limit each profile section to eight horizontal poster cards with a credit count and See all action for larger sections; open the full lazy filmography without fetching credits again.
- Show movie credits grouped into acting and crew work, with character or job labels, poster, title, and year; every movie opens the existing movie detail.
- Preserve Back navigation and the previous screen's scroll position through movie → person → movie exploration.
- Use the existing design system with English and Spanish UI resources, loading, empty, error, and retry behavior.
- Keep TV, person search, person favourites, external biography sources, and a general app redesign outside this pilot.

## Capabilities

### New Capabilities

- `person-exploration`: Person profiles and movie filmographies linked from existing cast and crew entries, with round-trip movie navigation.

### Modified Capabilities

None. This brownfield pilot has no existing OpenSpec capability to modify; the new delta documents only the added exploration behavior.

## Impact

- New `feature/person-detail/api` and `impl` modules; app navigation and Gradle module wiring.
- Movie-detail crew UI must preserve the person ID already present in the domain; cast already preserves it.
- Pure person/credit models and repository contract in `core/domain`; TMDB DTOs and mapping at the data boundary, following the existing feature service pattern.
- TMDB read endpoints: person details and movie credits. No Room schema change, Firebase writes, authentication change, or new runtime dependency is proposed.
- Existing FIP-023 external validation and release-pipeline external gates remain outside this pilot. Release preparation is now tracked in the [unified release guide](../../../docs/guides/GUIDE-release-preparation.md); this pilot does not complete those gates.
