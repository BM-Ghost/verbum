package com.verbum.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.ui.theme.VerbumScreenPreviews
import com.verbum.core.ui.components.VerbumLoadingState
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.profile.domain.model.ProfileStats
import com.verbum.feature.profile.domain.model.UserProfile

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::loadProfile,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        when (uiState) {
            is ProfileUiState.Loading -> {
                VerbumLoadingState(modifier = Modifier.padding(padding))
            }

            is ProfileUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(VerbumSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            is ProfileUiState.Success -> {
                ProfileSuccessContent(
                    profile = uiState.profile,
                    stats = uiState.stats,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ProfileSuccessContent(
    profile: UserProfile,
    stats: ProfileStats,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(VerbumSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = profile.displayName.take(2).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(Modifier.height(VerbumSpacing.md))

        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.headlineSmall,
        )

        if (profile.parish != null) {
            Text(
                text = profile.parish,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (profile.bio.isNotBlank()) {
            Spacer(Modifier.height(VerbumSpacing.sm))
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = VerbumSpacing.lg),
            )
        }

        Spacer(Modifier.height(VerbumSpacing.lg))

        // Social stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SocialStatItem(count = profile.postsCount, label = "Reflections")
            SocialStatItem(count = profile.followersCount, label = "Followers")
            SocialStatItem(count = profile.followingCount, label = "Following")
        }

        Spacer(Modifier.height(VerbumSpacing.lg))

        // Faith stats
        Text(
            text = "Faith Journey",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = VerbumSpacing.sm),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
        ) {
            StatCard(
                icon = Icons.Outlined.LocalFireDepartment,
                value = "${stats.readingStreak}",
                label = "Day Streak",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Outlined.MenuBook,
                value = "${stats.totalReadings}",
                label = "Readings",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Outlined.Bookmark,
                value = "${stats.bookmarks}",
                label = "Bookmarks",
                modifier = Modifier.weight(1f),
            )
        }

        if (profile.favoriteVerse != null) {
            Spacer(Modifier.height(VerbumSpacing.lg))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(VerbumSpacing.md)) {
                    Text(
                        text = "Favorite Verse",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(Modifier.height(VerbumSpacing.xs))
                    Text(
                        text = profile.favoriteVerse,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(VerbumSpacing.lg))

        // Edit profile button
        androidx.compose.material3.OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null)
            Spacer(Modifier.size(VerbumSpacing.sm))
            Text("Edit Profile")
        }
    }
}

@Composable
private fun SocialStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VerbumSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(VerbumSpacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
            ProfileContent(
                uiState = ProfileUiState.Success(
                    profile = UserProfile(
                        id = "u-1",
                        displayName = "Maria Santos",
                        email = "maria@verbum.app",
                        bio = "Seeking Christ in Scripture, liturgy, and daily prayer.",
                        parish = "St. Joseph Parish",
                        favoriteVerse = "John 1:14",
                        postsCount = 12,
                        followersCount = 108,
                        followingCount = 67,
                    ),
                    stats = ProfileStats(
                        totalReadings = 342,
                        readingStreak = 27,
                        bookmarks = 84,
                        reflectionsPosted = 12,
                    ),
                ),
                onNavigateBack = {},
                onRetry = {},
            )
        }
    }
}
