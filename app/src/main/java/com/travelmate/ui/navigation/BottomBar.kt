package com.travelmate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** The three bottom bar tabs (instructor's change request: Explore, Saved, Profile). */
enum class TopLevelTab(val screen: Screen, val label: String, val icon: ImageVector) {
    EXPLORE(Screen.Explore, "Explore", Icons.Rounded.TravelExplore),
    SAVED(Screen.Saved, "Saved", Icons.Rounded.Bookmark),
    PROFILE(Screen.Profile, "Profile", Icons.Rounded.Person),
}

/** Glass bottom bar with the three tabs. */
@Composable
fun BottomBar(
    selectedTab: TopLevelTab?,
    onSelect: (TopLevelTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    NavigationBar(
        modifier = modifier,
        containerColor = colors.surfaceGlass,
        contentColor = colors.textSecondary,
        tonalElevation = 0.dp,
    ) {
        TopLevelTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.accentGlow,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun BottomBarPreview() = PreviewSurface {
    BottomBar(selectedTab = TopLevelTab.EXPLORE, onSelect = {})
}
