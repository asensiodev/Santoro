## 1. Boundary agreement

- [x] 1.1 Record the user's agreement to ViewModel state/intents, plain Compose callbacks, navigation journey and light/dark visual boundaries in design.md before adding tests; verify the response is recorded.

## 2. Suggestion state, one behavior per red/green cycle

- [x] 2.1 Add a focused one-entry selection test, observe a behavioral assertion failure with compiling API scaffolding, implement minimal selection and verify the same test passes; record both managed commands/results.
- [x] 2.2 Add a focused two-entry choose-another test, observe behavioral red, implement exclusion of the previous choice and verify green, including rapid repeated intents; record evidence.
- [x] 2.3 Add focused tests individually for dismissal and unavailable content/search guards, implement each only after its behavioral red and rerun it to green; verify existing screen behavior remains green.
- [x] 2.4 Add local-stream tests individually for disappearance, metadata refresh and error while suggesting; implement missing reconciliation after behavioral red and verify focused green, with no persistence or sync effects; classify immediately passing metadata coverage as regression.

## 3. User interface, one behavior per red/green cycle

- [x] 3.1 Add a plain Compose test for the localized action on populated unfiltered content, observe behavioral red, implement the button and verify green; add unavailable/search-state coverage as separate cycles.
- [x] 3.2 Add a plain Compose test for the selected movie panel and detail callback, observe behavioral red, implement the sheet and reusable previewable content, then verify green. Separately cover another-choice and dismissal callbacks, and the single-entry action visibility; classify immediately passing checks as regression coverage.
- [x] 3.3 Add an app journey for suggestion to the correct movie detail and Back, observe behavioral red if the new wiring remains unimplemented, then implement and verify green; classify any immediately passing scenario as integration regression coverage.

## 4. Visuals and completion

- [x] 4.1 Record targeted Watchlist content and suggestion baselines in light/dark, inspect every changed capture, and independently verify Paparazzi; validate the choice transition with connected UI tests and retain manual gesture/rotation review as a visible pending gate if not performed.
- [x] 4.2 Run affected unit, Compose and journey tests plus detekt/ktlint; validate OpenSpec with --strict and record commands, outcomes and every changed class in implementation.md.
- [x] 4.3 Run test detekt ktlintCheck koverVerify assembleDebug assembleRelease and root verifyPaparazziDebug before a release-ready claim; retain physical-device, release startup and publication gates explicitly pending unless performed.

## 5. Icon follow-up

- [x] 5.1 Replace the initial Shuffle artwork with the existing Material Casino dice, record and inspect the six affected light/dark captures, and independently verify affected screenshots and static checks. Retain the implementation locally; publish only the documentation requested by the user.

## 6. Compact header follow-up

- [x] 6.1 Observe a failing plain Compose test for hiding the entry action with one movie, implement the visibility change, and verify the same test passes.
- [x] 6.2 Place the text-only tonal action beside the count in a wrapping header, preserve its callback and 48dp target, and verify light/dark, Spanish and large-font presentation plus affected checks. Record evidence without claiming fresh release readiness.

## 7. Reachable bottom action follow-up

- [x] 7.1 Add a plain Compose test that scrolls to the last saved movie and still finds a clickable suggestion action without covering the movie; observe red, pin the compact text-only action at the bottom end with reserved list space, and verify green.
- [x] 7.2 Inspect light/dark and large-font Spanish captures, including the existing bottom navigation in a test-only app-shell composition. Independently verify screenshots, affected tests and static checks, and record outcomes and remaining manual/release gates.

## 8. Floating overlay correction

- [x] 8.1 Observe a failing screen test proving the list viewport extends behind the action, then replace the reserved strip with a transparent overlay and scrollable end clearance; verify overlay placement, final-movie visibility and callback behavior.
- [x] 8.2 Verify final scroll clearance with Spanish 200% text, inspect long-list light/dark navigation-shell captures, independently verify affected screenshots/tests/static checks, and record the results.

## 9. Accent color refinement

- [x] 9.1 Apply the existing theme accent and matching foreground to the floating action without changing size, shape, elevation, placement or scroll behavior; inspect affected light/dark and large-font captures, independently verify module screenshots/static checks and record results.

## 10. Muted dark-theme accent

- [x] 10.1 Use the existing muted blue container/foreground pair on dark surfaces only, preserve light-theme colors and geometry, inspect all six affected dark captures and independently verify module screenshots/static checks.

## 11. Publication preparation

- [x] 11.1 Reproduce the outdated single-entry navigation journey, adjust its fixture to two current saved movies and assert navigation against the displayed selection, verify the journey and affected app static checks, and identify unrelated local files to exclude from the user-requested publication.
