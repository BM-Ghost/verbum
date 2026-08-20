pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "Verbum"

include(":app")

// Core modules
include(":core:common")
include(":core:ui")
include(":core:network")
include(":core:database")

// Feature modules
include(":feature:auth")
include(":feature:bible")
include(":feature:missal")
include(":feature:prayer")
include(":feature:community")
include(":feature:ai-verbum")
include(":feature:profile")
include(":feature:liturgical-calendar")
