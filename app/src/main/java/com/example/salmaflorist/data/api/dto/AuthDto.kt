package com.example.salmaflorist.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body untuk login
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Request body untuk register
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Response dari login/register - berisi user data dan JWT token
 */
data class AuthResponse(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("token")
    val token: String
)

/**
 * Response dari verify token
 */
data class VerifyResponse(
    @SerializedName("user")
    val user: UserDto
)

/**
 * Data user dari API
 */
data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Standard error response dari API
 */
data class ErrorResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("errors")
    val errors: Map<String, String>? = null
)

/**
 * Wrapper untuk API response yang mungkin gagal
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val errors: Map<String, String>? = null,
        val statusCode: Int? = null
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
