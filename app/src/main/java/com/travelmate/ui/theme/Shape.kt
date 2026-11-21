package com.travelmate.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corners (plan Section 10.5): chips 12 dp, cards 20 dp, sheets 28 dp top, buttons and pills fully rounded. */
object TMShapes {
    val chip = RoundedCornerShape(12.dp)
    val field = RoundedCornerShape(16.dp)
    val card = RoundedCornerShape(20.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val pill = CircleShape
}

val TravelMateShapes = Shapes(
    small = TMShapes.chip,
    medium = TMShapes.card,
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
