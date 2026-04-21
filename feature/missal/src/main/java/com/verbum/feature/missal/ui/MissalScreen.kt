package com.verbum.feature.missal.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.components.VerbumErrorState
import com.verbum.core.ui.components.VerbumLoadingIndicator
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.LocalLiturgicalSeason
import com.verbum.core.ui.theme.VerbumSpacing
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.missal.domain.model.DailyReadings
import com.verbum.feature.missal.domain.model.MissalReading
import com.verbum.feature.missal.domain.model.ReadingType

@Composable
fun MissalScreen(
    modifier: Modifier = Modifier,
    viewModel: MissalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MissalContent(uiState = uiState, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissalContent(
    uiState: MissalUiState,
    modifier: Modifier = Modifier,
) {
    val season = LocalLiturgicalSeason.current
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(800),
        label = "missal_container",
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Today\u2019s Mass",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "Liturgy of the Word",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )

        // Liturgical season banner — gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.6f),
                        ),
                    ),
                )
                .padding(horizontal = VerbumSpacing.screenPadding, vertical = VerbumSpacing.md),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer),
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text(
                    text = season.displayName.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        when (uiState) {
            is MissalUiState.Loading -> VerbumLoadingIndicator(message = "Preparing the Liturgy of the Word\u2026")
            is MissalUiState.Error -> VerbumErrorState(message = uiState.message, onRetry = {})
            is MissalUiState.Loaded -> {
                val readings = uiState.dailyReadings

                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = VerbumSpacing.screenPadding,
                        vertical = VerbumSpacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(VerbumSpacing.md),
                ) {
                    // Feast or memorial
                    readings.feastOrMemorial?.let { feast ->
                        item {
                            Text(
                                text = feast,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Italic,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = VerbumSpacing.sm),
                            )
                        }
                    }

                    items(readings.readings, key = { it.id }) { reading ->
                        ReadingCard(reading = reading)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingCard(reading: MissalReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.lg)) {
            // Reading type label with accent line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text(
                    text = reading.type.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Reference
            Text(
                text = reading.reference,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = VerbumSpacing.xs),
            )

            Spacer(Modifier.height(VerbumSpacing.md))

            // Reading text
            Text(
                text = reading.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = CrimsonTextFamily,
                    lineHeight = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MissalScreenPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MissalContent(
                uiState = MissalUiState.Loaded(
                    selectedDate = "2026-04-15",
                    dailyReadings = DailyReadings(
                        date = "2026-04-15",
                        season = variant.season,
                    feastOrMemorial = "Wednesday of the 3rd Week of Easter",
                    readings = listOf(
                        MissalReading(
                            id = "1",
                            type = ReadingType.FIRST_READING,
                            title = "First Reading",
                            reference = "Acts 8:1b-8",
                            text = "There broke out a severe persecution of the Church in Jerusalem...",
                        ),
                        MissalReading(
                            id = "2",
                            type = ReadingType.PSALM,
                            title = "Responsorial Psalm",
                            reference = "Ps 66:1-3a, 4-5, 6-7a",
                            text = "R. Let all the earth cry out to God with joy.\nShout joyfully to God, all the earth...",
                        ),
                        MissalReading(
                            id = "3",
                            type = ReadingType.GOSPEL,
                            title = "Gospel",
                            reference = "John 6:35-40",
                            text = "Jesus said to the crowds, \"I am the bread of life; whoever comes to me will never hunger...\"",
                        ),
                        ),
                    ),
                ),
            )
        }
    }
}
