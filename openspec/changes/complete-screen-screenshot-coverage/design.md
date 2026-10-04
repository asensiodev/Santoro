## Context

Graph Tier 2 inspection found ten plain screen composables, including person detail and filmography. Person has twelve screen snapshots; other modules have component snapshots but no full-screen classes. All seven feature implementations and the design system already apply the Paparazzi convention. CI currently verifies only person-detail. Source coverage has no recorded gaps for the eight missing screens and GenreChip test; PNG/build exclusions are inspected directly where needed.

## Decisions

Use the user-approved screenshot boundary: plain state-driven screens, fixed content and callbacks, in both themes. Render actual UI inside a full-size themed Material Surface with no ViewModel/Hilt/network setup. Reuse existing immutable state values and null image paths; set deterministic locale and restore it. Capture only applicable states, with guest/account variants for settings/profile. Dialog interactions and navigation remain covered by existing UI/journey tests rather than introducing test-only production APIs.

This adds regression coverage, not retrospective TDD. Any actual production defect must first reproduce through its existing observable boundary. Record new baselines separately, inspect every capture, then verify independently. Diagnose existing drift from actual source, image pixels and renderer configuration; never solve it by raising tolerances or indiscriminately overwriting goldens.

CI runs root verifyPaparazziDebug so every applied plugin is included. Keep this as a separate matrix entry and use a distinct artifact name to avoid collisions with unit-test uploads. Snapshot task failure must fail CI. No runtime/persistence/migration side effects; rollback removes test/CI changes and restores explicitly changed baselines.

## Risks

Legacy component baselines may differ from the current renderer; this is already observed for GenreChip. Investigate before enabling the full gate. Loading animations need a stable initial capture; use the existing Paparazzi snapshot mechanism and verify repeatability. Local macOS verification cannot establish hosted Linux parity; hosted CI remains a separate validation gate.
