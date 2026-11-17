## MODIFIED Requirements

### Requirement: Group the collection
The Watched screen SHALL offer Group by chips for Recent, A–Z, Director, Genre and Decade, in that order, and SHALL persist the selection on the device. The default SHALL be Recent. A saved Date watched selection from an earlier version SHALL open as Recent. Except in Recent, each group SHALL show a heading with its label and movie count. The app SHALL NOT show the date a movie was marked as watched anywhere on the Watched screen.

- Recent SHALL show every watched movie in a single section without a heading, most recently added first; movies without a stored added time SHALL appear last.
- A–Z SHALL group by the first character of the title, ignoring leading spaces, letter case and accents, under headings A to Z. Titles starting with any other character, including digits, symbols and letters outside A to Z after removing accents, SHALL appear in a final "#" group. Leading articles SHALL NOT be skipped. Within a letter, movies SHALL be ordered by title ignoring case and accents, then by release date.
- Director SHALL group by director, ordered by movie count descending then name. A co-directed movie SHALL appear under each director. Movies whose directors are unresolved or empty SHALL appear in a final Unknown director group.
- Genre SHALL group by each of the movie's genres, ordered by count descending then name. Movies without genres SHALL appear in a final No genre group.
- Decade SHALL group by release decade, newest first. Movies without a release date SHALL appear in a final Undated group.
- Within Director, Genre and Decade groups, movies SHALL be ordered by release date ascending, undated last, then by title.
- Recent and A–Z SHALL work in Shelf and List.

#### Scenario: Default grouping
- **WHEN** a user with no saved grouping opens a populated Watched screen
- **THEN** Recent is selected and the movies appear in one section without headings, the most recently added first

#### Scenario: Saved Date watched preference
- **WHEN** a user whose saved grouping is Date watched updates the app and opens the Watched screen
- **THEN** Recent is selected, no month headings are shown, and Recent stays selected after reopening the app

#### Scenario: Group alphabetically
- **WHEN** the user selects A–Z with watched movies "the Godfather", "Alien", "Érase una vez en América" and "12 Angry Men"
- **THEN** "Alien" appears under A, "Érase una vez en América" under E, "the Godfather" under T and "12 Angry Men" under a final "#" heading, each heading with its count

#### Scenario: Leading article
- **WHEN** the user selects A–Z with the watched movie "The Matrix"
- **THEN** it appears under T, not under M

#### Scenario: Group by director
- **WHEN** the user selects Director and has watched three Scorsese films and one Nolan film
- **THEN** a Martin Scorsese heading with count 3 appears before a Christopher Nolan heading with count 1

#### Scenario: Co-directed movie
- **WHEN** a watched movie has two directors and Director grouping is selected
- **THEN** the movie appears under both directors' headings

#### Scenario: Unresolved director
- **WHEN** a watched movie's directors have not been resolved yet
- **THEN** it appears under Unknown director, and moves to its director's group once resolved without user action

#### Scenario: Grouping persists
- **WHEN** the user selects Decade and later reopens the app
- **THEN** the Watched screen is grouped by decade

## ADDED Requirements

### Requirement: Collapse director groups
In Director grouping, each group heading, including Unknown director, SHALL have an expand/collapse control separate from the heading's director button. The control SHALL be an accessible button with a touch target of at least 48dp that announces the director and whether the group is expanded or collapsed. A collapsed group SHALL show only its heading with the movie count. Groups SHALL start collapsed when the app starts, and a director group that appears later, for example after marking a movie as watched, SHALL start collapsed. Expanded and collapsed groups SHALL be kept across configuration changes, when switching to another tab and back, and while the user visits the Director collection or a movie detail and returns, and SHALL NOT be saved across app restarts. While a search query is entered, every group with matches SHALL be shown expanded and the expand/collapse control SHALL be hidden; clearing or closing the search SHALL restore the groups the user had expanded. Tapping the director's name SHALL keep opening the Director collection, and tapping the expand/collapse control SHALL NOT open it.

#### Scenario: Open the Director grouping
- **WHEN** the user selects Director with movies by Scorsese, Nolan and an unresolved director
- **THEN** the Martin Scorsese, Christopher Nolan and Unknown director headings are shown with their counts and no movies

#### Scenario: Expand a director
- **WHEN** the user taps the expand control on the Christopher Nolan heading
- **THEN** Nolan's movies are shown below the heading, the control announces that the group is expanded, and the Director collection does not open

#### Scenario: Open the collection from a collapsed heading
- **WHEN** the user taps the Christopher Nolan name on a collapsed heading
- **THEN** the Director collection for Nolan opens, and Back returns with Nolan still collapsed

#### Scenario: Return from a movie
- **WHEN** the user has expanded Nolan, opens one of his movies and goes back
- **THEN** Nolan is still expanded and the scroll position is kept

#### Scenario: Search expands matches
- **WHEN** all directors are collapsed and the user searches "dune"
- **THEN** each director with a matching movie is shown expanded with its matches and no expand/collapse control, and closing the search shows all directors collapsed again

#### Scenario: Reopen the app
- **WHEN** the user expanded some directors and later reopens the app
- **THEN** the Director grouping is still selected and every director is collapsed

#### Scenario: Other groupings
- **WHEN** the user selects Genre, Decade, A–Z or Recent
- **THEN** no expand/collapse control is shown and every group shows its movies
