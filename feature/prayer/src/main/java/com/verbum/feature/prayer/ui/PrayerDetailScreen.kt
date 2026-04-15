package com.verbum.feature.prayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumScreenPreviews
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.prayer.domain.model.Prayer
import com.verbum.feature.prayer.domain.model.PrayerCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    // In production, inject ViewModel and get prayer from savedStateHandle
) {
    // Placeholder content — in production, ViewModel loads by prayerId
    val prayer = Prayer(
        id = "our_father",
        title = "Our Father",
        category = PrayerCategory.DEVOTION,
        text = """Our Father, who art in heaven,
hallowed be thy name;
thy kingdom come,
thy will be done
on earth as it is in heaven.
Give us this day our daily bread,
and forgive us our trespasses,
as we forgive those who trespass against us;
and lead us not into temptation,
but deliver us from evil.
Amen.""",
        latinText = """Pater noster, qui es in caelis,
sanctificetur nomen tuum.
Adveniat regnum tuum.
Fiat voluntas tua,
sicut in caelo et in terra.
Panem nostrum quotidianum da nobis hodie,
et dimitte nobis debita nostra
sicut et nos dimittimus debitoribus nostris.
Et ne nos inducas in tentationem,
sed libera nos a malo.
Amen.""",
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = prayer.title,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
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
        ) {
            // Prayer text — calm, readable
            Text(
                text = prayer.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = CrimsonTextFamily,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            prayer.latinText?.let { latin ->
                Spacer(Modifier.height(VerbumSpacing.xl))
                Text(
                    text = "LATIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(VerbumSpacing.sm))
                Text(
                    text = latin,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = CrimsonTextFamily,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(VerbumSpacing.xxl))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrayerDetailPreview() {
    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
            PrayerDetailScreen(onNavigateBack = {})
        }
    }
}
