package com.example.salmaflorist.data.api

import com.example.salmaflorist.data.api.dto.AuthResponse
import com.example.salmaflorist.data.api.dto.LoginRequest
import com.example.salmaflorist.data.api.dto.RegisterRequest
import com.example.salmaflorist.data.api.dto.VerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * REST API Service untuk endpoint autentikasi
 * Menggunakan Retrofit untuk HTTP requests
 */
interface ApiService {

    /**
     * Login user
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    /**
     * Register user baru
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    /**
     * Verifikasi token JWT
     * GET /auth/verify
     * Membutuhkan header Authorization: Bearer <token>
     */
    @GET("auth/verify")
    suspend fun verifyToken(): Response<VerifyResponse>
}
