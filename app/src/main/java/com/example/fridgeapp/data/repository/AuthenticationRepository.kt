package com.example.fridgeapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await

class AuthenticationRepository {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    init {
        _currentUser.value = firebaseAuth.currentUser
    }

    fun signIn(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = firebaseAuth.currentUser
                    onComplete(Result.success(firebaseAuth.currentUser!!))
                } else {
                    onComplete(Result.failure(Exception(task.exception?.message ?: "Unknown error")))
                }
            }
    }

    fun signUp(email: String, password: String, name: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = firebaseAuth.currentUser
                    onComplete(Result.success(firebaseAuth.currentUser!!))
                } else {
                    onComplete(Result.failure(Exception(task.exception?.message ?: "Unknown error")))
                }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(Exception(task.exception?.message ?: "Failed to send reset email"))
                }
            }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
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
}
