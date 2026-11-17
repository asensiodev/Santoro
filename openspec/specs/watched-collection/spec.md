# watched-collection Specification

## Purpose

Let people browse their watched movies as a collection, in a simple or visual view, listed by recently added or alphabetically, or grouped by director, genre or decade.

## Requirements


### Requirement: Choose a view mode
The Watched screen SHALL offer Shelf and List view modes through an accessible two-option toggle in the collection header with 48dp touch targets. Shelf SHALL show the existing poster grid with three fixed columns, each poster exposing the movie title to accessibility services and opening the existing movie detail on tap. List SHALL show rows with a small poster, the title and "year · director", opening the existing movie detail on tap. The selected mode SHALL persist on the device across app restarts. The default SHALL be Shelf, so existing users keep their current screen.

#### Scenario: Existing user upgrades
- **WHEN** a user with no saved mode opens the Watched screen
- **THEN** the poster grid is shown as Shelf, as before the upgrade

#### Scenario: Switch to List
- **WHEN** the user selects List on a populated Watched screen
- **THEN** the same movies appear as rows with year and director under the same group headings
- **AND** tapping a row opens that movie's detail

#### Scenario: Mode persists
- **WHEN** the user selects List, leaves the app and opens it again
- **THEN** the Watched screen opens in List

#### Scenario: Mode survives sign out and sign in
- **WHEN** the user selects List, signs out and signs in again on the same device
- **THEN** the Watched screen opens in List once their movies are restored

#### Scenario: Missing poster
- **WHEN** a movie has no poster path or the image fails to load
- **THEN** the Shelf cell and the List row show the existing placeholder with the movie title readable

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

### Requirement: Open a director from the collection
In Director grouping, a resolved director heading SHALL be an accessible button that opens the Director collection screen for that director. The Unknown director heading SHALL not be actionable.

#### Scenario: Open director collection
- **WHEN** the user taps the Martin Scorsese heading
- **THEN** the Director collection screen for that TMDB person opens, and Back returns to the Watched screen with mode, grouping and scroll position preserved

#### Scenario: Open director profile
- **WHEN** the user taps the director name on the Director collection screen opened from the Martin Scorsese heading
- **THEN** the person profile for that TMDB person opens, and Back returns to the Director collection

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

### Requirement: Collection header
The Watched screen SHALL show a header with the screen title and, when the collection has movies, a summary with the total number of watched movies, a search button and the view mode toggle. The total SHALL count each watched movie once, regardless of how many groups it appears in, and SHALL not change while searching. During Loading, Error and Empty, the header SHALL show only the title.

#### Scenario: Total with multi-membership
- **WHEN** the user has 48 watched movies, one of them co-directed, and Director grouping is selected
- **THEN** the summary reads 48 movies

#### Scenario: Header while loading or empty
- **WHEN** the collection is loading, failed to load or has no watched movies
- **THEN** only the title is shown, without summary, search button or view mode toggle

### Requirement: Search on demand
Search SHALL be opened from the header search button, replacing the title, summary, search button and view mode toggle with a focused search field that has a close action and a clear action. Grouping chips SHALL stay visible and usable while searching. Search SHALL filter movies by title within the current mode and grouping, removing groups with no matches. Closing search, through the close action or system Back, SHALL clear the query and restore the header; while search is open, system Back SHALL close search instead of leaving the screen. The clear action SHALL empty the query and keep search open. Open search and its query SHALL survive configuration changes. If the collection becomes empty while search is open, search SHALL close and the Empty state SHALL be shown.

#### Scenario: Open search
- **WHEN** the user taps the search button on a populated Watched screen
- **THEN** a focused search field replaces the header, the keyboard opens, and the grouping chips remain visible

#### Scenario: Search in Shelf by genre
- **WHEN** the user searches "dune" in Shelf grouped by Genre
- **THEN** only matching posters remain, under their genre headings with updated counts

#### Scenario: Change grouping while searching
- **WHEN** the user has searched "dune" and selects Decade
- **THEN** the matches are regrouped by decade and the query is kept

#### Scenario: No matches
- **WHEN** the search query matches no watched movie
- **THEN** the existing no-results message is shown, search stays open and the grouping chips remain visible

#### Scenario: Close search with Back
- **WHEN** search is open with the query "dune" and the user presses system Back
- **THEN** search closes, the query is cleared, the full collection and header are shown, and the user stays on the Watched screen

#### Scenario: Clear query
- **WHEN** search is open with a query and the user taps the clear action
- **THEN** the query is emptied, the full collection is shown and search stays open

#### Scenario: Collection becomes empty while searching
- **WHEN** search is open and the last watched movie is removed from the collection
- **THEN** search closes and the existing empty state is shown

### Requirement: Screen states and controls
Loading, Empty, No results and Error states SHALL behave as they do today in both modes. Mode and grouping controls SHALL be hidden when the collection is empty, loading or failed to load. Grouping chips SHALL use the same rounded chip style as the Search screen genre chips and mark the selected chip for accessibility services. Resolved director headings SHALL be visually distinct from non-actionable headings so users can tell they open the director.

#### Scenario: Empty collection
- **WHEN** the user has no watched movies
- **THEN** the existing empty state is shown without search, mode or grouping controls

### Requirement: Watched statistics leave the Watched screen
The Watched screen SHALL not show the stats dashboard. Watched statistics SHALL remain available to the app's domain layer so a later dedicated statistics screen can show them without recomputing their rules.

#### Scenario: Populated collection
- **WHEN** a user with watched movies opens the Watched screen
- **THEN** no statistics cards are shown above the collection

### Requirement: Measure collection usage
The app SHALL record an analytics event each time the Watched screen shows populated content, with the active mode and grouping as parameters, and an event when the user changes mode or grouping, with the new value. Events SHALL contain no movie, director or account identifiers.

#### Scenario: Returning user with a saved choice
- **WHEN** a user whose saved mode is List opens the Watched screen
- **THEN** a collection view event with mode List is recorded, even though no mode change happened in this session

#### Scenario: Change mode
- **WHEN** the user switches from Shelf to List
- **THEN** a mode change event with value List is recorded

### Requirement: Store and resolve directors and release dates
The app SHALL store each saved movie's directors (TMDB person ID and name) and release date locally and in Firestore, distinguishing an unresolved value from a resolved empty value (no director, or no release date on TMDB). Saving a movie from detail SHALL store both. Watched movies with unresolved directors or an unresolved release date SHALL be resolved once by a background job that requires network, uses one TMDB request per movie for both values, retries transient failures with backoff, stores resolved empty values when TMDB reports none or the movie no longer exists, never overwrites a value that is already resolved, uploads the resolved movies, and stops when none remain. Opening the detail of a watched movie SHALL store any directors or release date that the loaded detail resolves and the stored movie lacks. A movie restored from Firestore without a release date SHALL be treated as unresolved.

#### Scenario: Existing user upgrades
- **WHEN** a user with watched movies saved before this version opens the app with network
- **THEN** their directors are resolved in the background and the Director grouping fills in without user action

#### Scenario: Offline upgrade
- **WHEN** the app starts without network
- **THEN** the collection works with unresolved movies under Unknown director and Undated, and resolution runs when network returns

#### Scenario: Restore on a new device
- **WHEN** a user signs in on a new device and their watched movies are downloaded with stored directors and release dates
- **THEN** no TMDB request is made for those movies and the Decade grouping and List rows show their years

#### Scenario: Restored movies without release dates
- **WHEN** a user restores watched movies whose Firestore documents have no release date, as uploaded by earlier versions
- **THEN** their release dates are resolved in the background, they move from Undated to their decades without user action, and the resolved dates are uploaded so later restores include them

#### Scenario: Movie without director
- **WHEN** TMDB returns credits without a director for a watched movie
- **THEN** the movie is stored as resolved with no director, appears under Unknown director, and is not requested again

#### Scenario: Movie without release date
- **WHEN** TMDB returns no release date for a watched movie
- **THEN** the movie is stored as resolved without a date, appears under Undated, and is not requested again

#### Scenario: Cancellation
- **WHEN** the backfill is stopped by the system mid-run
- **THEN** already resolved movies stay stored, cancellation is not recorded as a failure, and remaining movies are resolved on the next run
