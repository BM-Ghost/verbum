package com.verbum.core.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.verbum.core.common.model.LiturgicalSeason

/**
 * Reusable preview matrix for screen-level composables.
 *
 * Each screen preview renders three variants so designers can verify:
 * - Light mode (Advent)
 * - Dark mode (Lent)
 * - A different liturgical season (Easter)
 */
@Composable
fun VerbumScreenPreviews(
    content: @Composable (season: LiturgicalSeason, darkTheme: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VerbumSpacing.lg),
    ) {
        PreviewVariantHeader(title = "Light - Advent")
        content(LiturgicalSeason.ADVENT, false)

        PreviewVariantHeader(title = "Dark - Lent")
        content(LiturgicalSeason.LENT, true)

        PreviewVariantHeader(title = "Light - Easter")
        content(LiturgicalSeason.EASTER, false)
    }
}

@Composable
private fun PreviewVariantHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
