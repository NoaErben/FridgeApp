package com.example.fridgeapp.data.model

/**
 * User is a data class that represents a user entity with three optional properties: name, email, and uid.
 */
data class User(
    val name: String? = null,
    val email: String? = null,
    val uid: String? = null
)
