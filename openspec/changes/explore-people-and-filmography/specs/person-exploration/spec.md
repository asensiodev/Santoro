## Purpose

Let users discover movies through the people who made them, with linked biographies and movie filmographies inside Santoro.

## ADDED Requirements

### Requirement: Open a person from movie credits
The app SHALL open a shared person profile for a tapped cast or crew member using their TMDB person identity.

#### Scenario: Actor entry
- **WHEN** a user taps an actor in movie detail
- **THEN** the app opens that actor's profile, regardless of their character or credit identity

#### Scenario: Director or other crew entry
- **WHEN** a user taps a displayed director, writer, cinematographer, or composer
- **THEN** the app opens the corresponding person's profile

### Requirement: Person profile and missing information
The profile SHALL show the person's name, portrait, biography, and available birth date, death date, and birthplace. Long biographies SHALL be expandable and collapsible. Missing biographies SHALL have a localized empty message, missing portraits SHALL have a placeholder, and absent personal facts SHALL be omitted.

#### Scenario: Complete profile
- **WHEN** profile details are available
- **THEN** the person header, available facts, and biography are displayed

#### Scenario: Missing optional details
- **WHEN** the biography, portrait, or personal facts are absent
- **THEN** the profile remains usable with the specified message, placeholder, or omitted fields

#### Scenario: Read a long biography
- **WHEN** the user expands a truncated biography
- **THEN** the full biography becomes readable and can be collapsed again

### Requirement: Movie filmography
The profile SHALL show movie credits in separate acting and crew sections, omitting sections without credits. Each section SHALL group entries by movie identity, preserve distinct character or job labels, and order dated movies by descending release date with movie ID as tie-breaker. Undated movies SHALL follow dated movies. Rows SHALL show available poster, title, and release year. TV credits SHALL be excluded.

#### Scenario: Actor who also directs
- **WHEN** the person has acting and crew credits
- **THEN** both sections appear and show their respective characters and jobs

#### Scenario: Multiple credits on one movie
- **WHEN** a person has multiple roles for the same movie within a section
- **THEN** that section shows one movie row with distinct role labels

#### Scenario: No movie credits
- **WHEN** movie credits are successfully loaded but empty
- **THEN** the profile shows a localized empty filmography message

### Requirement: Round-trip exploration
The app SHALL open the existing movie detail when a filmography row is tapped, allow further person exploration from that movie, and restore the preceding destination and scroll position on Back. Rapid repeated taps on one entry SHALL produce at most one destination while its source is leaving the foreground.

#### Scenario: Follow credits and return
- **WHEN** a user follows movie A → person P → movie B and presses Back twice
- **THEN** person P and then movie A reappear at their previous scroll positions

#### Scenario: Repeated tap
- **WHEN** the same movie or person entry is tapped repeatedly during navigation
- **THEN** only one destination is opened for that transition

### Requirement: Loading and independent failures
The profile SHALL show loading while details are requested and a retry action if details fail. Once details are available, filmography loading, empty, or error states SHALL remain inside the filmography section without removing the profile. Retrying filmography SHALL request credits again. Leaving the profile SHALL prevent obsolete requests from replacing another person's content and SHALL NOT show cancellation as a user error.

#### Scenario: Details failure
- **WHEN** person details fail to load
- **THEN** an error with retry is displayed and Back remains available

#### Scenario: Credits failure after details success
- **WHEN** details load but movie credits fail
- **THEN** the biography stays visible and the filmography section offers retry

#### Scenario: Navigate while loading
- **WHEN** the user leaves a profile before loading completes
- **THEN** subsequent navigation never displays that obsolete result as another person's profile or as a cancellation error

### Requirement: Localization and accessibility
The app SHALL use English and Spanish resources for interface labels and request TMDB content in the current app language. If the requested biography is empty, it SHALL display the localized empty message without automatic translation or a second-language request. Interactive entries SHALL have at least 48dp touch targets and accessible names; the UI SHALL support light and dark themes.

#### Scenario: Spanish profile
- **WHEN** the app language is Spanish and TMDB has no Spanish biography
- **THEN** the interface displays Spanish labels and the Spanish empty biography message, with any available movie credits still accessible

#### Scenario: Accessible entry
- **WHEN** a user explores credits with accessibility services
- **THEN** each interactive person or movie entry exposes its name and click action

### Requirement: Specialty-first filmography previews
The profile SHALL show Acting first when the known department is Acting or absent, and Behind the camera first for other known departments. Empty sections SHALL be omitted. Each section SHALL display its movie count and at most eight horizontal poster cards in the existing release-date order. See all SHALL appear only for sections with more than eight movies.

#### Scenario: Director who also acts
- **WHEN** a person with Directing as their known department has credits in both sections
- **THEN** Behind the camera appears before Acting

#### Scenario: Large filmography
- **WHEN** a section contains nine or more movies
- **THEN** the profile previews the first eight and offers See all

### Requirement: Complete filmography destination
See all SHALL open the selected person's complete acting or crew movie list with the same ordering and movie navigation. It SHALL reuse already loaded profile credits. Back SHALL restore the preceding list and profile scroll positions.

#### Scenario: Browse all and return
- **WHEN** a user opens See all, opens a movie and presses Back twice
- **THEN** the full list and profile return to their previous positions without another credits request

### Requirement: Chronological year headings
The complete acting and crew filmographies SHALL group movies under accessible release-year headings in the existing descending date order. Each year SHALL appear once as a heading and SHALL be omitted from its movie cards. Undated movies SHALL follow dated groups under a localized heading. Horizontal profile preview cards SHALL retain their release year.

#### Scenario: Several movies released in one year
- **WHEN** the complete list contains multiple movies released in the same year
- **THEN** one year heading precedes all of those cards without repeating the year inside them

#### Scenario: Singleton crew years and missing dates
- **WHEN** crew movies have distinct release years and some have no release date
- **THEN** each dated movie appears under its year and undated movies appear under the final localized heading
