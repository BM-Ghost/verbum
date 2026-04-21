package com.verbum.feature.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.ui.components.VerbumErrorState
import com.verbum.core.ui.components.VerbumLoadingIndicator
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.VerbumSpacing
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.community.domain.model.CommunityPost
import com.verbum.feature.community.domain.model.PostAuthor

@Composable
fun CommunityFeedScreen(
    onCreatePost: () -> Unit,
    onProfileClick: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CommunityFeedContent(
        uiState = uiState,
        onCreatePost = onCreatePost,
        onProfileClick = onProfileClick,
        onAmenClick = viewModel::onAmenPost,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityFeedContent(
    uiState: CommunityUiState,
    onCreatePost: () -> Unit,
    onProfileClick: (String) -> Unit,
    onAmenClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Text(
                            text = "Faith & Fellowship",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePost,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Post")
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        when (uiState) {
            is CommunityUiState.Loading -> VerbumLoadingIndicator(
                modifier = Modifier.padding(innerPadding),
            )
            is CommunityUiState.Error -> VerbumErrorState(
                message = uiState.message,
                onRetry = {},
                modifier = Modifier.padding(innerPadding),
            )
            is CommunityUiState.Loaded -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + VerbumSpacing.sm,
                        bottom = innerPadding.calculateBottomPadding() + 80.dp,
                        start = VerbumSpacing.screenPadding,
                        end = VerbumSpacing.screenPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(VerbumSpacing.md),
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            onProfileClick = { onProfileClick(post.author.id) },
                            onAmenClick = { onAmenClick(post.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    onProfileClick: () -> Unit,
    onAmenClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.lg)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onProfileClick),
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = post.author.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text(
                    text = post.author.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(VerbumSpacing.sm))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Verse reference card within post
            post.verseText?.let { verseText ->
                Spacer(Modifier.height(VerbumSpacing.sm))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(VerbumSpacing.sm)) {
                        post.verseReference?.let { ref ->
                            Text(
                                text = ref,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = "\"$verseText\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = CrimsonTextFamily,
                                fontStyle = FontStyle.Italic,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                Spacer(Modifier.height(VerbumSpacing.sm))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.xs)) {
                    post.tags.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ),
                            border = null,
                        )
                    }
                }
            }

            Spacer(Modifier.height(VerbumSpacing.sm))

            // Actions: Amen + Comment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAmenClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Amen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(VerbumSpacing.xs))
                    Text(
                        text = "${post.amenCount} Amen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(VerbumSpacing.xs))
                    Text(
                        text = "${post.commentCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityFeedPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommunityFeedContent(
            uiState = CommunityUiState.Loaded(
                posts = listOf(
                    CommunityPost(
                        id = "1",
                        author = PostAuthor("u1", "Maria Santos"),
                        content = "Today's reading reminded me of God's infinite mercy. Let us always trust in His plan.",
                        verseReference = "John 3:16",
                        verseText = "For God so loved the world that he gave his only Son...",
                        tags = listOf("Easter", "Gospel"),
                        amenCount = 24,
                        commentCount = 5,
                        createdAt = System.currentTimeMillis(),
                    ),
                    CommunityPost(
                        id = "2",
                        author = PostAuthor("u2", "Fr. James"),
                        content = "A beautiful reflection: The Eucharist is the source and summit of our faith.",
                        tags = listOf("Eucharist", "reflection"),
                        amenCount = 67,
                        commentCount = 12,
                        createdAt = System.currentTimeMillis(),
                    ),
                ),
            ),
            onCreatePost = {},
            onProfileClick = {},
            onAmenClick = {},
            )
        }
    }
}
