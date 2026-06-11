package com.example.salmaflorist.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String = "user" // "admin" atau "user"
)