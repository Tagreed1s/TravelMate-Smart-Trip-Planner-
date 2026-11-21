package com.travelmate.ui.screens.auth

import android.util.Patterns

/** Input rules shared by Login, Sign Up and Forgot Password. */
object AuthValidation {
    const val MIN_PASSWORD = 6
    const val MAX_NAME = 60

    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}
