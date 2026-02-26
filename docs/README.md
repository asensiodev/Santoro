# Documentation

> **License:** This documentation system (PRD, PRP template, guides, and copilot-instructions) is © 2026 [Ángel Asensio](https://github.com/asensiodev) and licensed under [CC BY 4.0](./LICENSE).

This folder contains all product and planning documentation for the project.

## Structure

```
docs/
├── prd/
│   └── PRD.md              # single source of truth — Git tracks history
├── plan/
│   ├── PRP-TEMPLATE.md
│   └── PRP-XXX-<slug>.md   # one per feature / initiative
└── guides/
    └── GUIDE-<topic>.md    # manual setup steps, external service config
```

---

## Workflow

```
PRD.md  →  PRP-<feature>.md  →  execute phase by phase  →  mark checkboxes
```

1. Write / review `PRD.md`.
2. Create a PRP from the template for each feature or initiative.
3. If the PRP has a **Prerequisites** section referencing a guide in `docs/guides/` — read the guide first.
4. Work phase by phase, task by task — mark each checkbox on completion.
5. If anything is ambiguous or doesn't fit the plan → **stop and ask**, never decide unilaterally.
6. Any AI session can open the PRP, read the checkboxes, and resume exactly where it left off.

---

## Conventions

### PRD (`prd/PRD.md`)
- Single file. No version number in the filename — Git history is the version log.
- All features and gaps use the unified `F-XX` ID sequence.
- Add a `## Changelog` entry at the bottom for every meaningful update.

### PRP (`plan/PRP-XXX-<feature-slug>.md`)
- One file per feature / initiative. Filename includes the numeric PRP ID (e.g. `PRP-003-firebase-sync.md`).
- No version number in the filename — Git tracks history.
- The `Version` field inside the document is informational metadata only.
- References the PRD section it derives from (`§F-XX`).
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

