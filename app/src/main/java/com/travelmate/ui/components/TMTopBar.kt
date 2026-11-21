package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Transparent top bar: optional back button, headlineSmall title, optional subtitle and avatar. */
@Composable
fun TMTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    avatarInitials: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (onBack != null) {
            GlassIconButton(icon = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", onClick = onBack)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
        if (avatarInitials != null) {
            Avatar(initials = avatarInitials, onClick = onAvatarClick)
        }
    }
}

/** Initials in a glowing circle. */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    onClick: (() -> Unit)? = null,
) {
    val colors = TravelMateTheme.colors
    val clickModifier = if (onClick != null) {
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Profile" }
    } else {
        Modifier
    }
    Box(modifier = modifier.then(clickModifier), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .background(colors.accentGlow, CircleShape)
                .border(1.dp, colors.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = if (size > 48.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelLarge,
                color = colors.primary,
            )
        }
    }
}

/** 40 dp glass circle with an icon, 48 dp touch target. Used over images and in top bars. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.surfaceGlass, CircleShape)
                .border(1.dp, colors.outlineGlass, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun TMTopBarPreview() = PreviewSurface {
    Column {
        TMTopBar(title = "Hi, Dana", avatarInitials = "DS", onAvatarClick = {})
        TMTopBar(title = "Weekend Getaway", subtitle = "12 - 16 Nov · 2 travelers", onBack = {})
    }
}
