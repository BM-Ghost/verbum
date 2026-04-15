package com.verbum.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verbum.core.common.model.LiturgicalSeason
import com.verbum.core.ui.components.VerbumLoadingState
import com.verbum.core.ui.theme.VerbumSpacing
import com.verbum.core.ui.theme.VerbumScreenPreviews
import com.verbum.core.ui.theme.VerbumTheme
import com.verbum.feature.calendar.domain.model.CelebrationRank
import com.verbum.feature.calendar.domain.model.LiturgicalColor
import com.verbum.feature.calendar.domain.model.LiturgicalDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun LiturgicalCalendarScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDaySelected = viewModel::selectDay,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    onNavigateBack: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liturgical Calendar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        when (uiState) {
            is CalendarUiState.Loading -> {
                VerbumLoadingState(modifier = Modifier.padding(padding))
            }

            is CalendarUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(VerbumSpacing.md),
                ) {
                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onPreviousMonth) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                        }
                        Text(
                            text = "${uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${uiState.currentMonth.year}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = onNextMonth) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }

                    Spacer(Modifier.height(VerbumSpacing.sm))

                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DayOfWeek.entries.forEach { dow ->
                            Text(
                                text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(VerbumSpacing.xs))

                    // Calendar grid
                    val firstDayOffset = uiState.days.first().date.dayOfWeek.value % 7
                    val gridItems = buildList {
                        repeat(firstDayOffset) { add(null) }
                        addAll(uiState.days)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((gridItems.size / 7 + 1) * 48).dp),
                        userScrollEnabled = false,
                    ) {
                        items(gridItems) { day ->
                            if (day != null) {
                                CalendarDayCell(
                                    day = day,
                                    isSelected = uiState.selectedDay?.date == day.date,
                                    isToday = day.date == LocalDate.now(),
                                    onClick = { onDaySelected(day.date) },
                                )
                            } else {
                                Box(modifier = Modifier.aspectRatio(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(VerbumSpacing.lg))

                    // Selected day details
                    uiState.selectedDay?.let { day ->
                        DayDetailCard(day = day)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: LiturgicalDay,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${day.date.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            )
            // Liturgical color dot
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(day.liturgicalColor.toComposeColor()),
            )
        }
    }
}

@Composable
private fun DayDetailCard(day: LiturgicalDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(VerbumSpacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(VerbumSpacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(day.liturgicalColor.toComposeColor()),
                )
                Text(
                    text = day.liturgicalColor.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(VerbumSpacing.sm))

            Text(
                text = day.celebration,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "${day.season.displayName} · ${day.rank.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            day.saintOfDay?.let { saint ->
                Spacer(Modifier.height(VerbumSpacing.sm))
                Text(
                    text = "🕊️ $saint",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LiturgicalColor.toComposeColor(): Color = when (this) {
    LiturgicalColor.GREEN -> MaterialTheme.colorScheme.primary
    LiturgicalColor.VIOLET -> MaterialTheme.colorScheme.secondary
    LiturgicalColor.WHITE -> MaterialTheme.colorScheme.surface
    LiturgicalColor.RED -> MaterialTheme.colorScheme.error
    LiturgicalColor.ROSE -> MaterialTheme.colorScheme.tertiary
    LiturgicalColor.BLACK -> MaterialTheme.colorScheme.onSurface
    LiturgicalColor.GOLD -> MaterialTheme.colorScheme.primaryContainer
}

@Preview(showBackground = true)
@Composable
private fun LiturgicalCalendarPreview() {
    val sampleDays = (1..30).map { dayOfMonth ->
        LiturgicalDay(
            date = LocalDate.of(2026, 4, dayOfMonth),
            season = LiturgicalSeason.EASTER,
            celebration = if (dayOfMonth == 12) "Easter Sunday" else "Easter Weekday",
            rank = if (dayOfMonth == 12) CelebrationRank.SUNDAY else CelebrationRank.WEEKDAY,
            liturgicalColor = if (dayOfMonth == 12) LiturgicalColor.GOLD else LiturgicalColor.WHITE,
            saintOfDay = if (dayOfMonth == 23) "St. George" else null,
        )
    }

    VerbumScreenPreviews { season, darkTheme ->
        VerbumTheme(liturgicalSeason = season, darkTheme = darkTheme) {
            CalendarContent(
                uiState = CalendarUiState.Success(
                    currentMonth = YearMonth.of(2026, 4),
                    days = sampleDays,
                    selectedDay = sampleDays.first(),
                    today = sampleDays[14],
                ),
                onNavigateBack = {},
                onDaySelected = {},
                onPreviousMonth = {},
                onNextMonth = {},
            )
        }
    }
}
