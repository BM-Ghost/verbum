# Verbum

Verbum is a modular Android app for Catholic faith formation, Scripture study, liturgy, prayer, community, and AI-assisted spiritual reflection.

## Core Product Areas

- Bible: multilingual Scripture reading with local-first data and paging.
- Liturgy (Missal + Calendar): daily readings and liturgical context with offline resilience.
- Prayer: locally seeded prayer content and user-facing prayer retrieval.
- Community + AI Verbum: social and AI-assisted experiences layered on top of the same modular foundation.

## Stack

- Kotlin + Jetpack Compose + Material 3
- Hilt for dependency injection
- Room for local persistence
- Retrofit + Moshi + OkHttp for networking
- Coroutines + Flow

## Project Layout

- `app/`: application module, navigation, and entry points
- `core/`: shared modules (common, ui, network, database)
- `feature/`: user-facing features

## Bible Implementation

### What Is Included

- English Bible source (`pg1581`) packaged in app assets.
- Latin Vulgate source (`vulsearch_vulgate`) packaged in app assets.
- Canonical mapping and book metadata for all supported books.
- Locale-aware language selection with user override support.

### Data Flow

1. On app bootstrap, Bible seeding runs from assets into Room.
2. Language selection resolves in this order:
	- explicit user selection
	- compatible device locale
	- English fallback
	- Latin fallback (if English unavailable)
3. Bible access is repository-driven, paged, and cache-assisted.

### Performance/Architecture Notes

- Batch inserts are used during seeding.
- Room is indexed for verse lookups.
- Paging is used for chapter/verse retrieval.
- In-memory chapter caching is used to reduce repeated I/O.

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

- Bible assets: `app/src/main/assets/bible/source/`
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

## Engineering Guardrails

The project enforces key scalability/readability rules with architecture and verification checks.

### Do

- Keep UI state hoisted so composables are reusable and previewable.
- Keep each feature isolated in its own module for loose coupling.
- Follow single responsibility in classes and functions.
- Add screen-level previews that cover light mode, dark mode, and multiple liturgical seasons.
- Route persistence access through repositories/services instead of directly in UI/ViewModel.
- Write and maintain unit tests for domain/service behavior.
- Split large ViewModels into focused ViewModels/use-cases.
- Use theme tokens (`MaterialTheme` + liturgical theme engine) for colors and styling.
- Keep liturgical season behavior dynamic and centralized in the theme/season engines.
- Use async/await-style coroutine APIs (`suspend`, `Flow`) with explicit error handling.
- Keep models type-safe (`data class`, `enum class`, sealed types).
- Route AI logic through the `feature/ai-verbum` service/data layer.

### Don't

- Do not place business logic in composable UI bodies.
- Do not hardcode API URLs, theme colors, or magic UI constants in feature screens.
- Do not tightly couple modules.
- Do not access DAOs from UI or ViewModels.
- Do not create God ViewModels.
- Do not bypass the liturgical theme system.
- Do not render raw AI responses without service-layer processing/validation.

### Automated Verification

Run:

`./gradlew verifyVerbumGuidelines`

This task checks for:

- Unsafe null handling patterns.
- Hardcoded hex colors outside the theme package.
- DAO imports in UI/ViewModel layer.
- Direct `VerbumApi` usage outside approved network/AI data layers.
- Missing screen previews and missing preview matrix usage.
- Missing unit tests.
