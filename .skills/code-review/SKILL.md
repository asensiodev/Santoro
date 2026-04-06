---
name: Code Review
description: Performs a strict code review against Santoro's clean architecture, MVI, and coding standards.
---

## Context
When conducting a code review, always strictly enforce the guidelines defined in `AGENTS.md` and `.github/copilot-instructions.md`.

## Coding Standards to Check
1. **No comments** in code — implementation AND tests. Zero exceptions.
2. **No hardcoded strings.** Use `stringResource(R.string.x)` from `:core:string-resources`.
3. **No raw `.dp`/`.sp`.** Use design tokens from `:core:design-system`.
4. **Immutability:** Always use `val`. No `!!`.
5. **Composables:** Optional params must start with `modifier: Modifier = Modifier`. No `LaunchedEffect(Unit)` for ViewModel init (use `LaunchedEffect(viewModel)`). No business logic in Composables. Previews should use `@PreviewLightDark`.
6. **State & MVI:** No `mutableStateOf` in ViewModel (use `MutableStateFlow`). Use `collectAsStateWithLifecycle()` in Compose.
7. **Coroutines:** No `GlobalScope` or `Dispatchers.IO`/`Main`. Use injected `DispatcherProvider`.
8. **Architecture Focus:** Domain has ZERO Android dependencies. Repository interfaces sit in Domain, implementations in Data. Never leak Room entities or API models.

## Feedback Format
Provide direct, concise feedback indicating the exact file, line, rule violated, and the correct code snippet to fix it. Do not praise or summarize, just output the necessary corrections.
