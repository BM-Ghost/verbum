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

            if (forceUnwrapRegex.containsMatchIn(content)) {
                violations += "$relativePath: avoid force-unwrapping style operators (unsafe null handling)."
            }

            if (!relativePath.startsWith("core/ui/src/main/java/com/verbum/core/ui/theme/") &&
                hardcodedColorRegex.containsMatchIn(content)
            ) {
                violations += "$relativePath: hardcoded hex colors are not allowed outside the theme system."
            }

            val isUiLayer = relativePath.contains("/ui/") || relativePath.endsWith("ViewModel.kt")
            if (isUiLayer && daoImportRegex.containsMatchIn(content)) {
                violations += "$relativePath: UI/ViewModel layer cannot import DAO types directly. Use repositories/services."
            }

            val canCallAiApi = relativePath.startsWith("feature/ai-verbum/src/main/java/com/verbum/feature/ai/data/")
            if (!canCallAiApi && aiApiCallRegex.containsMatchIn(content)) {
                violations += "$relativePath: AI endpoint calls must be routed through feature/ai-verbum data layer."
            }

            val isScreenFile =
                relativePath.contains("/src/main/java/") &&
                    relativePath.endsWith("Screen.kt")
            if (isScreenFile) {
                if (!content.contains("@Preview")) {
                    violations += "$relativePath: screen-level composables must define previews."
                }
                if (!content.contains("VerbumScreenPreviews")) {
                    violations += "$relativePath: screen previews must include light/dark and liturgical season variants via VerbumScreenPreviews."
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
            violations += "No unit tests found in src/test directories. Add unit tests for domain and service logic."
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Verbum guideline verification failed:")
                    violations.forEach { violation -> appendLine(" - $violation") }
                },
            )
        }
    }
}
