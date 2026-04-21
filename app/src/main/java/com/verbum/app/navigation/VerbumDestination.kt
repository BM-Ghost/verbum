package com.verbum.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation destinations for the Verbum app.
 */
sealed class VerbumDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
) {
    // Top-level destinations (bottom nav)
    data object Home : VerbumDestination(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    data object Bible : VerbumDestination(
        route = "bible",
        title = "Bible",
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
    )

    data object Missal : VerbumDestination(
        route = "missal",
        title = "Missal",
        selectedIcon = Icons.Filled.Church,
        unselectedIcon = Icons.Outlined.Church,
    )

    data object Prayer : VerbumDestination(
        route = "prayer",
        title = "Prayer",
        selectedIcon = Icons.Filled.VolunteerActivism,
        unselectedIcon = Icons.Outlined.VolunteerActivism,
    )

    data object Community : VerbumDestination(
        route = "community",
        title = "Community",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People,
    )

    // Nested destinations
    data object BibleReader : VerbumDestination(
        route = "bible/{bookId}/{chapter}",
        title = "Read",
    ) {
        fun createRoute(bookId: Int, chapter: Int) = "bible/$bookId/$chapter"
    }

    data object PrayerDetail : VerbumDestination(
        route = "prayer/{prayerId}",
        title = "Prayer",
    ) {
        fun createRoute(prayerId: String) = "prayer/$prayerId"
    }

    data object AiChat : VerbumDestination(route = "ai_chat", title = "Verbum AI")

    data object Profile : VerbumDestination(route = "profile/{userId}", title = "Profile") {
        fun createRoute(userId: String) = "profile/$userId"
    }

    data object MyProfile : VerbumDestination(route = "my_profile", title = "Profile")

    data object Auth : VerbumDestination(route = "auth", title = "Sign In")

    data object StudyGroup : VerbumDestination(
        route = "study_group/{groupId}",
        title = "Bible Study",
    ) {
        fun createRoute(groupId: String) = "study_group/$groupId"
    }

    data object CreatePost : VerbumDestination(route = "create_post", title = "Share Reflection")

    data object LiturgicalCalendar : VerbumDestination(route = "liturgical_calendar", title = "Calendar")

    data object BibleDiagnostics : VerbumDestination(route = "bible_diagnostics", title = "Bible Diagnostics")

    companion object {
        val bottomNavItems = listOf(Home, Bible, Missal, Prayer, Community)
    }
}
