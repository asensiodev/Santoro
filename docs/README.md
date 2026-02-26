# Documentation

> **License:** This documentation system (PRD, PRP template, guides, and copilot-instructions) is © 2026 [Ángel Asensio](https://github.com/asensiodev) and licensed under [CC BY 4.0](./LICENSE).

This folder contains all product and planning documentation for the project.

## Structure

```
docs/
├── prd/
│   └── PRD.md                # product-level source of truth (own projects)
├── briefs/
│   ├── FB-TEMPLATE.md        # feature brief template (corporate / sprint-based)
│   └── FB-XXX-<slug>.md      # one per feature brief
├── plan/
│   ├── PRP-TEMPLATE.md       # implementation plan template
│   └── PRP-XXX-<slug>.md     # one per implementation plan
└── guides/
    └── GUIDE-<topic>.md      # manual setup steps, external service config
```

---

## Workflows

### A — Own project (full PRD)

Use when you own the product and control the full roadmap.

```
PRD.md  →  PRP-<feature>.md  →  execute phase by phase  →  mark checkboxes
```

1. Write / review `PRD.md`.
2. Create a PRP from the template for each feature or initiative.
3. If the PRP has a **Prerequisites** section referencing a guide in `docs/guides/` — read the guide first.
4. Work phase by phase, task by task — mark each checkbox on completion.
5. If anything is ambiguous or doesn't fit the plan → **stop and ask**, never decide unilaterally.
6. Any AI session can open the PRP, read the checkboxes, and resume exactly where it left off.

### B — Corporate / sprint-based (Feature Brief)

Use when you join an existing project and work task-by-task from a sprint backlog.

```
Sprint ticket  →  FB-<feature>.md  →  PRP-<feature>.md  →  execute  →  mark checkboxes
```

1. Pick a task from the sprint backlog.
2. Create a **Feature Brief** (`FB-XXX-<slug>.md`) from the template — document current state, requested change, constraints, and acceptance criteria.
3. Create a **PRP** referencing the Feature Brief instead of a PRD.
4. Execute phase by phase, same as Workflow A.

> A Feature Brief replaces the PRD when you don't own the product. It captures just enough context for one feature.

---

## Conventions

### PRD (`prd/PRD.md`)
- Single file. No version number in the filename — Git history is the version log.
- All features and gaps use the unified `F-XX` ID sequence.
- Add a `## Changelog` entry at the bottom for every meaningful update.

### Feature Brief (`briefs/FB-XXX-<feature-slug>.md`)
- One file per sprint task, feature, or bug. Filename includes the numeric FB ID (e.g. `FB-001-search-filters.md`).
- Lightweight — one page max. Documents *what* is asked, not *how* to build it.
- References the sprint ticket (Jira, Linear, etc.).
- **Small tasks (< 4h):** Skip the FB and go straight to a PRP. Use §1 (Context & Motivation) for context.

### PRP (`plan/PRP-XXX-<feature-slug>.md`)
- One file per feature / initiative. Filename includes the numeric PRP ID (e.g. `PRP-003-firebase-sync.md`).
- No version number in the filename — Git tracks history.
- The `Version` field inside the document is informational metadata only.
- References a PRD section (`§F-XX`) or a Feature Brief (`FB-XXX`).
- Checkboxes are the source of truth for progress.
- Not all template sections are mandatory — delete sections that don't apply.
- **Create a PRP only when you are ready to plan and execute it.** Future ideas that are out of scope belong as a one-liner in the current PRP's `## Out of Scope / Follow-ups` section — not as a new file.

### PRP Lifecycle
- **🟡 Draft:** Plan written, not yet started.
- **🔵 In Progress:** At least one phase has begun.
- **✅ Done:** All checkboxes in all phases are marked `[x]` (or the Definition of Done in the metadata is met).

### Guides (`guides/GUIDE-<topic>.md`)
- Companion documents for manual setup steps or external service configuration.
- Referenced from PRP Prerequisites sections — not standalone work items.

---

## Document Reference

### When to use what

| Situation | Document | Why |
|-----------|----------|-----|
| Own project, planning the full product | **PRD** | Single source of truth for all features and gaps |
| Own project, implementing a feature | **PRP** (refs PRD §F-XX) | Execution plan with phases, tasks, and checkboxes |
| Corporate project, picking up a sprint task | **FB → PRP** | FB captures context you don't own; PRP plans the work |
| Corporate project, small task (< 4h) | **PRP only** | Use §1 Context & Motivation for the brief context |
| Corporate project, fixing a bug | **FB (Type: Bug) → PRP** | FB documents repro steps + expected behaviour; PRP plans the fix |
| Manual setup or external config needed | **Guide** | Referenced from PRP §0 Prerequisites |


