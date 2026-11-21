package com.travelmate.ui.screens.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.travelmate.ui.components.ImageScrim
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Shared frame of the Login and Sign Up screens: hero area on top (28 percent of the height),
 * then the form. Scrolls and moves above the keyboard on small screens.
 */
@Composable
fun AuthScaffold(
    snackbarHostState: SnackbarHostState,
    content: @Composable ColumnScope.() -> Unit,
) {
    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
        ) { padding ->
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                val heroHeight = maxHeight * 0.28f
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .navigationBarsPadding(),
                ) {
                    AuthHero(Modifier.fillMaxWidth().height(heroHeight))
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.gutter),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        content = content,
                    )
                    Spacer(Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

/** Brand name, screen title and one line of explanation. */
@Composable
fun AuthHeading(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    Column(modifier.padding(bottom = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text("TravelMate", style = MaterialTheme.typography.labelLarge, color = colors.primary)
        Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}

/** "Don't have an account? Sign up" style link under the main button. */
@Composable
fun AuthSwitchLink(question: String, action: String, onClick: () -> Unit, enabled: Boolean = true) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(question, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        TextButton(onClick = onClick, enabled = enabled) { Text(action, color = colors.secondary) }
    }
}

/**
 * Top of the auth screens. Shows `res/drawable-nodpi/auth_hero.webp` when the team adds it;
 * until then the brand mark on the ambient background.
 */
@SuppressLint("DiscouragedApi")
@Composable
fun AuthHero(modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    val context = LocalContext.current
    val heroId = remember { context.resources.getIdentifier("auth_hero", "drawable", context.packageName) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (heroId != 0) {
            Image(
                painter = painterResource(heroId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            ImageScrim(Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(colors.accentGlow, CircleShape)
                    .border(1.dp, colors.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.TravelExplore, contentDescription = null, tint = colors.primary, modifier = Modifier.size(48.dp))
            }
        }
    }
}
