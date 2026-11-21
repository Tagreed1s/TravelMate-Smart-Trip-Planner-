package com.travelmate.core

/**
 * Turns an exception into text for the user.
 * Shows the exception's own message for now; friendly Firebase and network messages are planned for the next phase.
 */
object ErrorMessages {
    fun userMessage(t: Throwable): String = t.localizedMessage ?: "Something went wrong"
}
