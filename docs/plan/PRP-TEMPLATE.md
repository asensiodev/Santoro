# PRP — [Feature Name]

<!-- Template © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

> Copy this file and rename it to `PRP-XXX-<feature-slug>.md` (e.g. `PRP-010-pull-to-refresh.md`).

| Field                  | Value                                     |
|------------------------|-------------------------------------------|
| **PRP ID**             | PRP-XXX                                   |
| **Version**            | 1.0                                       |
| **Status**             | 🟡 Draft · 🔵 In Progress · ✅ Done       |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-XX          |
| **Feature**            | _Name of the feature_                     |
| **Date**               | YYYY-MM-DD                                |
| **Author**             | _@your-handle_                            |
| **Definition of Done** | All checkboxes in all phases marked `[x]` |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

> **Sections follow a fixed order.** Delete sections that don't apply, but do not reorder them.
> A small UI feature may only need §1, §2, §5, §7.
> An infra refactor may skip §4 (User Stories) and §5 (UX) entirely.

---

## 0. Prerequisites (optional)

_PRPs that must be completed first, manual setup steps, or external service config. Link to companion guides in `docs/guides/` if applicable._

---

## 1. Context & Motivation

_Why are we building this? What problem does it solve?_

---

## 2. Goals

- [ ] Goal 1
- [ ] Goal 2

## 3. Non-Goals

- _What this plan explicitly does NOT cover._

---

## 4. User Stories

| ID    | As a…  | I want to…  | So that…  | Acceptance Criteria |
|-------|--------|-------------|-----------|---------------------|
| US-01 |        |             |           |                     |

---

## 5. UX / Flows

### Screen: [ScreenName]

| State   | What the user sees           | User actions available       |
|---------|------------------------------|------------------------------|
| Loading | Shimmer / progress indicator | —                            |
| Content | _Describe main content_      | Action A → navigates to X    |
| Empty   | Empty-state illustration     | Action B → triggers reload   |
| Error   | Error message + Retry button | Retry → re-fetches data      |

_One table per screen affected. Delete this section for non-UI work._

---

## 6. Architecture (optional)

_High-level flow diagram showing how layers connect for this feature. ASCII or Mermaid._

```
Presentation (ViewModel)
       │
       ▼
Domain (Use Case)
       │
       ▼
Data (Repository → DataSource)
```

---

## 7. Data Model (optional)

_New or modified entities: Room tables, Firestore documents, API DTOs, domain models._

---

## 8. Modules Affected

- `feature/<name>/impl`
- `core/<name>`

_Include new modules to create and existing modules to modify. Mention DI / nav wiring changes if relevant._

---

## 9. Phases & Tasks

> Each phase = a logical delivery unit. Complete all tasks in a phase before moving to the next.

### Phase 1 — [Name]

- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Phase 2 — [Name]

- [ ] Task 1
- [ ] Task 2

### Phase N — Tests

- [ ] Unit: ViewModel
- [ ] Unit: Use Cases
- [ ] Unit: Mappers / Repository

---

## 10. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 |          |            |

---

## 11. Out of Scope / Follow-ups

- _Item 1_

---

## 12. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | YYYY-MM-DD | Initial draft |
