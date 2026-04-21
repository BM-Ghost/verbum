package com.verbum.feature.bible.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.theme.ReadingTheme

/**
 * Chapter grid navigator — lets users jump to any chapter quickly.
 *
 * Chapters are displayed in a grid with the current chapter highlighted.
 * Tapping a chapter number immediately navigates there.
 */
@Composable
fun ChapterNavigator(
    totalChapters: Int,
    currentChapter: Int,
    bookName: String,
    theme: ReadingTheme,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = theme.pageBackground.copy(alpha = 0.97f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = bookName,
                    style = theme.chapterTitleStyle,
                    color = theme.chapterTitleColor,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = theme.textColor,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Select a chapter",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textColor.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(16.dp))

            // Chapter grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                gridItems((1..totalChapters).toList()) { chapter ->
                    val isSelected = chapter == currentChapter
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) theme.accentColor
                                else theme.textColor.copy(alpha = 0.06f),
                            )
                            .clickable { onChapterSelected(chapter) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$chapter",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isSelected) theme.pageBackground else theme.textColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Always-visible inline search form.
 * Shows a text field with a search icon and "Search" button.
 * Users can type a word or phrase and tap Search or press Enter.
 */
@Composable
fun VerseSearchBar(
    query: String,
    suggestions: List<Verse>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onSuggestionClick: (Verse) -> Unit,
    theme: ReadingTheme,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Type a word or verse…",
                        color = theme.textColor.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = theme.textColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch()
                    keyboardController?.hide()
                }),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = theme.textColor),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.accentColor,
                    unfocusedBorderColor = theme.textColor.copy(alpha = 0.15f),
                    focusedContainerColor = theme.pageBackground,
                    unfocusedContainerColor = theme.textColor.copy(alpha = 0.04f),
                    cursorColor = theme.accentColor,
                    focusedTextColor = theme.textColor,
                    unfocusedTextColor = theme.textColor,
                ),
            )

            Spacer(Modifier.width(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.accentColor)
                    .clickable {
                        onSearch()
                        keyboardController?.hide()
                    }
                    .padding(horizontal = 14.dp),
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.pageBackground,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = theme.textColor.copy(alpha = 0.05f),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(suggestions.take(6)) { verse ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(verse) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = theme.accentColor,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = verse.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textColor,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Search results overlay showing matched verses.
 */
@Composable
fun SearchResultsOverlay(
    results: List<Verse>,
    theme: ReadingTheme,
    onVerseClick: (Verse) -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = theme.pageBackground.copy(alpha = 0.97f),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${results.size} result${if (results.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = theme.textColor,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close", tint = theme.textColor)
                    }
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(results.size) { index ->
                        val verse = results[index]
                        SearchResultItem(
                            verse = verse,
                            theme = theme,
                            onClick = { onVerseClick(verse) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    verse: Verse,
    theme: ReadingTheme,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}",
            style = MaterialTheme.typography.labelLarge,
            color = theme.accentColor,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = verse.text,
            style = theme.verseTextStyle,
            color = theme.textColor,
            maxLines = 3,
        )
    }
}
