# Santoro – Documentation

This folder contains all product and planning documentation for the Santoro Android app.

## Structure

```
docs/
├── prd/
│   └── PRD.md          # single source of truth — Git tracks history
└── plan/
    ├── PRP-TEMPLATE.md
    └── PRP-<feature>-v<N>.md
```

---

## Workflow

```
PRD.md  →  PRP-<feature>-v1.0.md  →  execute phase by phase  →  mark checkboxes
```

1. Write / review `PRD.md`.
2. Create a PRP from the template for each feature or initiative.
3. Work phase by phase, task by task — mark each checkbox on completion.
4. If anything is ambiguous or doesn't fit the plan → **stop and ask**, never decide unilaterally.
5. Any Claude session can open the PRP, read the checkboxes, and resume exactly where it left off.

---

## Conventions

### PRD (`prd/PRD.md`)
- Single file. No version number in the filename — Git history is the version log.
- Add a `## Changelog` entry at the bottom for every meaningful update.

### PRP (`plan/PRP-<feature-slug>-v<N>.md`)
- One file per feature / initiative.
- Version bumped (`v2`, `v3`…) only if the scope changes significantly mid-execution.
- References the PRD section it derives from (`§F-XX`).
- Checkboxes are the source of truth for progress.
