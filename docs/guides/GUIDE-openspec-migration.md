# Guide: Gradual Migration From FIP And FB To OpenSpec

| Field | Value |
|---|---|
| **Date** | 2026-10-04 |
| **Status** | Pilot implemented; final validation, manual review and archive pending |
| **Target** | OpenSpec v1.12.0 with Codex and OpenCode |

## Pilot State — 2026-10-04

- Initialized using pinned `@fission-ai/openspec@1.12.0` with `--tools codex,opencode --profile core`.
- The global CLI is pinned to `@fission-ai/openspec@1.12.0`. `openspec --version` verifies the installed version. A pinned `npm exec --package @fission-ai/openspec@1.12.0 -- openspec <command>` remains an alternative on another machine.
- Reusable skills now live in `~/.agents/skills/openspec-*`, shared by Codex and OpenCode. OpenCode commands live in `~/.config/opencode/commands/opsx-*.md`. Repository-local copies were removed after checksum verification; shared invocation hints name both tools. Invoke `$openspec-propose` / `$openspec-apply-change` in Codex, or `/opsx-propose` / `/opsx-apply` / `/opsx-archive` in OpenCode.
- First product pilot: [explore-people-and-filmography](../../openspec/changes/explore-people-and-filmography/proposal.md), PRD F-29. Proposal, delta spec, design, tasks and [implementation evidence](../../openspec/changes/explore-people-and-filmography/implementation.md) are available. Core data, UI and navigation tasks have passed focused checks; outstanding gates remain unchecked.
- FIP-023 external/manual validation is still pending and FIP-024 is still Draft. Phase 0 is not complete: the read-only person pilot can be planned independently, while existing account work retains its original validation and accepted limitations.
- The PRD header is synchronized and F-25 is reconciled with FIP-021. Canonical legacy status inventory and the three-change adoption evaluation remain pending; this is a pilot, not a completed migration.
- Keep `openspec/specs/` empty until validated pilot behavior is synced/archived. Existing FIPs stay in place.

## Global Installation Verification — 2026-10-04

- Global `openspec --version`: `1.12.0`.
- All six shared skill frontmatters parse and match their directory names; all twelve installed files match the reviewed staging checksums.
- Codex app-server `skills/list` reports six enabled OpenSpec skills with `scope: user`, all from `~/.agents/skills/`.
- OpenCode `debug skill --pure` discovers those same six skills, and `debug config --pure` resolves all six `opsx-*` commands.
- Removed only the nineteen verified local adapter files, including the obsolete `.openspec-target` marker, and empty adapter directories. Existing project specs, change artifacts, TDD rules and unrelated user files are retained.
- `openspec validate explore-people-and-filmography --strict` and `git diff --check` pass. No Android code or tests changed during this tooling relocation.

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
- Preserve the accepted unsupported direct-account-replacement limitation recorded in `docs/FIXES-2026-09-06.md`; reopen session authority before introducing direct account switching.
- Retain the onboarding preference-ordering regression coverage and complete the remaining manual product checks.
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
openspec --version
```

Santoro uses user-level tooling because its owner works alone across projects. Keep reusable skills and commands global, while `openspec/config.yaml`, specs, changes, `AGENTS.md` and testing rules remain in the repository.

```text
~/.agents/skills/openspec-*/SKILL.md
~/.config/opencode/commands/opsx-*.md
```

Codex and OpenCode both discover the shared agent-compatible skill folder. OpenCode commands use its native global folder. Sources: [Codex skill locations](https://learn.chatgpt.com/docs/build-skills), [OpenCode skill locations](https://opencode.ai/docs/skills/) and [OpenCode command locations](https://opencode.ai/docs/commands/).

On another machine, generate the pinned adapters in a temporary workspace with `openspec init --tools codex,opencode --profile core`, inspect them, and install one shared skill set plus the OpenCode commands in the locations above. Preserve both Codex `$openspec-*` and OpenCode `/opsx-*` invocation hints when sharing the skill set. Verify destination files before removing temporary/local copies, and review conflicts instead of replacing unrelated global skills.

For a new project, initialize only project artifacts:

```bash
openspec init --tools none
```

Santoro already has its configuration and change artifacts; initialization is not needed again. A fresh clone relies on the owner installing global tooling separately.

The installed core workflow provides `/opsx-propose`, `/opsx-explore`, `/opsx-apply`, `/opsx-update`, `/opsx-sync` and `/opsx-archive`. Additional workflows require deliberate generation and review; `/opsx-verify` is not part of the current installation.

After installation:

- Verify the CLI version, six skill manifests and six command files.
- Check OpenCode discovery with `opencode debug skill` and `opencode debug config`; inspect only the relevant skill/command entries, since resolved configuration can contain private settings.
- Codex detects global skills automatically; use a new turn or restart if its selector has not refreshed.
- Disable telemetry if desired with `openspec config set telemetry.enabled false` or `OPENSPEC_TELEMETRY=0`.
- Run `openspec validate <change> --strict` against the project; avoid invoking propose/apply merely to test discovery because they can create or change artifacts.

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
- Generate/update adapters in a temporary workspace, then review and refresh the global skill/command files. Running `openspec init --tools codex,opencode` or `openspec update` in Santoro can recreate local adapters; keep the current global-only layout.
- Match the global CLI and generated adapter versions, and verify discovery after updates.
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
