package com.verbum.feature.prayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.verbum.feature.prayer.domain.model.Prayer
import com.verbum.feature.prayer.domain.model.PrayerCategory

@Composable
fun PrayerScreen(
    onPrayerSelected: (prayerId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PrayerContent(
        uiState = uiState,
        onPrayerSelected = onPrayerSelected,
        modifier = modifier,
    )
}

@Composable
private fun PrayerContent(
    uiState: PrayerUiState,
    onPrayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PrayerUiState.Loading -> VerbumLoadingIndicator(message = "Preparing prayers\u2026")
        is PrayerUiState.Error -> VerbumErrorState(
            message = uiState.message,
            onRetry = {},
        )
        is PrayerUiState.Loaded -> {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = VerbumSpacing.screenPadding,
                    vertical = VerbumSpacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xl),
            ) {
                uiState.prayersByCategory.forEach { (category, prayers) ->
                    item(key = category.name) {
                        Column(verticalArrangement = Arrangement.spacedBy(VerbumSpacing.sm)) {
                            Text(
                                text = "${category.emoji} ${category.displayName}",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(horizontal = VerbumSpacing.screenPadding),
                            )
                        }
                    }
                    items(prayers, key = { it.id }) { prayer ->
                        PrayerItem(
                            prayer = prayer,
                            onClick = { onPrayerSelected(prayer.id) },
                        )
                    }
                    item { Spacer(Modifier.height(VerbumSpacing.xl)) }
                }
            }
        }
    }
}

@Composable
private fun PrayerItem(
    prayer: Prayer,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = VerbumSpacing.screenPadding, vertical = VerbumSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = prayer.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = prayer.text.take(60) + if (prayer.text.length > 60) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = VerbumSpacing.screenPadding))
}

@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PrayerContent(
                uiState = PrayerUiState.Loaded(
                    prayersByCategory = mapOf(
                        PrayerCategory.MORNING to listOf(
                            Prayer("1", "Morning Offering", PrayerCategory.MORNING, "O Jesus, through the Immaculate Heart of Mary…"),
                        ),
                        PrayerCategory.ROSARY to listOf(
                            Prayer("2", "Holy Rosary", PrayerCategory.ROSARY, "In the name of the Father, and of the Son…"),
                        ),
                    ),
                ),
                onPrayerSelected = {},
            )
        }
    }
}
