## Purpose

Let people browse their watched movies as a collection, in a simple or visual view, grouped by date watched, director, genre or decade.

## ADDED Requirements

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
The Watched screen SHALL offer Group by chips for Date watched, Director, Genre and Decade, and SHALL persist the selection on the device. The default SHALL be Date watched, preserving the current monthly grouping. Each group SHALL show a heading with its label and movie count.

- Date watched SHALL group by watched month, newest first.
- Director SHALL group by director, ordered by movie count descending then name. A co-directed movie SHALL appear under each director. Movies whose directors are unresolved or empty SHALL appear in a final Unknown director group.
- Genre SHALL group by each of the movie's genres, ordered by count descending then name. Movies without genres SHALL appear in a final No genre group.
- Decade SHALL group by release decade, newest first. Movies without a release date SHALL appear in a final Undated group.
- Within a Date watched group, movies SHALL keep the current order, most recently watched first. Within other groups, movies SHALL be ordered by release date ascending, undated last, then by title.

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
In Director grouping, a resolved director heading SHALL be an accessible button that opens the existing person profile for that director. The Unknown director heading SHALL not be actionable.

#### Scenario: Open director profile
- **WHEN** the user taps the Martin Scorsese heading
- **THEN** the person profile for that TMDB person opens, and Back returns to the Watched screen with mode, grouping and scroll position preserved

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
