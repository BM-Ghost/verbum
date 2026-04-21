package com.verbum.feature.bible.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.bible.data.seed.BibleDiagnosticsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleDiagnosticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BibleDiagnosticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Bible Diagnostics (Debug)") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VerbumSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.md),
        ) {
            StatusCard(uiState)
            VerseCountsCard(uiState)
            IntegrityCard(uiState)
            ErrorCard(uiState)
            Spacer(modifier = Modifier.height(VerbumSpacing.xl))
        }
    }
}

@Composable
private fun StatusCard(state: BibleDiagnosticsState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md), verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs)) {
            Text("Seed status", style = MaterialTheme.typography.titleSmall)
            Text(state.seedStatus.name, style = MaterialTheme.typography.bodyLarge)
            val updatedAt = state.snapshot?.generatedAtMs
            Text(
                text = if (updatedAt != null) "Last update: ${updatedAt.formatAsDateTime()}" else "Last update: n/a",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VerseCountsCard(state: BibleDiagnosticsState) {
    Card {
        Column(modifier = Modifier.padding(VerbumSpacing.md), verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs)) {
            Text("Verse counts per language", style = MaterialTheme.typography.titleSmall)
            val counts = state.snapshot?.languageVerseCounts.orEmpty()
            if (counts.isEmpty()) {
                Text("No seeded language counts available yet.", style = MaterialTheme.typography.bodySmall)
            } else {
                counts.forEach { (language, count) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(language, style = MaterialTheme.typography.bodyMedium)
                        Text(count.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrityCard(state: BibleDiagnosticsState) {
    Card {
        Column(modifier = Modifier.padding(VerbumSpacing.md), verticalArrangement = Arrangement.spacedBy(VerbumSpacing.sm)) {
            Text("Integrity checks", style = MaterialTheme.typography.titleSmall)
            val snapshot = state.snapshot
            if (snapshot == null) {
                Text("No integrity snapshot yet.", style = MaterialTheme.typography.bodySmall)
                return@Column
            }

            snapshot.languageVerseCounts.keys.forEach { language ->
                val missingBooks = snapshot.missingBooksByLanguage[language].orEmpty()
                val partialBooks = snapshot.partialBooksByLanguage[language].orEmpty()
                val missingChapters = snapshot.missingChapterCountByLanguage[language] ?: 0

                Text("Language: $language", style = MaterialTheme.typography.labelLarge)
                Text("Missing books: ${missingBooks.size}", style = MaterialTheme.typography.bodySmall)
                if (missingBooks.isNotEmpty()) {
                    Text(missingBooks.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
                Text("Books with chapter gaps: ${partialBooks.size}", style = MaterialTheme.typography.bodySmall)
                if (partialBooks.isNotEmpty()) {
                    Text(partialBooks.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
                Text("Missing chapters total: $missingChapters", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(VerbumSpacing.xs))
            }
        }
    }
}

@Composable
private fun ErrorCard(state: BibleDiagnosticsState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        ),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md), verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs)) {
            Text("Last seed error", style = MaterialTheme.typography.titleSmall)
            Text(
                text = state.lastSeedError ?: "None",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

private fun Long.formatAsDateTime(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return formatter.format(Date(this))
}

@Preview(showBackground = true)
@Composable
private fun BibleDiagnosticsScreenPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            BibleDiagnosticsScreenContent(
                onNavigateBack = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BibleDiagnosticsScreenContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Bible Diagnostics (Debug)") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VerbumSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.md),
        ) {
            Text(
                "Bible diagnostics data unavailable in preview",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(VerbumSpacing.xl))
        }
    }
}
