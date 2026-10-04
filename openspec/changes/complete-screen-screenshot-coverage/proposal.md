## Why

CI explicitly verifies only person screenshots. Other features have component baselines but no full-screen snapshots, so changes to screen composition can regress unnoticed. This strengthens existing PRD screen flows and F-24 visual contrast validation without changing product behavior.

## What Changes

- Add deterministic light/dark screenshots at plain screen seams for Login, Search, See all movies, Movie detail, Watchlist, Watched movies, Profile and Settings.
- Cover content and relevant loading, empty/no-results and error states. Extend person screen state coverage where missing.
- Verify every snapshot-enabled module in CI, including design-system components and existing feature snapshots; upload named failure artifacts.
- Diagnose legacy baseline drift before changing any existing golden image; preserve evidence and inspect intentional baseline changes.

## Capabilities

### New Capabilities
- screen-screenshot-coverage: Deterministic full-screen visual regression coverage enforced in CI.

### Modified Capabilities
None.

## Impact

Test sources, checked-in PNG baselines, CI and testing documentation. Existing Paparazzi plugins and plain UI composables are reused. No new fake repositories, runtime dependencies, account behavior, persistence or navigation changes.
