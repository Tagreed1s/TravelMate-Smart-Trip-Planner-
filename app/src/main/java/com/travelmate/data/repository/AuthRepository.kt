package com.travelmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.travelmate.data.firebase.FirebasePaths
import com.travelmate.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    /** The signed-in user, or null. Driven by FirebaseAuth.AuthStateListener. */
    val currentUser: StateFlow<User?>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
) : AuthRepository {

    private val _currentUser = MutableStateFlow(auth.currentUser?.toUser())
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { _currentUser.value = it.currentUser?.toUser() }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Sign up failed")
        val cleanName = name.trim()
        user.updateProfile(userProfileChangeRequest { displayName = cleanName }).await()
        // The auth listener fired before the name was set, so publish the user again with the name.
        _currentUser.value = User(uid = user.uid, name = cleanName, email = user.email.orEmpty())
        database.getReference(FirebasePaths.profile(user.uid)).setValue(
            mapOf(
                "name" to cleanName,
                "email" to user.email.orEmpty(),
                "createdAt" to ServerValue.TIMESTAMP,
            ),
        ).await()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override fun signOut() = auth.signOut()

    /** Name = display name, falling back to the part of the email before "@". */
    private fun FirebaseUser.toUser(): User {
        val mail = email.orEmpty()
        val fallback = mail.substringBefore("@").ifBlank { "Traveler" }
        return User(uid = uid, name = displayName?.takeIf { it.isNotBlank() } ?: fallback, email = mail)
    }
}
