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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class)
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
) {
    val season = LocalLiturgicalSeason.current
    val primaryColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        animationSpec = tween(800),
        label = "primary_anim",
    )
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(800),
        label = "container_anim",
    )

    // Staggered entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Top Bar ──
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Verbum Dei",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = "The Word of God",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateToCalendar) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Liturgical Calendar",
                        tint = primaryColor,
                    )
                }
                IconButton(onClick = onNavigateToProfile) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        )

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 },
        ) {
            Column {
                // ── Liturgical Season Hero ──
                SeasonHeroBanner(
                    season = season,
                    containerColor = containerColor,
                    primaryColor = primaryColor,
                )

                Spacer(Modifier.height(VerbumSpacing.lg))

                // ── Verse of the Day ──
                VerseOfDayCard(primaryColor = primaryColor)

                Spacer(Modifier.height(VerbumSpacing.lg))

                // ── Quick Actions Grid ──
                SectionHeader(title = "Quick Actions")
                Spacer(Modifier.height(VerbumSpacing.sm))
                QuickActionsRow(
                    onNavigateToBible = onNavigateToBible,
                    onNavigateToMissal = onNavigateToMissal,
                    onNavigateToPrayer = onNavigateToPrayer,
                    onNavigateToCommunity = onNavigateToCommunity,
                    onNavigateToAiChat = onNavigateToAiChat,
                )

                Spacer(Modifier.height(VerbumSpacing.xl))

                // ── Today's Mass Preview ──
                SectionHeader(title = "Today's Mass")
                Spacer(Modifier.height(VerbumSpacing.sm))
                TodaysMassCard(
                    season = season,
                    containerColor = containerColor,
                    onClick = onNavigateToMissal,
                )

                Spacer(Modifier.height(VerbumSpacing.xl))

                // ── Daily Prayer ──
                SectionHeader(title = "Daily Prayer")
                Spacer(Modifier.height(VerbumSpacing.sm))
                DailyPrayerCard(
                    season = season,
                    onClick = onNavigateToPrayer,
                )

                Spacer(Modifier.height(VerbumSpacing.xl))

                // ── Continue Reading ──
                val readingState = continueReadingState
                if (readingState != null) {
                    SectionHeader(title = "Continue Reading")
                    Spacer(Modifier.height(VerbumSpacing.sm))
                    ContinueReadingCard(
                        bookName = readingState.bookName,
                        chapter = readingState.chapter,
                        lastVerse = readingState.lastVerse,
                        onClick = { onNavigateToReader(readingState.bookId, readingState.chapter, readingState.lastVerse) },
                    )
                }

                Spacer(Modifier.height(VerbumSpacing.xxl + VerbumSpacing.lg))
            }
        }
    }
}

@Composable
private fun SeasonHeroBanner(
    season: LiturgicalSeason,
    containerColor: Color,
    primaryColor: Color,
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding)
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha * 0.3f),
                            containerColor,
                            containerColor.copy(alpha = 0.6f),
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                        radius = size.maxDimension,
                    ),
                )
            }
            .padding(VerbumSpacing.lg + VerbumSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = seasonEmoji(season),
                fontSize = 36.sp,
            )
            Spacer(Modifier.height(VerbumSpacing.sm))
            Text(
                text = season.displayName.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(VerbumSpacing.xs))
            Text(
                text = seasonGreeting(season),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CrimsonTextFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun VerseOfDayCard(primaryColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text(
                    text = "VERSE OF THE DAY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = primaryColor,
                )
            }
            Spacer(Modifier.height(VerbumSpacing.md))
            Text(
                text = "\u201CIn the beginning was the Word, and the Word was with God, and the Word was God.\u201D",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = CrimsonTextFamily,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(VerbumSpacing.sm))
            Text(
                text = "\u2014 John 1:1",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = primaryColor,
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onNavigateToBible: () -> Unit,
    onNavigateToMissal: () -> Unit,
    onNavigateToPrayer: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToAiChat: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = VerbumSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val actions = listOf(
            Triple(Icons.Filled.AutoStories, "Bible", onNavigateToBible),
            Triple(Icons.Filled.Church, "Missal", onNavigateToMissal),
            Triple(Icons.Filled.VolunteerActivism, "Prayer", onNavigateToPrayer),
            Triple(Icons.Filled.People, "Community", onNavigateToCommunity),
            Triple(Icons.Outlined.AutoAwesome, "Verbum AI", onNavigateToAiChat),
        )
        items(actions, key = { it.second }) { (icon, label, onClick) ->
            QuickActionChip(icon = icon, label = label, onClick = onClick)
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun TodaysMassCard(
    season: LiturgicalSeason,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(VerbumSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Wednesday of the 3rd Week of Easter",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(VerbumSpacing.xs))
                Text(
                    text = "First Reading: Acts 8:1b-8",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Text(
                    text = "Gospel: John 6:35-40",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun DailyPrayerCard(
    season: LiturgicalSeason,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(VerbumSpacing.sm))
                Text(
                    text = "Suggested Prayer",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(VerbumSpacing.sm))
            Text(
                text = seasonPrayer(season),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CrimsonTextFamily,
                    lineHeight = 24.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(
    bookName: String,
    chapter: Int,
    lastVerse: Int,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerbumSpacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(VerbumSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(VerbumSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$bookName $chapter",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Continue from verse $lastVerse",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = VerbumSpacing.screenPadding),
    )
}

private fun seasonEmoji(season: LiturgicalSeason): String = when (season) {
    LiturgicalSeason.ADVENT -> "\uD83D\uDD6F\uFE0F"
    LiturgicalSeason.CHRISTMAS -> "\u2B50"
    LiturgicalSeason.LENT -> "\u271D\uFE0F"
    LiturgicalSeason.EASTER -> "\uD83C\uDF1E"
    LiturgicalSeason.PENTECOST -> "\uD83D\uDD25"
    LiturgicalSeason.ORDINARY_TIME -> "\uD83C\uDF3F"
}

private fun seasonGreeting(season: LiturgicalSeason): String = when (season) {
    LiturgicalSeason.ADVENT -> "Prepare the way of the Lord"
    LiturgicalSeason.CHRISTMAS -> "The Word became flesh and dwelt among us"
    LiturgicalSeason.LENT -> "Return to the Lord with all your heart"
    LiturgicalSeason.EASTER -> "He is risen! Alleluia!"
    LiturgicalSeason.PENTECOST -> "Come, Holy Spirit, fill the hearts of your faithful"
    LiturgicalSeason.ORDINARY_TIME -> "Grow in the grace and knowledge of our Lord"
}

private fun seasonPrayer(season: LiturgicalSeason): String = when (season) {
    LiturgicalSeason.ADVENT -> "Come, Lord Jesus. Fill our hearts with hope as we await your coming."
    LiturgicalSeason.CHRISTMAS -> "O God, who wonderfully created human dignity and still more wonderfully restored it, grant that we may share in the divinity of Christ."
    LiturgicalSeason.LENT -> "Lord, grant us the grace of true repentance. Help us turn our hearts back to You."
    LiturgicalSeason.EASTER -> "God of life, through the resurrection of your Son, you have filled us with joy. Help us be witnesses of your love."
    LiturgicalSeason.PENTECOST -> "Come, Holy Spirit, enkindle in us the fire of your love. Send forth your Spirit and renew the face of the earth."
    LiturgicalSeason.ORDINARY_TIME -> "Lord, guide our steps today. May we grow closer to You in all that we do."
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
