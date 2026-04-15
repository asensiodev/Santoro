# AGENTS.md — Santoro

Android movie companion app · Kotlin · Jetpack Compose · Clean Architecture + MVI · Multi-module

## Architecture

- **Layers:** Presentation → Domain ← Data. Domain has zero Android dependencies (pure Kotlin/JVM).
- **Module split:** Each `feature/` and `library/` module has an `api/` (routes, public interfaces) and `impl/` (everything else, marked `internal`). Core modules are shared.
- **MVI pattern:** ViewModel exposes `uiState: StateFlow<*UiState>` + `effect: Flow<*Effect>` via a Channel. Screen sends sealed `*Intent` objects through `viewModel.process(intent)`. See `feature/watchlist/impl/src/main/.../presentation/` for reference.
- **Navigation:** Type-safe Compose Navigation. Routes are `@Serializable data object` in `api/` modules. `impl/` modules expose a `NavGraphBuilder.xyzRoute()` extension. All wired in `app/`.
- **DI:** Hilt. Feature use cases provided via `@Module @InstallIn(SingletonComponent::class) internal object` in `impl/di/`. Dispatchers injected via `DispatcherProvider` interface (`core/domain`) → `DefaultDispatcherProvider` (`core/data`).

## Module Conventions

| Plugin alias (in `build.gradle.kts`) | Use for |
|---|---|
| `convention.android.feature` | Feature `impl/` modules (auto-adds Compose, Hilt, design-system, string-resources, domain, test deps) |
| `convention.jvm.library` | Feature `api/` modules and pure-Kotlin libraries |
| `convention.android.library` / `convention.android.library.compose` | Core modules |
| `convention.paparazzi` | Screenshot tests |

Convention plugins live in `build-logic/convention/`. They compose each other — check `AndroidFeatureConventionPlugin.kt` to see what's auto-included before adding duplicate dependencies.

## Coding Rules

- **No comments** in code — implementation AND tests. Zero exceptions.
- **No hardcoded strings.** Use `stringResource(R.string.x)` from `:core:string-resources`. If the key doesn't exist, create it in `core/string-resources/src/main/res/values/strings.xml` (and `values-es/strings.xml` for Spanish).
- **No raw `.dp`/`.sp`.** Use design tokens: `Spacings.spacing16`, `Size.size48`, `Weights.W10`, or `MaterialTheme.typography`. Defined in `core/design-system/src/main/java/.../theme/`.
- **Immutability:** always `val`. No `!!`. Composable optional params start with `modifier: Modifier = Modifier`.
- **Lambdas:** `it` only in short single-line lambdas; explicit names otherwise.
- No `GlobalScope` or `Dispatchers.IO` — use injected `DispatcherProvider`.
- No `mutableStateOf` in ViewModel — use `MutableStateFlow`. Never expose mutables publicly.
- `collectAsStateWithLifecycle()` — never `collectAsState()`.
- `LaunchedEffect(viewModel)` — never `LaunchedEffect(Unit)` for ViewModel init.
- No business logic in Composables — push to ViewModel/UseCase.
- All `when` on sealed classes must be exhaustive (no `else`).
- Repository: Domain defines interface, Data implements. Never leak Room entities or API models outside Data.
- Error propagation: `Result<T>`. Map errors at ViewModel via string resources.

## UI/UX Rules

- **Previews:** `@PreviewLightDark` for every public/internal Composable with mock data.
- **State coverage:** Every screen must handle: Loading, Content, Empty, Error. Never a blank screen.
- **Touch targets:** Minimum 48dp.
- **Lazy layouts:** Stable `key` (never index). Use `contentType` for mixed-type lists.
- **Contrast:** WCAG AA. Use `onPrimaryContainer`/`onSurface` tokens.

## Testing

- **JUnit 5** with `@BeforeEach` setup. Backtick names: `` `GIVEN x WHEN y THEN z` ``.
- **Assertions:** Kluent — `shouldBeEqualTo`, `shouldBeNull`, etc.
- **Mocking:** MockK. Use helpers from `:core:testing`: `relaxedMockk()`, `verifyOnce {}`, `coVerifyOnce {}`, `verifyNever {}`, `coVerifyNever {}`.
- **Coroutines:** `TestDispatcherProvider` for use-case tests; `CoroutineTestExtension` or manual `Dispatchers.setMain`/`resetMain` for ViewModel tests.
- **Flows:** Turbine for Flow assertion.
- **Screenshots:** Paparazzi in feature `impl/` modules.
- **Coverage targets:** ViewModel 90%+ · UseCase 100% · Repository 90%+ · Mapper 100%.
- Run all tests: `./gradlew test`. Run single module: `./gradlew :feature:watchlist:impl:test`.

## Build & Quality

```sh
./gradlew assembleDebug            # debug build
./gradlew test                     # all unit tests
./gradlew detekt                   # static analysis
./gradlew ktlintCheck              # code style
./gradlew ktlintFormat             # auto-fix style
./gradlew koverHtmlReport          # coverage report
```

Pre-commit hook (`hooks/pre-commit`) runs detekt + ktlint automatically on staged `.kt`/`.kts` files. Installed via `copyGitHooks` task (runs on `preBuild`).

## Planned Work — FIP System

Feature work is tracked in `docs/plan/FIP-XXX-*.md`. Before coding a feature:
1. Read the FIP top to bottom. If it references a Brief (`docs/briefs/FB-XXX`) or guide (`docs/guides/`), read those first.
2. Work phase by phase. Mark `[x]` checkboxes as you complete tasks.
3. If anything is ambiguous — **stop and ask**, never decide unilaterally.

### FIP Constraint Enforcement

Each phase in a FIP may declare **Data sources** and **Side effects** blocks:
- **Data sources:** Where each required datum comes from (navigation params, local state, service response). Only use the listed sources.
- **Side effects — ✅ Allowed:** Only the explicitly listed operations are permitted.
- **Side effects — ❌ Forbidden:** Do not perform these under any circumstance.

If a phase has no Data sources / Side effects blocks, fall back to the general rule: implement only what the FIP describes. Do not invent service calls, use cases, or data sources not in the plan.

### Agent Roles

| Agent | Responsibility | Boundary |
|---|---|---|
| **planner** | Creates/updates Feature Briefs (`FB-XXX`) and FIPs (`FIP-XXX`) in `docs/` | Does NOT write production code or tests |
| **executor** | Implements code following a FIP | Does NOT create or modify planning documents |

Both agents must read `AGENTS.md` before starting any work.

## Key Directories

- `build-logic/convention/` — Gradle convention plugins (start here to understand build setup)
- `core/design-system/.../theme/` — `Spacings`, `Size`, `Weights`, `AppIcons`, `Color`, `Theme`
- `core/string-resources/src/main/res/` — All user-facing strings (en + es)
- `core/testing/` — `MockExtensions.kt`, `TestDispatcherProvider`, `CoroutineTestExtension`
- `core/domain/` — Shared models, `DispatcherProvider`, cross-feature use cases
- `gradle/libs.versions.toml` — Single source of truth for all dependency versions

