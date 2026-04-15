package com.verbum.feature.bible.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.ui.components.VerbumErrorState
import com.verbum.core.ui.components.VerbumLoadingIndicator
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumScreenPreviews
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.Testament

@Composable
fun BibleScreen(
    onBookChapterSelected: (bookId: Int, chapter: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BibleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BibleContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onBookChapterSelected = onBookChapterSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BibleContent(
    uiState: BibleUiState,
    onSearchQueryChanged: (String) -> Unit,
    onBookChapterSelected: (bookId: Int, chapter: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Holy Bible",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "Sacred Scripture",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )

        when (uiState) {
            is BibleUiState.Loading -> VerbumLoadingIndicator(message = "Loading the Word\u2026")
            is BibleUiState.Error -> VerbumErrorState(
                message = uiState.message,
                onRetry = {},
            )
            is BibleUiState.BooksLoaded -> {
                // Search bar with rounded shape
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            "Search the Scriptures\u2026",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VerbumSpacing.screenPadding, vertical = VerbumSpacing.sm),
                )

                AnimatedVisibility(
                    visible = uiState.searchQuery.length >= 3,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    // Show search results
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = VerbumSpacing.screenPadding,
                            vertical = VerbumSpacing.sm,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.searchResults, key = { "${it.bookId}_${it.chapter}_${it.verseNumber}" }) { verse ->
                            SearchResultItem(
                                bookName = verse.bookName,
                                chapter = verse.chapter,
                                verseNumber = verse.verseNumber,
                                text = verse.text,
                                onClick = { onBookChapterSelected(verse.bookId, verse.chapter) },
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.searchQuery.length < 3,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column {
                        // Testament tabs - modern pill-style
                        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                        val tabs = listOf("Old Testament", "New Testament")

                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                    height = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            divider = {},
                            modifier = Modifier.padding(horizontal = VerbumSpacing.sm),
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (selectedTab == index) FontWeight.Bold
                                                else FontWeight.Normal,
                                            ),
                                        )
                                    },
                                )
                            }
                        }

                        val books = if (selectedTab == 0) uiState.oldTestament else uiState.newTestament

                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = VerbumSpacing.screenPadding,
                                vertical = VerbumSpacing.md,
                            ),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(books, key = { it.id }) { book ->
                                BookListItem(
                                    book = book,
                                    onChapterSelected = { chapter ->
                                        onBookChapterSelected(book.id, chapter)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookListItem(
    book: BibleBook,
    onChapterSelected: (Int) -> Unit,
) {
    Surface(
        onClick = { onChapterSelected(1) },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VerbumSpacing.sm, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Book abbreviation badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = book.abbreviation,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(VerbumSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${book.totalChapters} chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    bookName: String,
    chapter: Int,
    verseNumber: Int,
    text: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md)) {
            Text(
                text = "$bookName $chapter:$verseNumber",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(VerbumSpacing.xs))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CrimsonTextFamily),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BibleScreenPreview() {
    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
            BibleContent(
                uiState = BibleUiState.BooksLoaded(
                    oldTestament = listOf(
                        BibleBook(1, "Genesis", "Gn", Testament.OLD, 50),
                        BibleBook(2, "Exodus", "Ex", Testament.OLD, 40),
                    ),
                    newTestament = listOf(
                        BibleBook(47, "Matthew", "Mt", Testament.NEW, 28),
                        BibleBook(48, "Mark", "Mk", Testament.NEW, 16),
                    ),
                ),
                onSearchQueryChanged = {},
                onBookChapterSelected = { _, _ -> },
            )
        }
    }
}
