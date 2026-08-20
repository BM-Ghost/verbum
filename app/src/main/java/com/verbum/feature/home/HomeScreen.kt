package com.verbum.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.LocalLiturgicalSeason
import com.verbum.core.ui.theme.VerbumSpacing
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.bible.domain.ContinueReadingState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToBible: () -> Unit,
    onNavigateToMissal: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToReader: (bookId: Int, chapter: Int, verse: Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
    continueReadingState: ContinueReadingState? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val season = LocalLiturgicalSeason.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    // Staggered entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Top Bar ──
        TopBar(
            onNavigateToCalendar = onNavigateToCalendar,
            onNavigateToProfile = onNavigateToProfile,
        )

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 },
        ) {
            Column(
                modifier = Modifier.padding(top = VerbumSpacing.xs),
            ) {
                // ── Hero Card (Season + Verse of the Day combined) ──
                HeroCard(
                    season = season,
                    primaryColor = primaryColor,
                    containerColor = containerColor,
                    viewModel = viewModel,
                )

                Spacer(Modifier.height(VerbumSpacing.md))

                // ── Today's Mass (promoted, redesigned as the primary feature card) ──
                TodaysMassCard(
                    season = season,
                    containerColor = containerColor,
                    onClick = onNavigateToMissal,
                    viewModel = viewModel,
                )

                Spacer(Modifier.height(VerbumSpacing.md))

                // ── Daily Prayer and Continue Reading share a row ──
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = VerbumSpacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
                ) {
                    DailyPrayerCard(
                        season = season,
                        onClick = onNavigateToPrayer,
                        modifier = Modifier.weight(1f),
                        viewModel = viewModel,
                    )
                    
                    val readingState = continueReadingState
                    if (readingState != null) {
                        ContinueReadingCard(
                            bookName = readingState.bookName,
                            chapter = readingState.chapter,
                            lastVerse = readingState.lastVerse,
                            onClick = { onNavigateToReader(readingState.bookId, readingState.chapter, readingState.lastVerse) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(VerbumSpacing.lg))
            }
        }
    }
}

// MARK: - Top Bar
@Composable
private fun TopBar(
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .padding(top = VerbumSpacing.sm)
            .padding(bottom = VerbumSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = "Verbum Dei",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "The Word of God",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TopBarIconButton(
                icon = Icons.Filled.CalendarMonth,
                onClick = onNavigateToCalendar,
            )
            TopBarIconButton(
                icon = Icons.Filled.Person,
                onClick = onNavigateToProfile,
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            },
        shape = CircleShape,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

// MARK: - Hero Card (Season + Verse of the Day combined)
@Composable
private fun HeroCard(
    season: LiturgicalSeason,
    primaryColor: Color,
    containerColor: Color,
    viewModel: HomeViewModel,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha * 0.3f),
                            containerColor,
                            containerColor.copy(alpha = 0.6f),
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.05f),
                        radius = size.maxDimension,
                    ),
                )
            }
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(22.dp),
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
        ) {
            // Season badge row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            primaryColor.copy(alpha = 0.14f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = viewModel.seasonEmoji(season),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = season.displayName.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = viewModel.seasonGreeting(season),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Divider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                thickness = 1.dp,
            )

            // Verse of the day, with an editorial pull-quote treatment
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(primaryColor, RoundedCornerShape(50))
                    )
                    Text(
                        text = "VERSE OF THE DAY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.5.sp,
                        ),
                        color = primaryColor,
                    )
                }

                Text(
                    text = "\u201CIn the beginning was the Word, and the Word was with God, and the Word was God.\u201D",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = CrimsonTextFamily,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "\u2014 John 1:1",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = primaryColor,
                )
            }
        }
    }
}

// MARK: - Today's Mass (promoted, redesigned as the primary feature card)
@Composable
private fun TodaysMassCard(
    season: LiturgicalSeason,
    containerColor: Color,
    onClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.55f),
                        ),
                    ),
                )
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = "TODAY'S MASS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.ArrowCircleRight,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f),
                )
            }

            Text(
                text = viewModel.todaysMass.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MassReadingRow(
                    icon = Icons.Outlined.Book,
                    text = viewModel.todaysMass.firstReading,
                )
                MassReadingRow(
                    icon = Icons.Outlined.Book,
                    text = viewModel.todaysMass.gospel,
                )
            }
        }
    }
}

@Composable
private fun MassReadingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(11.dp)
                .padding(top = 1.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// MARK: - Daily Prayer (compact, shares row with Continue Reading)
@Composable
private fun DailyPrayerCard(
    season: LiturgicalSeason,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(132.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f),
                shape = RoundedCornerShape(18.dp),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.sm + 2.dp),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PanTool,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "PRAYER",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = viewModel.seasonPrayer(season),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CrimsonTextFamily,
                    lineHeight = 24.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

// MARK: - Continue Reading (compact, shares row with Daily Prayer)
@Composable
private fun ContinueReadingCard(
    bookName: String,
    chapter: Int,
    lastVerse: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .height(132.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f),
                shape = RoundedCornerShape(18.dp),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.sm + 2.dp),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Book,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "CONTINUE READING",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                minLines = 1,
            )
            Text(
                text = "$bookName $chapter:$lastVerse",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CrimsonTextFamily,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowCircleRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(VerbumPreviewVariantProvider::class) variant: VerbumPreviewVariant,
) {
    VerbumTheme(liturgicalSeason = variant.season, darkTheme = variant.darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onNavigateToBible = {},
                onNavigateToMissal = {},
                onNavigateToAiChat = {},
                onNavigateToPrayer = {},
                onNavigateToProfile = {},
                onNavigateToCommunity = {},
                onNavigateToCalendar = {},
            )
        }
    }
}
