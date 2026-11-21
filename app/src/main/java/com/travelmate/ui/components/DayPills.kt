package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme
import java.time.LocalDate

/** Horizontal day switcher: "Day 1" over "Thu 12"; the selected day has the accent glow. */
@Composable
fun DayPills(
    days: Int,
    startDate: LocalDate,
    selectedDay: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDay) {
        listState.animateScrollToItem((selectedDay - 1).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(days) { index ->
            val day = index + 1
            val selected = day == selectedDay
            Column(
                modifier = Modifier
                    .widthIn(min = 64.dp)
                    .clip(TMShapes.field)
                    .background(if (selected) colors.accentGlow else colors.surfaceGlass)
                    .border(1.dp, if (selected) colors.primary else colors.outlineGlass, TMShapes.field)
                    .selectable(selected = selected, role = Role.Tab) { onSelect(day) }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Day $day",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) colors.primary else colors.textPrimary,
                )
                Text(
                    text = Formatters.weekdayDay(startDate.plusDays(index.toLong())),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DayPillsPreview() = PreviewSurface {
    DayPills(days = 5, startDate = LocalDate.of(2026, 11, 12), selectedDay = 2, onSelect = {})
}
