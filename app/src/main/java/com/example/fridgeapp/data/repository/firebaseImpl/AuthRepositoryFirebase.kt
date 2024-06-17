package com.example.fridgeapp.data.repository.firebaseImpl

import com.example.fridgeapp.data.model.User
import com.example.fridgeapp.data.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [AuthRepository] using Firebase for authentication and user data management.
 */
class AuthRepositoryFirebase : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")


    override suspend fun signIn(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.success(firebaseAuth.currentUser!!))
                } else {
                    onComplete(Result.failure(Exception(task.exception?.message ?: "Unknown error")))
                }
            }
    }

    override suspend fun signUp(email: String, password: String, name: String, onComplete: (Result<FirebaseUser>) -> Unit) {
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

    override suspend fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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

    override suspend fun currentUserName(): String? {
        return try {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val snapshot = userDatabaseReference.child(uid).get().await()
                snapshot.child("name").value as? String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveUserToDatabase(name: String, onComplete: (Result<Unit>) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val email = currentUser.email ?: ""

            val user = User(name, email, uid)

            userDatabaseReference.child(uid).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(Result.success(Unit))
                    } else {
                        onComplete(Result.failure(Exception("Failed to save user to database")))
                    }
                }
        } else {
            onComplete(Result.failure(Exception("No authenticated user found")))
        }
    }

}