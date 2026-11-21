package com.travelmate.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * App frame: the bottom bar is shown only on Explore, Saved and Profile.
 * [content] receives the padding the tab screens must leave for the bottom bar.
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = TopLevelTab.entries.firstOrNull { it.screen.route == currentRoute }
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = selectedTab != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                BottomBar(selectedTab = selectedTab, onSelect = { navController.navigateToTab(it) })
            }
        },
        content = content,
    )
}

/** Tab switch that keeps each tab's state (scroll position) and never stacks tabs. */
fun NavController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.screen.route) {
        popUpTo(Screen.Explore.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
