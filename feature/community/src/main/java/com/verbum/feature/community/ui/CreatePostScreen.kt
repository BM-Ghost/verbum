package com.verbum.feature.community.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.verbum.core.ui.components.VerbumButton
import com.verbum.core.ui.theme.VerbumSpacing
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reflectionText by rememberSaveable { mutableStateOf("") }
    var verseReference by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Share a Reflection") },
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
                .padding(VerbumSpacing.screenPadding),
        ) {
            OutlinedTextField(
                value = reflectionText,
                onValueChange = { reflectionText = it },
                label = { Text("Your reflection…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VerbumSpacing.xxl * 3),
                maxLines = 8,
            )

            Spacer(Modifier.height(VerbumSpacing.md))

            OutlinedTextField(
                value = verseReference,
                onValueChange = { verseReference = it },
                label = { Text("Verse reference (optional, e.g. John 3:16)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(VerbumSpacing.md))

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated, e.g. Lent, Gospel)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(VerbumSpacing.lg))

            VerbumButton(
                text = "Share with Community",
                onClick = {
                    // TODO: wire to ViewModel
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = reflectionText.isNotBlank(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreatePostPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            CreatePostScreen(onNavigateBack = {})
        }
    }
}
