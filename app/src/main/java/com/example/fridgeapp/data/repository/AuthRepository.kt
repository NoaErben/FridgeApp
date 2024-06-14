package com.example.fridgeapp.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

interface AuthRepository {

    fun signIn(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit)
    fun signUp(email: String, password: String, name: String, onComplete: (Result<FirebaseUser>) -> Unit)
    fun signOut()
    fun isUserLoggedIn(): Boolean
    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    fun currentUser(): FirebaseUser?
}