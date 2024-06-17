package com.example.fridgeapp.data.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fridgeapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel for the authentication-related fragments (My profile / signin / signup).
 *
 */
class AuthenticationViewmodel (private val authRep: AuthRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    init {
        _currentUser.value = authRep.currentUser()
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRep.signIn(email, password) { result ->
                result.onSuccess {
                    onSuccess()
                }
                result.onFailure {
                    onFailure(it as Exception) // Ensure it is cast to Exception
                }
            }
        }
    }

    fun signUp(email: String, password: String, name: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRep.signUp(email, password, name) { result ->
                result.onSuccess {
                    viewModelScope.launch {
                        authRep.saveUserToDatabase(name) { result ->
                            result.onSuccess {
                                onSuccess()
                            }
                            result.onFailure {
                                onFailure(it as Exception) // Ensure it is cast to Exception
                            }
                        }
                    }
                }
                result.onFailure {
                    onFailure(it as Exception) // Ensure it is cast to Exception
                }
            }
        }
    }

    fun signOut() {
        authRep.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return authRep.isUserLoggedIn()
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            authRep.sendPasswordResetEmail(email, onSuccess, onFailure)
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            val result = authRep.changePassword(oldPassword, newPassword)
            result.onSuccess {
                onSuccess()
            }
            result.onFailure {
                onFailure(it as Exception) // Ensure it is cast to Exception
            }
        }
    }


    fun getCurrentUserName(onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val userName = authRep.currentUserName()
                onSuccess(userName)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    class AuthenticationViewmodelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthenticationViewmodel(repo) as T
        }
    }


}