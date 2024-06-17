package com.example.fridgeapp.data.repository

import com.google.firebase.auth.FirebaseUser

/**
 * Interface for authentication-related operations.
 */
interface AuthRepository {

    suspend fun signIn(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit)
    suspend fun signUp(email: String, password: String, name: String, onComplete: (Result<FirebaseUser>) -> Unit)
    fun signOut()
    fun isUserLoggedIn(): Boolean
    suspend fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    fun currentUser(): FirebaseUser?
    suspend fun currentUserName(): String?
    suspend fun saveUserToDatabase(name: String, onComplete: (Result<Unit>) -> Unit)

}