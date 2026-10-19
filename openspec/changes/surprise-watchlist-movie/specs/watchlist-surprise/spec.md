## Purpose

Help people choose a movie from their saved Watchlist by offering a random, read-only suggestion and a direct path to its existing detail.

## ADDED Requirements

### Requirement: Suggest a saved movie

The Watchlist SHALL offer Surprise me / Sorpréndeme when unfiltered content contains movies. Activating it SHALL show a dismissible suggestion panel with one randomly selected current Watchlist entry, its available poster/title/metadata, and View details / Ver ficha. Selection SHALL use loaded local data and SHALL NOT change saved movie flags or schedule sync.

#### Scenario: Choose from a populated Watchlist
- **WHEN** the user activates Surprise me on an unfiltered populated Watchlist
- **THEN** the panel shows a movie belonging to that Watchlist
- **AND** movie membership and watched status remain unchanged

#### Scenario: No suitable screen content
- **WHEN** the screen is loading, empty, failed, or displaying a search query
- **THEN** Surprise me is absent and no new suggestion can be selected

#### Scenario: Offline or missing poster
- **WHEN** loaded Watchlist movies are available without a network connection or poster
- **THEN** selection still works and existing image placeholder behavior is retained

### Requirement: Choose another suggestion

The panel SHALL offer Pick another / Elegir otra only when at least two entries exist. Activating it SHALL select a different movie from the immediately previous suggestion and animate the change without a new loading request.

#### Scenario: Two saved movies
- **WHEN** the panel shows one of two saved movies and the user chooses another
- **THEN** the panel shows the other movie

#### Scenario: Only one saved movie
- **WHEN** the Watchlist contains one movie
- **THEN** the panel shows that movie and omits Pick another

### Requirement: Dismiss and open the selected detail

The panel SHALL support normal dismissal by Back, outside tap and sheet gesture. View details SHALL close the panel and open the existing detail for the selected movie. Returning SHALL show the Watchlist without reopening the panel.

#### Scenario: Open detail and return
- **WHEN** the user opens the suggested movie's detail and navigates Back
- **THEN** the selected movie's correct detail has been opened and the Watchlist is visible without its suggestion panel

#### Scenario: Dismiss without choosing
- **WHEN** the user dismisses the suggestion panel
- **THEN** the underlying Watchlist remains unchanged

### Requirement: Keep suggestions consistent with screen data

The panel SHALL close if its selected movie disappears, data loading fails, or the search query changes. It SHALL refresh displayed metadata for a still-present selection when the local list updates. Selection SHALL be synchronous and SHALL NOT start additional collectors. It SHALL survive recomposition and ordinary configuration changes through the existing screen owner; process-death restoration is not required.

#### Scenario: Selected movie removed by a list update
- **WHEN** a local list update no longer contains the selected movie
- **THEN** the suggestion panel closes

#### Scenario: Data failure or new search
- **WHEN** the loaded list reports an error or the user enters a search query
- **THEN** the suggestion panel closes and the existing screen error/search behavior applies

#### Scenario: Rapid selection requests
- **WHEN** multiple choose-another actions are processed
- **THEN** each selection uses the latest selection and list without persistence writes or new collectors
