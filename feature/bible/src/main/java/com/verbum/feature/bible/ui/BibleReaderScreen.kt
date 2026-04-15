package com.verbum.feature.bible.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.ui.components.VerbumErrorState
import com.verbum.core.ui.components.VerbumLoadingIndicator
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumScreenPreviews
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.bible.domain.model.Verse

@Composable
fun BibleReaderScreen(
    onNavigateBack: () -> Unit,
    onAskAi: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BibleReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BibleReaderContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onVerseClick = viewModel::onVerseSelected,
        onDismissVerseActions = viewModel::onDismissVerseActions,
        onBookmarkClick = viewModel::onToggleBookmark,
        onAskAi = { verse ->
            onAskAi("${verse.bookName} ${verse.chapter}:${verse.verseNumber}")
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BibleReaderContent(
    uiState: BibleReaderUiState,
    onNavigateBack: () -> Unit,
    onVerseClick: (Verse) -> Unit,
    onDismissVerseActions: () -> Unit,
    onBookmarkClick: (Verse) -> Unit,
    onAskAi: (Verse) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is BibleReaderUiState.Loading -> VerbumLoadingIndicator()
            is BibleReaderUiState.Error -> VerbumErrorState(
                message = uiState.message,
                onRetry = {},
            )
            is BibleReaderUiState.Loaded -> {
                TopAppBar(
                    title = {
                        Text(
                            text = "${uiState.bookName} ${uiState.chapter}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = VerbumSpacing.screenPadding,
                        vertical = VerbumSpacing.md,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.verses, key = { it.verseNumber }) { verse ->
                        VerseItem(
                            verse = verse,
                            onClick = { onVerseClick(verse) },
                        )
                    }
                }

                // Bottom sheet for verse actions
                uiState.selectedVerse?.let { verse ->
                    ModalBottomSheet(
                        onDismissRequest = onDismissVerseActions,
                        sheetState = rememberModalBottomSheetState(),
                        dragHandle = { BottomSheetDefaults.DragHandle() },
                    ) {
                        VerseActionsSheet(
                            verse = verse,
                            onBookmark = { onBookmarkClick(verse) },
                            onShare = { /* share intent */ },
                            onAskAi = { onAskAi(verse) },
                            onDismiss = onDismissVerseActions,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerseItem(
    verse: Verse,
    onClick: () -> Unit,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            ) {
                append("${verse.verseNumber} ")
            }
            append(verse.text)
        },
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = CrimsonTextFamily,
            lineHeight = 28.sp,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = VerbumSpacing.xs),
    )
}

@Composable
private fun VerseActionsSheet(
    verse: Verse,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onAskAi: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(VerbumSpacing.lg),
    ) {
        Text(
            text = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(VerbumSpacing.sm))
        Text(
            text = verse.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CrimsonTextFamily),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(VerbumSpacing.lg))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onBookmark(); onDismiss() }) {
                Icon(
                    imageVector = if (verse.isBookmarked) Icons.Filled.Bookmark
                    else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Bookmark")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onShare(); onDismiss() }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Share")
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onAskAi(); onDismiss() }) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = "Ask AI")
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text("Ask Verbum AI")
            }
        }

        Spacer(Modifier.height(VerbumSpacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun BibleReaderPreview() {
    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
            BibleReaderContent(
                uiState = BibleReaderUiState.Loaded(
                    bookName = "John",
                    chapter = 1,
                    totalChapters = 21,
                    verses = listOf(
                        Verse(50, "John", 1, 1, "In the beginning was the Word, and the Word was with God, and the Word was God."),
                        Verse(50, "John", 1, 2, "He was in the beginning with God."),
                        Verse(50, "John", 1, 3, "All things were made through him, and without him was not any thing made that was made."),
                    ),
                ),
                onNavigateBack = {},
                onVerseClick = {},
                onDismissVerseActions = {},
                onBookmarkClick = {},
                onAskAi = {},
            )
        }
    }
}
