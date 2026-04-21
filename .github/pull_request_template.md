## Description
<!-- Required: Explain the context and motivation for this change. -->



## Type of Change
<!-- Check all that apply. -->
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that alters existing behaviour)
- [ ] Refactor (code restructure with no behaviour change)
- [ ] Performance improvement
- [ ] UI / Preview update
- [ ] Documentation / README update
- [ ] CI/CD / DevOps change (requires owner approval)
- [ ] Dependency update

---

## Contributor Checklist

### Code Quality
- [ ] `./gradlew verifyVerbumGuidelines` passes locally with no violations
- [ ] `./gradlew lintDebug` produces zero new warnings
- [ ] No `TODO` or `FIXME` left in changed files

### Architecture
- [ ] All new `@HiltViewModel` classes inject only `Repository` interfaces — no direct DAO access
- [ ] UI state flows through `StateFlow` in ViewModel — no raw LiveData
- [ ] New feature code lives in `feature/<name>/` — no business logic added to `core/` without discussion
- [ ] No circular module dependencies introduced (`:app` → `feature:*` → `core:*` only)

### UI / Previews
- [ ] Every new `@Composable` screen has a `@Preview` using `@PreviewParameter(VerbumPreviewVariantProvider::class)` (12-variant matrix)
- [ ] Preview renders correctly in Android Studio for all 12 variants (6 seasons × light/dark)
- [ ] Material 3 components used — no `androidx.compose.material` (v2) imports
- [ ] `@OptIn(ExperimentalMaterial3Api::class)` added where required (e.g. `TopAppBar`, `ExposedDropdownMenuBox`)

### Tests
- [ ] Unit tests added or updated for new logic
- [ ] `./gradlew testDebugUnitTest` passes locally

### Security
- [ ] No secrets, API keys, or credentials added to source files
- [ ] All network calls use HTTPS
- [ ] Input from external sources is validated before use

---

## Screenshots / Screen Recordings
<!-- If UI changes are included, add before/after screenshots for at least one light and one dark theme variant. -->

| Before | After |
|--------|-------|
|        |       |

---

## Related Issues
<!-- Link related issues using "Closes #123" or "Relates to #456" syntax. -->
