# PRP — [Feature Name]

> Copy this file and rename it to `PRP-<feature-slug>.md`.

| Field           | Value                                                        |
|-----------------|--------------------------------------------------------------|
| **PRP ID**      | PRP-XXX                                                      |
| **Version**     | 1.0                                                          |
| **Status**      | 🟡 Draft · 🔵 In Progress · ✅ Done                          |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-XX                             |
| **Feature**     | _Name of the feature_                                        |
| **Date**        | YYYY-MM-DD                                                   |
| **Author**      | @asensio                                                     |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

_Why are we building this? What problem does it solve?_

---

## 2. Goals

- [ ] Goal 1
- [ ] Goal 2

### 2.1 Non-Goals

- _What this plan explicitly does NOT cover._

---

## 3. User Stories

| ID    | As a…  | I want to…  | So that…  | Acceptance Criteria |
|-------|--------|-------------|-----------|---------------------|
| US-01 |        |             |           |                     |

---

## 4. UX / Flows

_Screens, states, and transitions._

```
Screen A
  ├── State: Loading
  ├── State: Content
  │     └── Action → Screen B
  ├── State: Error (retry)
  └── State: Empty
```

---

## 5. Phases & Tasks

> Each phase maps to a logical delivery unit. Complete all tasks in a phase before moving to the next.

### Phase 1 — [Name]

- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

### Phase 2 — [Name]

- [ ] Task 1
- [ ] Task 2

### Phase 3 — Tests

- [ ] Unit: ViewModel
- [ ] Unit: Use Cases
- [ ] Unit: Mappers / Repository
- [ ] Screenshot: new Composables (all states)

---

## 6. Technical Notes

### Modules affected
- `feature/<name>/impl`
- `core/<name>`

### Data / API changes
_New endpoints, DB columns, or domain model changes._

### DI / navigation wiring
_New Hilt modules, bindings, or nav graph changes._

---

## 7. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 |          |            |

---

## 8. Out of Scope / Follow-ups

- _Item 1_

---

## 9. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | YYYY-MM-DD | Initial draft |
