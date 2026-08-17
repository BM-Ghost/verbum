# Verbum

> An Android app for Catholic faith formation — Scripture, liturgy, prayer, community, and AI-assisted spiritual reflection.
>
> Started by [Bwire](https://meshackbwire.vercel.app/) and open to creative collaboration.

---

[![PR Validation](https://github.com/BM-Ghost/verbum/actions/workflows/01-pr-validation.yml/badge.svg)](https://github.com/BM-Ghost/verbum/actions/workflows/01-pr-validation.yml) [![CI — Dev](https://github.com/BM-Ghost/verbum/actions/workflows/02-ci-dev.yml/badge.svg?branch=dev)](https://github.com/BM-Ghost/verbum/actions/workflows/02-ci-dev.yml) [![Deploy — SIT](https://github.com/BM-Ghost/verbum/actions/workflows/03-deploy-sit.yml/badge.svg?branch=sit)](https://github.com/BM-Ghost/verbum/actions/workflows/03-deploy-sit.yml) [![Deploy — UAT](https://github.com/BM-Ghost/verbum/actions/workflows/04-deploy-uat.yml/badge.svg?branch=uat)](https://github.com/BM-Ghost/verbum/actions/workflows/04-deploy-uat.yml) [![Deploy — Production](https://github.com/BM-Ghost/verbum/actions/workflows/05-deploy-production.yml/badge.svg?branch=main)](https://github.com/BM-Ghost/verbum/actions/workflows/05-deploy-production.yml) [![Security Scan](https://github.com/BM-Ghost/verbum/actions/workflows/06-security-scan.yml/badge.svg)](https://github.com/BM-Ghost/verbum/actions/workflows/06-security-scan.yml)

---

## Core Product Areas

- **Bible** — Douay-Rheims Bible, Challoner Revision (DRC), paged and local-first.
- **Liturgy** — daily Missal readings and liturgical calendar, works fully offline.
- **Prayer** — seeded prayer content, readily available without a network connection.
- **Community + AI Verbum** — social features and AI-assisted reflection, built on the same modular foundation.

## Stack

- Kotlin + Jetpack Compose + Material 3
- Hilt for dependency injection
- Room for local persistence
- Retrofit + Moshi + OkHttp for networking
- Coroutines + Flow

## Project Layout

```
app/        → application entry point, navigation graph, feature wiring
core/       → shared infrastructure (common, ui, network, database)
feature/    → self-contained feature modules
```

## Bible Implementation

### What Is Included

- English DRC source packaged in app assets from `scrollmapper/bible_databases` (2025).
- OpenBible cross-reference data packaged from the same project's 2025 source extras.
- Canonical mapping and book metadata for all 73 Catholic books.
- Locale-aware language selection with user override support.

### Data Flow

1. On app bootstrap, Bible seeding runs from assets into Room.
2. Language selection resolves in this order:
	- explicit user selection
	- compatible device locale
	- English fallback
        - English fallback
3. Bible access is repository-driven, paged, and cache-assisted.

### Performance/Architecture Notes

- Batch inserts are used during seeding.
- Room is indexed for verse lookups.
- Paging is used for chapter/verse retrieval.
- In-memory chapter caching is used to reduce repeated I/O.
- Cross-references are streamed into indexed Room storage during bootstrap and queried by source verse with vote ranking.

## Liturgy (Missal + Calendar) Implementation

### What Is Included

- Missal readings and liturgical calendar assets under `app/src/main/assets/missal/`.
- Multi-year static JSON datasets (as available from the source repository).
- Calendar and season logic in `feature/missal` domain/data layers.

### Data Strategy

Liturgy follows a local-first model with layered fallback:

1. Return cached database data when available.
2. Fall back to bundled assets when cache is missing.
3. Optionally refresh from network sources where supported.
4. Persist refreshed data back to Room.

This keeps the liturgy experience usable offline while allowing refresh when connectivity is available.

## Assets and Seeding

- Bible asset: `app/src/main/assets/bible/source/scrollmapper/DRC.csv`
- Cross-reference asset: `app/src/main/assets/bible/source/scrollmapper/cross_references.txt`
- Missal assets: `app/src/main/assets/missal/`
- Prayer assets: `app/src/main/assets/prayers/`

Seeding and warmup are orchestrated from app bootstrap coordination logic so initial data is available early in app lifecycle.

## Build and Verification

### Main Build Check

`./gradlew :app:compileDebugKotlin`

### Full Project Build

`./gradlew build`

### Architecture/Guideline Verification

`./gradlew verifyVerbumGuidelines`

On Windows PowerShell, use `./gradlew.bat ...` if required by local shell configuration.

## Contributing

### Your Role as a Contributor

Contributors work on feature branches and open PRs to `dev`. Everything beyond that — promoting builds through SIT, UAT, and out to the Play Store — is handled by the maintainer. Think of it like a parish: the Priest (that's me) handles the liturgy of deployment, you just need to show up prepared. 😏

### Branch Naming — Required

All PRs to `dev` **must** originate from a branch with one of these prefixes:

| Prefix | Example |
|---|---|
| `feature-<description>` | `feature-bible-search` |
| `ft-<description>` | `ft-prayer-detail-screen` |

PRs from branches that do not match this convention are **automatically rejected** by CI before any code review begins. To rename your branch:

```bash
git branch -m old-name feature-your-description
git push origin feature-your-description
git push origin --delete old-name
```

### The Promotion Pipeline

Once you open a PR, this is what happens:

```
feature-* / ft-*
       │
       ▼  Pull Request (you open this)
      dev ◄── CI validates: branch name, guidelines, lint, tests, build
               │
               └── Admin reviews and merges if all checks pass
                       │
                       ▼  Admin opens PR: dev → sit
                      sit ◄── CI validates again
                               │
                               └── Admin merges; auto-deploys to SIT testers
                                       │
                                       ▼  QA/Admin opens PR: sit → uat
                                      uat ◄── CI validates (release mode)
                                               │
                                               └── Manual approval → Firebase UAT testers
                                                       │
                                                       ▼  QA/Admin opens PR: uat → main
                                                      main ◄── Manual approval
                                                               │
                                                               └── Signed AAB → Google Play Store
```

**Your job ends at step one.** Open a clean PR to `dev` with all local checks passing. Once it merges, [Bwire](https://meshackbwire.vercel.app/) takes it from there.

### Before Opening a PR

Run all three checks locally and ensure they pass with zero errors:

```bash
./gradlew verifyVerbumGuidelines
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

---

## Engineering Guidelines

This section is the contributor contract for this repository.

### Architecture and Module Boundaries

- `app`: app bootstrap, navigation graph, feature orchestration only.
- `core/common`: shared primitives (extensions, result wrappers, dispatchers, utilities).
- `core/ui`: theme engine, design tokens, reusable UI components, preview utilities.
- `core/database`: Room entities, DAOs, migrations.
- `core/network`: Retrofit APIs, interceptors, DTO mapping helpers.
- `feature/*`: feature-specific UI, ViewModel, domain use-cases, repositories, DI.

Allowed dependency direction:

1. `app` -> `feature/*`, `core/*`
2. `feature/*` -> `core/*`
3. `core/*` -> no `feature/*`

Forbidden:

- `feature/*` -> `feature/*`
- UI/ViewModel layer -> direct DAO imports
- Feature UI/ViewModel -> direct network API calls

### ✅ Do's (Technical)

1. Use this screen structure: `FeatureScreen(...)` for wiring and `FeatureContent(...)` for pure UI rendering.
2. Hoist screen state from ViewModel (`StateFlow` + `collectAsStateWithLifecycle`) into `Content` composables.
3. Route data access through `UseCase` and `Repository` layers; keep DAOs inside data/repository boundaries.
4. Keep each ViewModel focused; split by use-case when a ViewModel starts owning unrelated flows.
5. Use type-safe domain models (`data class`, `enum class`, sealed UI state) instead of untyped maps.
6. Use `MaterialTheme` and liturgical theme tokens for all colors/typography/spacing.
7. Add previews for every `*Screen.kt` file.
8. Use one of the approved preview matrix patterns:
	 1. `VerbumScreenPreviews { ... }`, or
	 2. `@PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant`
9. For `TopAppBar` and other experimental Material 3 APIs, annotate with `@OptIn(ExperimentalMaterial3Api::class)` at the right scope.
10. Keep AI calls behind `feature/ai-verbum` data/domain layers.
11. Write/maintain unit tests for domain logic and repository boundaries.
12. Validate input in use-cases before repository calls (search query length, IDs, language keys, etc.).

### ❌ Don'ts (Technical)

1. Do not import `com.verbum.core.database.dao.*` in `/ui/` or `*ViewModel.kt` files.
2. Do not use force unwrap (`!!`); use safe fallbacks and explicit null handling.
3. Do not hardcode hex colors in feature/UI files (`Color(0x...)`); use theme tokens.
4. Do not call `verbumApi.sendAiMessage(...)` outside `feature/ai-verbum` data layer.
5. Do not put business logic directly inside composable bodies.
6. Do not create feature-to-feature compile-time dependencies.
7. Do not introduce Material 2 components; stay on Material 3.
8. Do not create omni ViewModels that coordinate unrelated responsibilities.
9. Do not add hidden global state as a shortcut for navigation/data flow.
10. Do not skip previews for new/changed screen-level composables.
11. Do not ship code without running verification + compile checks.

### Naming and File Conventions

1. Screen file: `FeatureScreen.kt`
2. ViewModel: `FeatureViewModel.kt`
3. Use case: `ActionThingUseCase.kt`
4. Repository pair: `ThingRepository.kt` + `ThingRepositoryImpl.kt`
5. UI state: sealed type named `FeatureUiState`

### Required Local Checks Before PR

Run these from repository root:

1. `./gradlew verifyVerbumGuidelines`
2. `./gradlew :app:compileDebugKotlin`
3. `./gradlew :app:assembleDebug`

Recommended for broader validation:

1. `./gradlew build`
2. `./gradlew test`

### Automated Guideline Verification

`./gradlew verifyVerbumGuidelines` enforces:

1. No `!!` force unwraps.
2. No hardcoded hex colors outside allowed theme packages.
3. No DAO imports in UI/ViewModel layers.
4. No direct AI endpoint calls outside approved AI data layer.
5. Every `*Screen.kt` includes `@Preview` and a preview variant matrix pattern.
6. Unit tests exist in `src/test`.

Violations are reported with exact `file:line` locations.
