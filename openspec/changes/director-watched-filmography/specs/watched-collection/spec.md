## MODIFIED Requirements

### Requirement: Open a director from the collection
In Director grouping, a resolved director heading SHALL be an accessible button that opens the Director collection screen for that director. The Unknown director heading SHALL not be actionable.

#### Scenario: Open director collection
- **WHEN** the user taps the Martin Scorsese heading
- **THEN** the Director collection screen for that TMDB person opens, and Back returns to the Watched screen with mode, grouping and scroll position preserved

#### Scenario: Open director profile
- **WHEN** the user taps the director name on the Director collection screen opened from the Martin Scorsese heading
- **THEN** the person profile for that TMDB person opens, and Back returns to the Director collection
