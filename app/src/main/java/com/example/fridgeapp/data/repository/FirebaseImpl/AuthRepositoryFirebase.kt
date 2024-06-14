package com.example.fridgeapp.data.repository.FirebaseImpl

import com.example.fridgeapp.data.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepositoryFirebase : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun signIn(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        // TODO: make suspend
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.success(firebaseAuth.currentUser!!))
                } else {
                    onComplete(Result.failure(Exception(task.exception?.message ?: "Unknown error")))
                }
            }
    }

    override fun signUp(email: String, password: String, name: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        // TODO: make suspend
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.success(firebaseAuth.currentUser!!))
                } else {
                    onComplete(Result.failure(Exception(task.exception?.message ?: "Unknown error")))
                }
            }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(Exception(task.exception?.message ?: "Failed to send reset email"))
                }
            }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No authenticated user found"))
        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        return try {
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun currentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

}