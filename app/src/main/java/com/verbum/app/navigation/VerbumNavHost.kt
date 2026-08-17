package com.verbum.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.verbum.app.BuildConfig
import com.verbum.feature.ai.ui.AiChatScreen
import com.verbum.feature.auth.ui.AuthScreen
import com.verbum.feature.bible.ui.BibleDiagnosticsScreen
import com.verbum.feature.bible.ui.BibleReaderScreen
import com.verbum.feature.bible.ui.BibleScreen
import com.verbum.feature.calendar.ui.LiturgicalCalendarScreen
import com.verbum.feature.community.ui.CommunityFeedScreen
import com.verbum.feature.community.ui.CreatePostScreen
import com.verbum.feature.home.HomeScreen
import com.verbum.feature.home.HomeViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.feature.missal.ui.MissalScreen
import com.verbum.feature.prayer.ui.PrayerDetailScreen
import com.verbum.feature.prayer.ui.PrayerScreen
import com.verbum.feature.profile.ui.ProfileScreen

private const val NAV_ANIM_DURATION = 300

@Composable
fun VerbumApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptic = LocalHapticFeedback.current

    val showBottomBar = VerbumDestination.bottomNavItems.any { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 0.dp,
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    ),
                ) {
                    VerbumDestination.bottomNavItems.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) {
                                        destination.selectedIcon ?: return@NavigationBarItem
                                    } else {
                                        destination.unselectedIcon ?: return@NavigationBarItem
                                    },
                                    contentDescription = destination.title,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(VerbumDestination.AiChat.route)
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Verbum AI",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = VerbumDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        initialOffset = { it / 4 },
                    )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        initialOffset = { it / 4 },
                    )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200))
            },
        ) {
            // ── Home ──
            composable(VerbumDestination.Home.route) {
                val homeViewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val continueReading by homeViewModel.continueReading.collectAsStateWithLifecycle()
                HomeScreen(
                    onNavigateToBible = {
                        navController.navigate(VerbumDestination.Bible.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMissal = {
                        navController.navigate(VerbumDestination.Missal.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAiChat = { navController.navigate(VerbumDestination.AiChat.route) },
                    onNavigateToPrayer = {
                        navController.navigate(VerbumDestination.Prayer.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = { navController.navigate(VerbumDestination.MyProfile.route) },
                    onNavigateToCommunity = {
                        navController.navigate(VerbumDestination.Community.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCalendar = { navController.navigate(VerbumDestination.LiturgicalCalendar.route) },
                    onNavigateToReader = { bookId, chapter, verse ->
                        navController.navigate(
                            VerbumDestination.BibleReader.createRoute(bookId, chapter, verse),
                        )
                    },
                    continueReadingState = continueReading,
                )
            }

            // ── Bible ──
            composable(VerbumDestination.Bible.route) {
                BibleScreen(
                    onBookChapterSelected = { bookId, chapter ->
                        navController.navigate(
                            VerbumDestination.BibleReader.createRoute(bookId, chapter)
                        )
                    },
                    onOpenDiagnostics = if (BuildConfig.DEBUG) {
                        {
                            navController.navigate(VerbumDestination.BibleDiagnostics.route)
                        }
                    } else {
                        null
                    },
                    showDiagnosticsButton = BuildConfig.DEBUG,
                )
            }

            if (BuildConfig.DEBUG) {
                composable(VerbumDestination.BibleDiagnostics.route) {
                    BibleDiagnosticsScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }

            composable(
                route = VerbumDestination.BibleReader.route,
                arguments = listOf(
                    navArgument("bookId") { type = NavType.IntType },
                    navArgument("chapter") { type = NavType.IntType },
                    navArgument("verse") { type = NavType.IntType; defaultValue = -1 },
                ),
            ) {
                BibleReaderScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAskAi = { navController.navigate(VerbumDestination.AiChat.route) },
                )
            }

            // ── Missal ──
            composable(VerbumDestination.Missal.route) {
                MissalScreen()
            }

            // ── Community ──
            composable(VerbumDestination.Community.route) {
                CommunityFeedScreen(
                    onCreatePost = { navController.navigate(VerbumDestination.CreatePost.route) },
                    onProfileClick = { userId ->
                        navController.navigate(VerbumDestination.Profile.createRoute(userId))
                    },
                )
            }

            composable(VerbumDestination.CreatePost.route) {
                CreatePostScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Prayer ──
            composable(VerbumDestination.Prayer.route) {
                PrayerScreen(
                    onPrayerSelected = { prayerId ->
                        navController.navigate(VerbumDestination.PrayerDetail.createRoute(prayerId))
                    },
                )
            }

            composable(
                route = VerbumDestination.PrayerDetail.route,
                arguments = listOf(navArgument("prayerId") { type = NavType.StringType }),
            ) {
                PrayerDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── AI Chat ──
            composable(
                VerbumDestination.AiChat.route,
                enterTransition = {
                    scaleIn(initialScale = 0.92f, animationSpec = tween(NAV_ANIM_DURATION)) +
                        fadeIn(animationSpec = tween(NAV_ANIM_DURATION))
                },
                exitTransition = {
                    scaleOut(targetScale = 0.92f, animationSpec = tween(200)) +
                        fadeOut(animationSpec = tween(200))
                },
            ) {
                AiChatScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Profile ──
            composable(
                route = VerbumDestination.Profile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
            ) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(VerbumDestination.MyProfile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Auth ──
            composable(VerbumDestination.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(VerbumDestination.Home.route) {
                            popUpTo(VerbumDestination.Auth.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Liturgical Calendar ──
            composable(VerbumDestination.LiturgicalCalendar.route) {
                LiturgicalCalendarScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
