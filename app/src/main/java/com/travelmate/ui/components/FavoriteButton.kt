package com.travelmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Heart button used by DestinationCard, the destination hero and the place sheet.
 * Draws nothing unless both values are given (for example in previews).
 */
@Composable
fun FavoriteSlot(
    isFavorite: Boolean?,
    onToggleFavorite: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (isFavorite == null || onToggleFavorite == null) return
    GlassIconButton(
        icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
        onClick = onToggleFavorite,
        modifier = modifier,
    )
}
