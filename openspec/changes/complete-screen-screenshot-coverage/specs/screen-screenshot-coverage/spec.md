## ADDED Requirements

### Requirement: Screen visual coverage
The test suite SHALL capture every plain app screen in light and dark themes with deterministic state. Content, loading, empty/no-results and error SHALL be covered where supported. Profile and settings SHALL cover guest and signed-in variants.

#### Scenario: Screen regression
- **WHEN** a covered screen changes its layout or themed appearance
- **THEN** independent snapshot verification compares its render with its reviewed baseline

### Requirement: Deterministic reviewed baselines
Snapshots SHALL avoid live HTTP/image requests and uncontrolled locale/time. New or changed baselines SHALL be recorded separately, visually inspected and independently verified. Existing mismatches SHALL be diagnosed without increasing tolerance to hide regressions.

#### Scenario: Legacy mismatch
- **WHEN** an existing baseline fails verification
- **THEN** its source, renderer and diff are inspected before any baseline update

### Requirement: Complete CI verification
CI SHALL explicitly verify all snapshot-enabled modules and preserve failure images/reports in a uniquely named artifact. A mismatch SHALL fail verification.

#### Scenario: Feature screenshot fails
- **WHEN** any feature or design-system snapshot differs from its baseline
- **THEN** the screenshot CI entry fails and uploads available diagnostics
