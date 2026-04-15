package com.verbum.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.theme.LocalLiturgicalSeason
import com.verbum.core.ui.theme.VerbumShapes
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme

@Composable
fun LiturgicalSeasonBanner(
    season: LiturgicalSeason,
    dateLabel: String,
    seasonSubtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = VerbumShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = season.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = VerbumSpacing.xs),
            )

            Text(
                text = seasonSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = VerbumSpacing.xs),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiturgicalSeasonBannerPreview() {
    VerbumTheme(liturgicalSeason = LiturgicalSeason.ADVENT) {
        LiturgicalSeasonBanner(
            season = LiturgicalSeason.ADVENT,
            dateLabel = "Tuesday, December 3, 2024",
            seasonSubtitle = "First Week of Advent — Come, Lord Jesus",
            modifier = Modifier.padding(16.dp),
        )
    }
}
