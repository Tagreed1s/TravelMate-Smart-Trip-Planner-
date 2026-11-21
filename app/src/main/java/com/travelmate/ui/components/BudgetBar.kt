package com.travelmate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.BudgetLevel
import com.travelmate.domain.model.budgetLevel
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateColors
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Pinned 72 dp glass bar with the trip total (animated) over the budget, and a 4 dp progress line in
 * success / warning / error color. The whole bar opens the cost breakdown.
 */
@Composable
fun BudgetBar(
    totalSar: Int,
    budgetSar: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    val animatedTotal by animateIntAsState(totalSar, tween(300, easing = FastOutSlowInEasing), label = "total")
    val levelColor by animateColorAsState(colors.forLevel(budgetLevel(totalSar, budgetSar)), label = "level")
    val fraction by animateFloatAsState(
        targetValue = if (budgetSar > 0) (totalSar.toFloat() / budgetSar).coerceIn(0f, 1f) else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fraction",
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(TMShapes.card)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.outlineGlass, TMShapes.card)
            .clickable(role = Role.Button, onClickLabel = "Show cost breakdown", onClick = onClick),
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(Modifier.weight(1f)) {
                Text(Formatters.money(animatedTotal), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text("of ${Formatters.money(budgetSar)}", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null, tint = colors.textSecondary)
        }
        Box(Modifier.fillMaxWidth().height(4.dp).background(colors.outlineGlass)) {
            Box(Modifier.fillMaxWidth(fraction).height(4.dp).background(levelColor))
        }
    }
}

fun TravelMateColors.forLevel(level: BudgetLevel): Color = when (level) {
    BudgetLevel.WITHIN -> success
    BudgetLevel.NEAR_LIMIT -> warning
    BudgetLevel.OVER -> error
}

@ThemePreviews
@Composable
private fun BudgetBarPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        BudgetBar(totalSar = 2_000, budgetSar = 3_000, onClick = {})
        BudgetBar(totalSar = 2_800, budgetSar = 3_000, onClick = {})
        BudgetBar(totalSar = 3_400, budgetSar = 3_000, onClick = {})
    }
}
