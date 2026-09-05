# Guide: Gradual Migration From FIP And FB To OpenSpec

| Field | Value |
|---|---|
| **Date** | 2026-09-06 |
| **Status** | Proposed |
| **Target** | OpenSpec v1.12.0 with OpenCode |

## Goal

Adopt OpenSpec for new non-trivial changes without losing Santoro's product context, Android engineering rules, operational guides, historical plans, or validation discipline.

The migration is incremental. Existing FIPs are not bulk-converted, active plans are not moved mid-execution, and no feature is documented in both a FIP and an OpenSpec change.

## Target Documentation Model

```text
AGENTS.md
    Repository-wide engineering and execution rules

docs/prd/PRD.md
    Product vision, boundaries, roadmap, and feature status

docs/guides/
    Operational setup and release runbooks

docs/plan/
    Existing FIPs retained as historical records

docs/briefs/
    Existing FB template retained during the pilot, then marked legacy

openspec/config.yaml
    Concise project context and artifact-specific planning rules

openspec/specs/
    Current behavior, grown incrementally from archived changes

openspec/changes/<change>/
    proposal.md
    specs/<capability>/spec.md
    design.md
    tasks.md
```

## Artifact Mapping

| Current content | OpenSpec destination |
|---|---|
| FB current state and requested change | `proposal.md` |
| FIP context, motivation, goals, and scope | `proposal.md` |
| User stories and acceptance criteria | Delta specs with requirements and scenarios |
| UX behavior and failure states | Delta specs; implementation detail in `design.md` |
| Architecture, data model, and decisions | `design.md` |
| Data sources and allowed/forbidden side effects | Required `design.md` rules |
| Phases and checkboxes | `tasks.md` |
| Validation commands and manual evidence | Verification tasks and completion evidence |
| Current shipped behavior | `openspec/specs/` after sync/archive |
| Historical implementation record | Archived OpenSpec change or existing legacy FIP |

## Phase 0. Close The Current Work

- Finish FIP-023 under the existing FIP workflow.
- Resolve the release-blocking sync authority issue in `docs/FIXES-2026-09-06.md`.
- Resolve the onboarding preference ordering issue.
- Do not migrate FIP-023 while implementation or validation is active.

## Phase 1. Repair The Documentation Baseline

- Align the PRD header version with its latest version-history entry.
- Reconcile F-25 with the completed FIP-021 status.
- Define one canonical status vocabulary for legacy FIPs.
- Identify active, completed, and draft FIPs.
- Keep existing paths stable so historical links remain valid.
- Do not rewrite old FIPs solely to match current architecture.

Exit criterion: the PRD and active FIPs agree about current delivery status before OpenSpec becomes another source of state.

## Phase 2. Install And Initialize A Pinned Pilot

Prerequisite: Node.js 20.19.0 or newer.

Install the reviewed version rather than an unpinned `latest`:

```bash
npm install -g @fission-ai/openspec@1.12.0
openspec init --tools opencode
```

Select the expanded workflow if `verify` and onboarding commands are desired:

```bash
openspec config profile
openspec update
```

Expected OpenCode integration:

```text
.opencode/skills/openspec-*/SKILL.md
.opencode/commands/opsx-*.md
```

Expected command form in OpenCode:

```text
/opsx-propose
/opsx-apply
/opsx-verify
/opsx-archive
```

After initialization:

- Review every generated file before committing it.
- Disable telemetry if desired with `openspec config set telemetry.enabled false` or `OPENSPEC_TELEMETRY=0`.
- Restart OpenCode only if the generated command discovery instructions request it.
- Confirm `openspec status` and one OpenCode command work before starting the pilot.

## Phase 3. Configure Santoro Rules

Start with the default `spec-driven` schema. Do not create a custom schema until a real limitation is demonstrated.

Keep `openspec/config.yaml` concise and point agents to `AGENTS.md` rather than copying all repository rules. Configure artifact guidance equivalent to:

### Proposal

- Reference the PRD feature or external ticket.
- Describe realistic user impact.
- State scope and non-goals.
- List affected modules.

### Specs

- Use testable requirements and WHEN/THEN scenarios.
- Cover success, failure, cancellation, empty data, concurrency, lifecycle, migration, and failure UX where relevant.
- Specify observable behavior, not implementation classes.

### Design

- Read `AGENTS.md` and affected guides.
- Identify every data source.
- List allowed and forbidden side effects.
- Define authority, cancellation, dispatcher, persistence, migration, rollback, and lifecycle behavior where relevant.
- Record accepted limitations using realistic reachability, likelihood, and user harm.

### Tasks

- Group tasks into logical delivery units.
- Include focused verification with each unit.
- Run affected tests before aggregate gates.
- Mark a task complete only after implementation and validation.
- Preserve manual device, emulator, CI, signing, and release checks where applicable.

OpenSpec validation and `/opsx-verify` complement but do not replace Gradle, instrumentation, CI, or manual release gates.

## Phase 4. Run One Small Real Pilot

- Choose one approved, bounded product change. F-26 or F-27 are suitable candidates.
- Do not use FIP-024 for the first behavioral pilot unless it is intentionally ported before implementation.
- Run `/opsx-explore` if requirements or affected code are unclear.
- Create the change with `/opsx-propose <change-name>`.
- Review `proposal.md`, delta specs, `design.md`, and `tasks.md` before implementation.
- Do not create a parallel FIP.
- Implement with `/opsx-apply`.
- Allow the applying agent to update `tasks.md`; require human review before archive.
- Run repository validation independently of `/opsx-verify`.
- Sync and archive only after code, tests, documentation, and required external checks agree.

## Phase 5. Evaluate The Pilot

Record:

- Planning time compared with a similar FIP.
- Ambiguities found before coding.
- Ease of resuming in a new session.
- Traceability from scenarios to tests.
- Quality of technical design and side-effect boundaries.
- Reliability of OpenCode commands.
- Manual effort needed to keep PRD status synchronized.
- Any information that did not fit proposal, specs, design, or tasks.

Continue the pilot for three to five changes before changing repository-wide policy.

## Phase 6. Adopt For New Work

If the pilot succeeds:

- Update `docs/README.md` to make OpenSpec the workflow for new non-trivial changes.
- Update `AGENTS.md` to require relevant OpenSpec artifacts instead of a new FIP.
- Mark `docs/plan/` and `docs/briefs/` as legacy historical documentation.
- Stop creating new FB and FIP files.
- Keep existing FIPs in place and finish any active one under its original rules.
- Keep the PRD as product and roadmap context.
- Keep guides as operational documentation.
- Let `openspec/specs/` grow only when real changes are archived.

If the pilot fails, remove only the pilot integration after preserving any useful decisions. Existing documentation remains intact.

## Phase 7. Maintain OpenSpec Deliberately

- Pin the CLI version used by the project or installation instructions.
- Review release notes before upgrades.
- Run `openspec update` deliberately because generated OpenCode skills and commands can change.
- Review generated diffs after every update.
- Avoid beta stores or cross-repository planning until the local single-repository workflow is stable.
- Do not treat specs as complete coverage of untouched brownfield code.

## Migration Definition Of Done

- At least three representative changes have completed the propose, apply, verify, sync, and archive loop.
- OpenCode commands are reliable in normal sessions.
- No active change has duplicate FIP and OpenSpec plans.
- `docs/README.md` and `AGENTS.md` describe the final workflow consistently.
- PRD status remains synchronized at archive/release boundaries.
- Existing FIP links remain valid.
- Current specs contain only behavior established through reviewed changes.
- Gradle, instrumentation, CI, manual-device, signing, and release validation remain authoritative.

## Official References

- OpenSpec repository: https://github.com/Fission-AI/OpenSpec
- OPSX workflow: https://github.com/Fission-AI/OpenSpec/blob/main/docs/opsx.md
- Existing project adoption: https://github.com/Fission-AI/OpenSpec/blob/main/docs/existing-projects.md
- Supported tools and OpenCode paths: https://github.com/Fission-AI/OpenSpec/blob/main/docs/supported-tools.md
- Release v1.12.0: https://github.com/Fission-AI/OpenSpec/releases/tag/v1.12.0
