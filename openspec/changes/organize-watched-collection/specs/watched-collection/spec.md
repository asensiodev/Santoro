## Purpose

Let people browse their watched movies as a collection, in a simple or visual view, grouped by date watched, director, genre or decade.

## ADDED Requirements

### Requirement: Choose a view mode
The Watched screen SHALL offer Shelf and List view modes through an accessible two-option toggle with 48dp touch targets. Shelf SHALL show the existing poster grid with three fixed columns, each poster exposing the movie title to accessibility services and opening the existing movie detail on tap. List SHALL show rows with a small poster, the title and "year · director", opening the existing movie detail on tap. The selected mode SHALL persist on the device across app restarts. The default SHALL be Shelf, so existing users keep their current screen.

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

### Requirement: Search, stats and screen states in every mode
Search SHALL filter movies within the current mode and grouping, removing groups with no matches. The stats dashboard SHALL remain available as today. Loading, Empty, No results and Error states SHALL behave as they do today in both modes; mode and grouping controls SHALL be hidden when the collection is empty.

#### Scenario: Search in Shelf by genre
- **WHEN** the user searches "dune" in Shelf grouped by Genre
- **THEN** only matching posters remain, under their genre headings with updated counts

#### Scenario: Empty collection
- **WHEN** the user has no watched movies
- **THEN** the existing empty state is shown without mode or grouping controls

### Requirement: Measure collection usage
The app SHALL record an analytics event each time the Watched screen shows populated content, with the active mode and grouping as parameters, and an event when the user changes mode or grouping, with the new value. Events SHALL contain no movie, director or account identifiers.

#### Scenario: Returning user with a saved choice
- **WHEN** a user whose saved mode is List opens the Watched screen
- **THEN** a collection view event with mode List is recorded, even though no mode change happened in this session

#### Scenario: Change mode
- **WHEN** the user switches from Shelf to List
- **THEN** a mode change event with value List is recorded

### Requirement: Store and resolve directors
The app SHALL store each saved movie's directors (TMDB person ID and name) locally and in Firestore, distinguishing unresolved directors from a resolved empty list. Saving a movie from detail SHALL store its directors. Watched movies with unresolved directors SHALL be resolved once by a background job that requires network, retries transient failures with backoff, stores a resolved empty list when TMDB reports no director or the movie no longer exists, uploads each resolved movie, and stops when none remain. Opening the detail of a watched movie with unresolved directors SHALL store the directors from the loaded detail.

#### Scenario: Existing user upgrades
- **WHEN** a user with watched movies saved before this version opens the app with network
- **THEN** their directors are resolved in the background and the Director grouping fills in without user action

#### Scenario: Offline upgrade
- **WHEN** the app starts without network
- **THEN** the collection works with unresolved movies under Unknown director, and resolution runs when network returns

#### Scenario: Restore on a new device
- **WHEN** a user signs in on a new device and their watched movies are downloaded with stored directors
- **THEN** no director request is made for those movies

#### Scenario: Movie without director
- **WHEN** TMDB returns credits without a director for a watched movie
- **THEN** the movie is stored as resolved with no director, appears under Unknown director, and is not requested again

#### Scenario: Cancellation
- **WHEN** the backfill is stopped by the system mid-run
- **THEN** already resolved movies stay stored, cancellation is not recorded as a failure, and remaining movies are resolved on the next run
