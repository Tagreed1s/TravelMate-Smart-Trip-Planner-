package com.travelmate.domain.model

data class User(val uid: String, val name: String, val email: String) {
    val firstName: String get() = name.trim().substringBefore(' ')

    /** Up to two initials for the avatar, for example "Dana Saleh" -> "DS". */
    val initials: String
        get() = name.trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}
