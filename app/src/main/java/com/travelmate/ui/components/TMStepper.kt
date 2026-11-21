package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme
import kotlinx.coroutines.delay

/** "-" value "+" in one pill. Holding a button repeats the step. */
@Composable
fun TMStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    valueLabel: String = "",
) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(TMShapes.pill)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.outlineGlass, TMShapes.pill),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        RepeatingIconButton(
            icon = Icons.Rounded.Remove,
            contentDescription = "Decrease $valueLabel".trim(),
            enabled = value > range.first,
            onClick = { onValueChange((value - 1).coerceIn(range)) },
        )
        Text(
            text = value.toString(),
            style = NumberStyle,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp),
        )
        RepeatingIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Increase $valueLabel".trim(),
            enabled = value < range.last,
            onClick = { onValueChange((value + 1).coerceIn(range)) },
        )
    }
}

@Composable
private fun RepeatingIconButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = TravelMateTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val currentOnClick by rememberUpdatedState(onClick)
    val currentEnabled by rememberUpdatedState(enabled)
    // Set while a long press is repeating, so the click on release does not add one more step.
    var repeated by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            repeated = false
            delay(400)
            while (currentEnabled) {
                repeated = true
                currentOnClick()
                delay(80)
            }
        }
    }

    IconButton(
        onClick = {
            if (!repeated) onClick()
            repeated = false
        },
        enabled = enabled,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = colors.textPrimary,
            disabledContentColor = colors.textSecondary.copy(alpha = 0.38f),
        ),
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@ThemePreviews
@Composable
private fun TMStepperPreview() = PreviewSurface {
    TMStepper(value = 3, onValueChange = {}, range = 1..14, valueLabel = "days")
}
