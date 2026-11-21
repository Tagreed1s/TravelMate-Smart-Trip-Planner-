package com.travelmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Outlined glass text field with a leading icon and the error text under it. */
@Composable
fun TMTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    error: String? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = TravelMateTheme.colors
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val passwordToggle: @Composable () -> Unit = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
            )
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = if (isPassword) passwordToggle else trailingIcon,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = TMShapes.field,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceGlass,
            unfocusedContainerColor = colors.surfaceGlass,
            disabledContainerColor = colors.surfaceGlass,
            errorContainerColor = colors.surfaceGlass,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outlineGlass,
            focusedLeadingIconColor = colors.primary,
            unfocusedLeadingIconColor = colors.textSecondary,
            unfocusedTrailingIconColor = colors.textSecondary,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.textSecondary,
            focusedPlaceholderColor = colors.textSecondary,
            unfocusedPlaceholderColor = colors.textSecondary,
            cursorColor = colors.primary,
        ),
    )
}

@ThemePreviews
@Composable
private fun TMTextFieldPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        TMTextField(value = "dana@example.com", onValueChange = {}, label = "Email", leadingIcon = Icons.Rounded.Email)
        TMTextField(
            value = "secret",
            onValueChange = {},
            label = "Password",
            leadingIcon = Icons.Rounded.Lock,
            isPassword = true,
            error = "At least 6 characters",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
    }
}
