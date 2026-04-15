# Verbum

Verbum is a modular Android app for Catholic faith formation, Scripture study, liturgy, prayer, community, and AI-assisted spiritual reflection.

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
