package com.example.salmaflorist.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Konfigurasi API Client menggunakan Retrofit dan OkHttp
 * Menyediakan logging, auth token interceptor, dan error handling
 */
object ApiConfig {

    private const val TAG = "ApiConfig"
    // Base URL API tanpa /api prefix karena endpoint sudah includes path
    private const val BASE_URL = "https://salmaflorist-api.vercel.app/"

    /**
     * Logging interceptor untuk debugging
     * Mencatat request dan response detail
     */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Auth token interceptor untuk menambahkan JWT token ke setiap request
     */
    private fun createAuthInterceptor(tokenProvider: (() -> String?)? = null): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Tambahkan header Content-Type
            requestBuilder.addHeader("Content-Type", "application/json")

            // Tambahkan header Authorization jika token tersedia
            tokenProvider?.let { provider ->
                val token = provider()
                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "Adding auth token to request: ${originalRequest.url}")
                }
            }

            val request = requestBuilder.build()
            Log.d(TAG, "Making request: ${request.method} ${request.url}")

            try {
                val response = chain.proceed(request)

                // Log response
                Log.d(TAG, "Response: ${response.code} ${response.message} for ${request.url}")

                if (!response.isSuccessful) {
                    val errorBody = response.peekBody(Long.MAX_VALUE).string()
                    Log.e(TAG, "Error response body: $errorBody")
                }

                response
            } catch (e: Exception) {
                Log.e(TAG, "Request failed: ${e.message}", e)
                throw e
            }
        }
    }

    /**
     * Membuat OkHttpClient dengan konfigurasi timeout dan interceptor
     */
    private fun createOkHttpClient(tokenProvider: (() -> String?)? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createAuthInterceptor(tokenProvider))
            .build()
    }

    /**
     * Membuat instance Retrofit tanpa auth token (untuk login/register)
     */
    fun getRetrofitClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Membuat instance Retrofit dengan auth token (untuk endpoint yang butuh autentikasi)
     */
    fun getAuthRetrofitClient(tokenProvider: () -> String?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(tokenProvider))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Membuat instance ApiService tanpa auth
     */
    fun getApiService(): ApiService {
        return getRetrofitClient().create(ApiService::class.java)
    }

    /**
     * Membuat instance ApiService dengan auth
     */
    fun getApiServiceWithAuth(tokenProvider: () -> String?): ApiService {
        return getAuthRetrofitClient(tokenProvider).create(ApiService::class.java)
    }
}
