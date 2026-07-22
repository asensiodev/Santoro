# Documentation

> **License:** This documentation system (PRD, FIP template, guides, and copilot-instructions) is © 2026 [Ángel Asensio](https://github.com/asensiodev) and licensed under [CC BY 4.0](./LICENSE).

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
│   ├── FIP-TEMPLATE.md       # feature implementation plan template
│   └── FIP-XXX-<slug>.md     # one per feature implementation plan
└── guides/
    └── GUIDE-<topic>.md      # manual setup steps, external service config
```

---

## Workflows

### A — Own project (full PRD)

Use when you own the product and control the full roadmap.

```
PRD.md  →  FIP-<feature>.md  →  execute phase by phase  →  mark checkboxes
```

1. Write / review `PRD.md`.
2. Create a FIP from the template for each feature or initiative.
3. If the FIP has a **Prerequisites** section referencing a guide in `docs/guides/` — read the guide first.
4. Work phase by phase, task by task — mark each checkbox on completion.
5. If anything is ambiguous or doesn't fit the plan → **stop and ask**, never decide unilaterally.
6. Any AI session can open the FIP, read the checkboxes, and resume exactly where it left off.

### B — Corporate / sprint-based (Feature Brief)

Use when you join an existing project and work task-by-task from a sprint backlog.

```
Sprint ticket  →  FB-<feature>.md  →  FIP-<feature>.md  →  execute  →  mark checkboxes
```

1. Pick a task from the sprint backlog.
2. Create a **Feature Brief** (`FB-XXX-<slug>.md`) from the template — document current state, requested change, constraints, and acceptance criteria.
3. Create a **FIP** referencing the Feature Brief instead of a PRD.
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
- **Small tasks (< 4h):** Skip the FB and go straight to a FIP. Use §1 (Context & Motivation) for context.

### FIP — Feature Implementation Plan (`plan/FIP-XXX-<feature-slug>.md`)
- One file per feature / initiative. Filename includes the numeric FIP ID (e.g. `FIP-003-firebase-sync.md`).
- No version number in the filename — Git tracks history.
- The `Version` field inside the document is informational metadata only.
- References a PRD section (`§F-XX`) or a Feature Brief (`FB-XXX`).
- Checkboxes are the source of truth for progress.
- Not all template sections are mandatory — delete sections that don't apply.
- **Create a FIP only when you are ready to plan and execute it.** Future ideas that are out of scope belong as a one-liner in the current FIP's `## Out of Scope / Follow-ups` section — not as a new file.

### FIP Lifecycle
- **🟡 Draft:** Plan written, not yet started.
- **🔵 In Progress:** At least one phase has begun.
- **✅ Done:** All checkboxes in all phases are marked `[x]` (or the Definition of Done in the metadata is met).

### Closing a FIP (mandatory)
When a FIP transitions to **✅ Done**:
1. Update the FIP `Status` field to `✅ Done`.
2. Update the **source document** (PRD or Feature Brief) that the FIP references:
   - **PRD:** Set the feature's `Status` row to `✅ Shipped` and add a link to the FIP.
   - **Feature Brief:** Set the FB's `Status` field to `✅ Done`.
3. This ensures the PRD/FB always reflects the current state of the product without having to inspect every FIP individually.

### Guides (`guides/GUIDE-<topic>.md`)
- Companion documents for manual setup steps or external service configuration.
- Referenced from FIP Prerequisites sections — not standalone work items.

---

## Agent Workflow

This project uses two specialised agents. Both must read `AGENTS.md` before any work.

| Agent | What it does | What it does NOT do |
|---|---|---|
| **planner** | Creates/updates FBs and FIPs in `docs/` | Write production code or tests |
| **executor** | Implements code following a FIP | Create or modify planning documents |

### Planner rules
1. Read `docs/README.md` + the relevant template (`FB-TEMPLATE.md` or `FIP-TEMPLATE.md`) + `AGENTS.md` before writing.
2. For each phase, specify **Data sources** and **Side effects** (✅ Allowed / ❌ Forbidden) when the phase touches data or external services.
3. If required information is missing or ambiguous — **stop and ask**.

### Executor rules
1. Read `AGENTS.md` + the full FIP before writing any code.
2. The FIP is the **single source of truth**. Respect Data sources and Side effects constraints per phase.
3. Do NOT invent service calls, use cases, or data sources not in the plan.
4. If the plan is ambiguous or incomplete — **stop and ask**, do not guess.

---

## Document Reference

### When to use what

| Situation | Document | Why |
|-----------|----------|-----|
| Own project, planning the full product | **PRD** | Single source of truth for all features and gaps |
| Own project, implementing a feature | **FIP** (refs PRD §F-XX) | Execution plan with phases, tasks, and checkboxes |
| Corporate project, picking up a sprint task | **FB → FIP** | FB captures context you don't own; FIP plans the work |
| Corporate project, small task (< 4h) | **FIP only** | Use §1 Context & Motivation for the brief context |
| Corporate project, fixing a bug | **FB (Type: Bug) → FIP** | FB documents repro steps + expected behaviour; FIP plans the fix |
| Manual setup or external config needed | **Guide** | Referenced from FIP §0 Prerequisites |
