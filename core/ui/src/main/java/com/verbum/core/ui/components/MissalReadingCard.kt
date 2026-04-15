package com.verbum.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.verbum.core.ui.theme.ScriptureTypography
import com.verbum.core.ui.theme.VerbumShapes
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumTheme

@Composable
fun MissalReadingCard(
    readingLabel: String,
    reference: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VerbumShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md)) {
            Text(
                text = readingLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = reference,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = VerbumSpacing.xs),
            )
            Text(
                text = text,
                style = ScriptureTypography.verseText,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = VerbumSpacing.sm),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MissalReadingCardPreview() {
    VerbumTheme {
        MissalReadingCard(
            readingLabel = "FIRST READING",
            reference = "Isaiah 7:10-14",
            text = "The Lord spoke to Ahaz: Ask for a sign from the Lord, your God; let it be deep as the nether world, or high as the sky!",
            modifier = Modifier.padding(16.dp),
        )
    }
}
