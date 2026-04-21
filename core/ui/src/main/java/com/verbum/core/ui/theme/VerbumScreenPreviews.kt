package com.verbum.core.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
    variants: List<VerbumPreviewVariant> = VerbumPreviewDefaults.allThemeVariants,
    content: @Composable (season: LiturgicalSeason, darkTheme: Boolean) -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VerbumSpacing.lg),
    ) {
        variants.forEach { variant ->
            VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PreviewVariantHeader(title = variant.title)
                        Spacer(modifier = Modifier.height(VerbumSpacing.sm))
                        content(variant.season, variant.darkTheme)
                    }
                }
            }
        }
    }
}

data class VerbumPreviewVariant(
    val title: String,
    val season: LiturgicalSeason,
    val darkTheme: Boolean,
)

object VerbumPreviewDefaults {
    val allThemeVariants: List<VerbumPreviewVariant> = LiturgicalSeason.entries
        .flatMap { season ->
            listOf(
                VerbumPreviewVariant("Light - ${season.displayName}", season, false),
                VerbumPreviewVariant("Dark - ${season.displayName}", season, true),
            )
        }
}

/**
 * PreviewParameterProvider that supplies all liturgical season × light/dark variants.
 *
 * Use with @PreviewParameter so Android Studio renders each variant as a separate
 * named preview panel:
 *
 * ```
 * @Preview
 * @Composable
 * private fun MyScreenPreview(
 *     @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
 * ) {
 *     VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
 *         Surface(color = MaterialTheme.colorScheme.background) { MyContent() }
 *     }
 * }
 * ```
 */
class VerbumPreviewVariantProvider : PreviewParameterProvider<VerbumPreviewVariant> {
    override val values: Sequence<VerbumPreviewVariant> =
        VerbumPreviewDefaults.allThemeVariants.asSequence()
}

@Composable
private fun PreviewVariantHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
