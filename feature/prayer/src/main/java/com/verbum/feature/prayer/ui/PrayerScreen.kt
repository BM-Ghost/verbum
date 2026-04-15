package com.verbum.feature.prayer.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.verbum.core.ui.theme.VerbumScreenPreviews
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerContent(
    uiState: PrayerUiState,
    onPrayerSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Prayer Library",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "Draw near to God",
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
            is PrayerUiState.Loading -> VerbumLoadingIndicator(message = "Preparing prayers\u2026")
            is PrayerUiState.Error -> VerbumErrorState(
                message = uiState.message,
                onRetry = {},
            )
            is PrayerUiState.Loaded -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = VerbumSpacing.screenPadding,
                        vertical = VerbumSpacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    uiState.prayersByCategory.forEach { (category, prayers) ->
                        item(key = category.name) {
                            CategoryHeader(category = category)
                        }
                        items(prayers, key = { it.id }) { prayer ->
                            PrayerItem(
                                prayer = prayer,
                                onClick = { onPrayerSelected(prayer.id) },
                            )
                        }
                        item { Spacer(Modifier.height(VerbumSpacing.md)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: PrayerCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = VerbumSpacing.sm, horizontal = VerbumSpacing.xs),
    ) {
        Text(
            text = category.emoji,
            fontSize = 22.sp,
        )
        Spacer(Modifier.width(VerbumSpacing.sm))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PrayerItem(
    prayer: Prayer,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier.padding(VerbumSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prayer.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(VerbumSpacing.xs))
                Text(
                    text = prayer.text.take(100) + if (prayer.text.length > 100) "\u2026" else "",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = CrimsonTextFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrayerScreenPreview() {
    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
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
