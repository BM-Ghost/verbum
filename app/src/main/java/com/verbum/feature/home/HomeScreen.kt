package com.verbum.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.theme.CrimsonTextFamily
import com.verbum.core.ui.theme.LocalLiturgicalSeason
import com.verbum.core.ui.theme.VerbumPreviewVariant
import com.verbum.core.ui.theme.VerbumPreviewVariantProvider
import com.verbum.core.ui.theme.VerbumSpacing
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
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Staggered entrance animation (matches iOS .easeOut(duration: 0.6).delay(0.1))
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    // Mirrors iOS's GeometryReader + .frame(minHeight: geometry.size.height, alignment: .top):
    // content stretches to fill the screen on tall devices, and still scrolls normally
    // when it doesn't fit on smaller ones.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenMinHeight = this.maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = screenMinHeight),
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
                    Column {
                        // ── Hero Card (Season + Verse of the Day combined) ──
                        HeroCard(
                            season = season,
                            primaryColor = primaryColor,
                            containerColor = containerColor,
                            surfaceColor = surfaceColor,
                            viewModel = viewModel,
                        )

                        Spacer(Modifier.height(VerbumSpacing.md))

                        // ── Today's Mass (promoted, primary feature card) ──
                        TodaysMassCard(
                            containerColor = containerColor,
                            primaryColor = primaryColor,
                            onClick = onNavigateToMissal,
                            viewModel = viewModel,
                        )

                        Spacer(Modifier.height(VerbumSpacing.md))

                        // ── Daily Prayer and Continue Reading share a row ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    onClick = {
                                        onNavigateToReader(
                                            readingState.bookId,
                                            readingState.chapter,
                                            readingState.lastVerse,
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        Spacer(Modifier.height(VerbumSpacing.lg))
                    }
                }
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
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = "Verbum Dei",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
            TopBarIconButton(icon = Icons.Filled.CalendarMonth, onClick = onNavigateToCalendar)
            TopBarIconButton(icon = Icons.Filled.Person, onClick = onNavigateToProfile)
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(38.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                CircleShape,
            )
            .border(BorderStroke(1.dp, primary.copy(alpha = 0.08f)), CircleShape)
            .clickableNoRipple(interactionSource, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = primary,
        )
    }
}

// MARK: - Hero Card (Season + Verse of the Day combined)
@Composable
private fun HeroCard(
    season: LiturgicalSeason,
    primaryColor: Color,
    containerColor: Color,
    surfaceColor: Color,
    viewModel: HomeViewModel,
) {
    // Static radial gradient — matches iOS's fixed RadialGradient
    // (primary 0.16 → primaryContainer → surface), no pulsing animation.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.07f),
                spotColor = Color.Black.copy(alpha = 0.07f),
            )
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.16f),
                            containerColor,
                            surfaceColor,
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.05f),
                        radius = size.maxDimension * 0.9f,
                    ),
                )
            }
            .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f)), RoundedCornerShape(22.dp)),
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
                        .background(primaryColor.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = viewModel.seasonEmoji(season),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
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

            // Verse of the day, editorial pull-quote treatment
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(primaryColor, RoundedCornerShape(50)),
                    )
                    Text(
                        text = "VERSE OF THE DAY",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                        color = primaryColor,
                    )
                }

                Text(
                    text = "\u201C${viewModel.verseOfDay.text}\u201D",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = CrimsonTextFamily,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 28.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "\u2014 ${viewModel.verseOfDay.reference}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = primaryColor,
                )
            }
        }
    }
}

// MARK: - Today's Mass (promoted, primary feature card)
@Composable
private fun TodaysMassCard(
    containerColor: Color,
    primaryColor: Color,
    onClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = primaryColor.copy(alpha = 0.14f),
                spotColor = primaryColor.copy(alpha = 0.14f),
            )
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(containerColor, containerColor.copy(alpha = 0.55f)),
                    ),
                )
            }
            .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.12f)), RoundedCornerShape(20.dp))
            .clickableNoRipple(interactionSource, onClick),
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // Matches iOS "building.columns.fill"
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = "TODAY'S MASS",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Distinct icons per reading, matching iOS (book.closed.fill vs text.book.closed.fill)
                MassReadingRow(icon = Icons.Outlined.MenuBook, text = viewModel.todaysMass.firstReading)
                MassReadingRow(icon = Icons.Outlined.AutoStories, text = viewModel.todaysMass.gospel)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(132.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f)),
                RoundedCornerShape(18.dp),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.sm + 2.dp),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                // Closest available Material icon to iOS "hands.clap.fill"
                Icon(
                    imageVector = Icons.Outlined.PanTool,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "PRAYER",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(132.dp)
            .graphicsLayer {
                alpha = if (isPressed) 0.92f else 1f
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f)),
                RoundedCornerShape(18.dp),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(VerbumSpacing.sm + 2.dp),
            verticalArrangement = Arrangement.spacedBy(VerbumSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "CONTINUE READING",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                minLines = 1,
            )
            Text(
                text = "$bookName $chapter:$lastVerse",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CrimsonTextFamily),
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

// Small helper: clickable without the default ripple, so the scale/alpha
// press effect reads the same as iOS's PressableStyle instead of double-feedback.
@Composable
private fun Modifier.clickableNoRipple(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = this.then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
)

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
