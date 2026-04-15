package com.verbum.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.ScriptureTypography
import com.verbum.core.ui.theme.VerbumShapes
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme

@Composable
fun VerseCard(
    bookName: String,
    chapter: Int,
    verseNumber: Int,
    verseText: String,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onAskAiClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightColor: androidx.compose.ui.graphics.Color? = null,
) {
    val backgroundColor by animateColorAsState(
        targetValue = highlightColor ?: MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 600),
        label = "verse_bg"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VerbumShapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md)) {
            // Reference
            Text(
                text = "$bookName $chapter:$verseNumber",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            // Verse text
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = ScriptureTypography.verseNumber.fontSize)) {
                        append("$verseNumber ")
                    }
                    append(verseText)
                },
                style = ScriptureTypography.verseText,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = VerbumSpacing.sm),
            )

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VerbumSpacing.sm),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onAskAiClick) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Ask Verbum AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerseCardPreview() {
    VerbumTheme {
        VerseCard(
            bookName = "John",
            chapter = 1,
            verseNumber = 1,
            verseText = "In the beginning was the Word, and the Word was with God, and the Word was God.",
            isBookmarked = true,
            onBookmarkClick = {},
            onShareClick = {},
            onAskAiClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
