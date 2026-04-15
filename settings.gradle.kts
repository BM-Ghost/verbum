pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
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
