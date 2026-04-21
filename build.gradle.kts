import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}

tasks.register("verifyVerbumGuidelines") {
    group = "verification"
    description = "Validates architecture and UI guardrails for Verbum modules."
    notCompatibleWithConfigurationCache("Reads source files directly during task execution.")

    doLast {
        val kotlinFiles = rootDir
            .walkTopDown()
            .filter { file ->
                file.isFile &&
                    file.extension == "kt" &&
                    !file.invariantSeparatorsPath.contains("/build/")
            }
            .toList()

        val violations = mutableListOf<String>()

        val forceUnwrapRegex = Regex("!!")
        val hardcodedColorRegex = Regex("Color\\(\\s*0x[0-9A-Fa-f]+")
        val daoImportRegex = Regex("import\\s+com\\.verbum\\.core\\.database\\.dao\\.")
        val aiApiCallRegex = Regex("\\bverbumApi\\.sendAiMessage\\s*\\(")

        kotlinFiles.forEach { file ->
            val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
            val content = file.readText()
            val lines = content.split("\n")

            // Check force unwrap (!!) operators
            lines.forEachIndexed { index, line ->
                if (forceUnwrapRegex.containsMatchIn(line)) {
                    violations += "$relativePath:${index + 1}: Unsafe null handling - avoid force-unwrap (!!) operators"
                }
            }

            // Check hardcoded colors
            val isInThemePackage = relativePath.startsWith("core/ui/src/main/java/com/verbum/core/ui/theme/") ||
                relativePath.contains("feature/bible/src/main/java/com/verbum/feature/bible/ui/reading/theme/")
            if (!isInThemePackage) {
                lines.forEachIndexed { index, line ->
                    if (hardcodedColorRegex.containsMatchIn(line)) {
                        violations += "$relativePath:${index + 1}: Hardcoded hex colors not allowed outside theme system"
                    }
                }
            }

            // Check DAO imports
            val isUiLayer = relativePath.contains("/ui/") || relativePath.endsWith("ViewModel.kt")
            if (isUiLayer) {
                lines.forEachIndexed { index, line ->
                    if (daoImportRegex.containsMatchIn(line)) {
                        violations += "$relativePath:${index + 1}: UI/ViewModel cannot import DAO types - use repositories/services"
                    }
                }
            }

            // Check AI API calls
            val canCallAiApi = relativePath.startsWith("feature/ai-verbum/src/main/java/com/verbum/feature/ai/data/")
            if (!canCallAiApi) {
                lines.forEachIndexed { index, line ->
                    if (aiApiCallRegex.containsMatchIn(line)) {
                        violations += "$relativePath:${index + 1}: AI endpoint calls must route through feature/ai-verbum data layer"
                    }
                }
            }

            // Check screen previews
            val isScreenFile =
                relativePath.contains("/src/main/java/") &&
                    relativePath.endsWith("Screen.kt")
            if (isScreenFile) {
                if (!content.contains("@Preview")) {
                    violations += "$relativePath: Missing @Preview decorator - screen-level composables must define previews"
                }
                val hasPreviewMatrix = content.contains("VerbumScreenPreviews") ||
                    content.contains("VerbumPreviewVariantProvider")
                if (!hasPreviewMatrix) {
                    violations +=
                        "$relativePath: Missing preview variant matrix - use VerbumScreenPreviews or @PreviewParameter(VerbumPreviewVariantProvider::class)"
                }
            }
        }

        val unitTestFiles = rootDir
            .walkTopDown()
            .filter { file ->
                file.isFile &&
                    file.extension == "kt" &&
                    file.invariantSeparatorsPath.contains("/src/test/")
            }
            .toList()

        if (unitTestFiles.isEmpty()) {
            violations += "Missing unit tests: No tests found in src/test directories. Add unit tests for domain and service logic."
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine()
                    appendLine("╔════════════════════════════════════════════════════════════════╗")
                    appendLine("║  ❌ Verbum Engineering Guideline Violations Detected  ❌        ║")
                    appendLine("╚════════════════════════════════════════════════════════════════╝")
                    appendLine()
                    violations.forEach { violation -> appendLine("  $violation") }
                    appendLine()
                    appendLine("📖 See README.md for complete Engineering Guidelines")
                    appendLine("🔧 Fix violations and re-run: ./gradlew verifyVerbumGuidelines")
                    appendLine()
                },
            )
        } else {
            println()
            println("╔════════════════════════════════════════════════════════════════╗")
            println("║  ✅ All Engineering Guidelines Verified Successfully  ✅      ║")
            println("╚════════════════════════════════════════════════════════════════╝")
            println()
            println("  ✓ No unsafe null handling (!! operators)")
            println("  ✓ No hardcoded colors outside theme packages")
            println("  ✓ No DAO imports in UI/ViewModel layers")
            println("  ✓ No direct API calls outside data layers")
            println("  ✓ All screens have previews with theme variants")
            println("  ✓ Unit tests present")
            println()
        }
    }
}
