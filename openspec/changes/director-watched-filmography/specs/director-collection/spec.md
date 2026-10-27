## Purpose

Show a director's feature filmography against the user's watched collection, so users can see what they have watched, what is missing and how complete their collection is.

## ADDED Requirements

### Requirement: Director collection screen
The app SHALL show a Director collection screen with the director's name, a "Watched X of Y" summary with a progress bar and percentage, and the director's feature films in a three-column poster grid ordered by release date ascending, undated last. Watched films SHALL show their poster at full emphasis with a watched mark exposed to accessibility services. Unwatched films SHALL show a dashed placeholder with title and year and SHALL be announced as not watched. Tapping any film SHALL open the existing movie detail; tapping the director name SHALL open the existing person profile.

#### Scenario: Partially watched director
- **WHEN** the user opens a director with 26 feature films of which 9 are watched
- **THEN** the summary reads 9 of 26 with 35% progress, and the grid shows 9 marked and 17 placeholder films in chronological order

#### Scenario: Open an unwatched film
- **WHEN** the user taps an unwatched placeholder
- **THEN** that movie's detail opens, and Back returns to the Director collection with its scroll position

#### Scenario: Watched state changes
- **WHEN** the user marks a film as watched from its detail and returns
- **THEN** the film shows as watched and the summary updates without a new filmography request

### Requirement: Feature films only
The filmography SHALL include movies where the person has the Director job, that are not flagged as video, are not documentaries, and have a release date on or before today. Movies the user has watched with this person as a stored director SHALL always be included and counted, even if they would otherwise be excluded. Movies SHALL be deduplicated by ID.

#### Scenario: Documentary excluded
- **WHEN** a director's TMDB credits include a documentary they directed that the user has not watched
- **THEN** it is not shown and not counted in Y

#### Scenario: Watched documentary kept
- **WHEN** the user has watched a documentary by that director
- **THEN** it is shown as watched and counted in both X and Y

#### Scenario: Unreleased film excluded
- **WHEN** a credit has no release date or a future release date
- **THEN** it is not shown and not counted

### Requirement: Show pending switch
The screen SHALL offer a Show pending switch, on by default, persisted on the device. When off, only watched films SHALL be shown; the summary SHALL remain unchanged.

#### Scenario: Hide pending
- **WHEN** the user turns Show pending off
- **THEN** only watched films remain, the summary still reads X of Y, and the choice persists for other directors and app restarts

### Requirement: Loading, error and empty states
The screen SHALL show the director name and the user's watched films for that director immediately from local data, a loading indicator for the remaining filmography, and a retryable inline error if the filmography request fails. Offline, the screen SHALL still list the watched films with a summary of X watched and no total. Cancellation SHALL not be shown as an error.

#### Scenario: Filmography request fails
- **WHEN** the TMDB credits request fails
- **THEN** watched films remain visible with an inline error and Retry; Retry loads the full filmography

#### Scenario: Leave while loading
- **WHEN** the user navigates Back before the filmography loads
- **THEN** the request is cancelled without an error state or analytics error
