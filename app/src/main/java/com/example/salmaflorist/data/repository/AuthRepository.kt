package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.AuthResponse
import com.example.salmaflorist.data.api.dto.ErrorResponse
import com.example.salmaflorist.data.api.dto.LoginRequest
import com.example.salmaflorist.data.api.dto.RegisterRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk autentikasi
 * Menangani komunikasi dengan API dan error handling
 */
class AuthRepository {

    private val TAG = "AuthRepository"
    private val apiService = ApiConfig.getApiService()
    private val gson = Gson()

    /**
     * Login user dengan error handling lengkap
     * @paramEmail Email user
     * @paramPassword Password user
     * @return ApiResult<AuthResponse> berisi data user dan token
     */
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        Log.d(TAG, "Attempting login for email: $email")

        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Making login API call...")

                val response = apiService.login(LoginRequest(email = email, password = password))

                Log.d(TAG, "Login response code: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val authResponse = response.body()!!
                        Log.d(TAG, "Login successful for user: ${authResponse.user.username}, role: ${authResponse.user.role}")
                        ApiResult.Success(authResponse)
                    }
                    response.code() == 401 -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.w(TAG, "Login failed: Invalid credentials - ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                    response.code() == 400 -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.w(TAG, "Login failed: Validation error - ${error.message}, errors: ${error.errors}")
                        ApiResult.Error(
                            message = error.message,
                            errors = error.errors,
                            statusCode = response.code()
                        )
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.e(TAG, "Login failed: Server error ${response.code()} - ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Login failed: Network error - ${e.message}", e)
            ApiResult.Error(
                message = "Koneksi internet bermasalah. Periksa koneksi Anda dan coba lagi.",
                statusCode = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: Unexpected error - ${e.message}", e)
            ApiResult.Error(
                message = "Terjadi kesalahan tak terduga. Silakan coba lagi.",
                statusCode = null
            )
        }
    }

    /**
     * Register user baru dengan error handling lengkap
     * @paramUsername Username user
     * @paramEmail Email user
     * @paramPassword Password user
     * @return ApiResult<AuthResponse> berisi data user dan token
     */
    suspend fun register(username: String, email: String, password: String): ApiResult<AuthResponse> {
        Log.d(TAG, "Attempting register for username: $username, email: $email")

        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Making register API call...")

                val response = apiService.register(RegisterRequest(username = username, email = email, password = password))

                Log.d(TAG, "Register response code: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val authResponse = response.body()!!
                        Log.d(TAG, "Register successful for user: ${authResponse.user.username}")
                        ApiResult.Success(authResponse)
                    }
                    response.code() == 400 -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.w(TAG, "Register failed: Validation error - ${error.message}, errors: ${error.errors}")
                        ApiResult.Error(
                            message = error.message,
                            errors = error.errors,
                            statusCode = response.code()
                        )
                    }
                    response.code() == 409 -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.w(TAG, "Register failed: Email already exists - ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val error = parseError(errorBody)
                        Log.e(TAG, "Register failed: Server error ${response.code()} - ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Register failed: Network error - ${e.message}", e)
            ApiResult.Error(
                message = "Koneksi internet bermasalah. Periksa koneksi Anda dan coba lagi.",
                statusCode = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Register failed: Unexpected error - ${e.message}", e)
            ApiResult.Error(
                message = "Terjadi kesalahan tak terduga. Silakan coba lagi.",
                statusCode = null
            )
        }
    }

    /**
     * Parse error body dari response API
     * @paramJsonString Raw JSON error response
     * @return ErrorResponse object
     */
    private fun parseError(jsonString: String?): ErrorResponse {
        return try {
            if (jsonString.isNullOrBlank()) {
                ErrorResponse("Terjadi kesalahan pada server")
            } else {
                gson.fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse error response: ${e.message}")
            ErrorResponse("Terjadi kesalahan pada server")
        }
    }

    /**
     * Mendapatkan pesan error yang user-friendly
     * @paramError ApiResult.Error object
     * @return String pesan error
     */
    fun getUserFriendlyMessage(error: ApiResult.Error): String {
        val message = when (error.statusCode) {
            400 -> "Data yang dimasukkan tidak valid"
            401 -> "Email atau password salah"
            409 -> "Email sudah terdaftar"
            500 -> "Server sedang mengalami gangguan"
            else -> error.message
        }

        // Tambahkan detail error field jika ada
        val fieldErrors = error.errors?.values?.joinToString(", ")
        return if (!fieldErrors.isNullOrEmpty()) {
            "$message: $fieldErrors"
        } else {
            message
        }
    }
}

/**
 * Companion object untuk singleton pattern
 */
object AuthRepositoryProvider {
    val instance: AuthRepository by lazy { AuthRepository() }
}
