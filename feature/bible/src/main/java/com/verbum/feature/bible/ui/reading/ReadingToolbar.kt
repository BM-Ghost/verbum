package com.verbum.feature.bible.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verbum.feature.bible.ui.reading.theme.ReadingTheme
import com.verbum.feature.bible.ui.reading.theme.ReadingThemeType

/**
 * The reading toolbar displayed at the top of the reader.
 *
 * Provides: back navigation, chapter title, chapter nav arrows,
 * search toggle, reading mode switch, and theme picker.
 */
@Composable
fun ReadingToolbar(
    bookName: String,
    chapter: Int,
    totalChapters: Int,
    readingMode: ReadingMode,
    currentThemeType: ReadingThemeType,
    theme: ReadingTheme,
    onNavigateBack: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleChapterNav: () -> Unit,
    onReadingModeChange: (ReadingMode) -> Unit,
    onThemeChange: (ReadingThemeType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showThemeMenu by remember { mutableStateOf(false) }
    var showModeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = theme.toolbarBackground,
        shadowElevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Back
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.toolbarContent,
                    )
                }

                // Chapter navigation
                IconButton(
                    onClick = onPreviousChapter,
                    enabled = chapter > 1,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous chapter",
                        tint = theme.toolbarContent.copy(alpha = if (chapter > 1) 1f else 0.3f),
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Tappable chapter title → opens chapter navigator
                Text(
                    text = "$bookName $chapter",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = theme.toolbarContent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleChapterNav)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )

                IconButton(
                    onClick = onNextChapter,
                    enabled = chapter < totalChapters,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next chapter",
                        tint = theme.toolbarContent.copy(alpha = if (chapter < totalChapters) 1f else 0.3f),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                // Search
                IconButton(onClick = onToggleSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = theme.toolbarContent,
                    )
                }

                // Reading mode
                IconButton(onClick = { showModeMenu = true }) {
                    Icon(
                        imageVector = when (readingMode) {
                            ReadingMode.SCROLL -> Icons.Filled.ViewStream
                            ReadingMode.CODEX -> Icons.AutoMirrored.Filled.MenuBook
                        },
                        contentDescription = "Reading mode",
                        tint = theme.toolbarContent,
                    )
                }
                DropdownMenu(
                    expanded = showModeMenu,
                    onDismissRequest = { showModeMenu = false },
                ) {
                    val allowedModes = theme.allowedModes
                    ReadingMode.entries
                        .filter { mode -> allowedModes == null || mode.name in allowedModes }
                        .forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = mode == readingMode,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = theme.accentColor,
                                        ),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(mode.displayName, fontWeight = FontWeight.Medium)
                                        Text(
                                            mode.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onReadingModeChange(mode)
                                showModeMenu = false
                            },
                        )
                    }
                }

                // Theme picker
                IconButton(onClick = { showThemeMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.ColorLens,
                        contentDescription = "Theme",
                        tint = theme.toolbarContent,
                    )
                }
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false },
                ) {
                    ReadingThemeType.entries.forEach { themeType ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = themeType == currentThemeType,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = theme.accentColor,
                                        ),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(themeType.displayName, fontWeight = FontWeight.Medium)
                                        Text(
                                            themeType.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onThemeChange(themeType)
                                showThemeMenu = false
                            },
                        )
                    }
                }
            }
        }
    }
}
