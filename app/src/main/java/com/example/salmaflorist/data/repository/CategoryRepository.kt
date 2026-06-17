package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk kategori (ADMIN only)
 * Membutuhkan auth token untuk semua operasi write
 */
class CategoryRepository(private val tokenProvider: () -> String) {

    private val TAG = "CategoryRepository"
    private val apiService = ApiConfig.getApiServiceWithAuth(tokenProvider)
    private val gson = com.google.gson.Gson()

    /**
     * Get semua kategori
     * @return ApiResult<List<CategoryDto>>
     */
    suspend fun getCategories(): ApiResult<List<CategoryDto>> {
        Log.d(TAG, "Fetching categories")

        return safeApiCall {
            val response = apiService.getCategories()
            Log.d(TAG, "Categories response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val categories = response.body()!!.categories
                    Log.d(TAG, "Successfully fetched ${categories.size} categories")
                    ApiResult.Success(categories)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to fetch categories: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Buat kategori baru (ADMIN only)
     * @param name Nama kategori
     * @return ApiResult<CategoryDto>
     */
    suspend fun createCategory(name: String): ApiResult<CategoryDto> {
        Log.d(TAG, "Creating category: $name")

        return safeApiCall {
            val request = CreateCategoryRequest(name = name)
            val response = apiService.createCategory(request)
            Log.d(TAG, "Create category response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val category = response.body()!!.category
                    Log.d(TAG, "Successfully created category: ${category.name}")
                    ApiResult.Success(category)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk membuat kategori", statusCode = 403)
                }
                response.code() == 400 -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.w(TAG, "Validation error: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        errors = error.errors,
                        statusCode = response.code()
                    )
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to create category: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Update kategori (ADMIN only)
     * @param id ID kategori
     * @param name Nama kategori baru
     * @return ApiResult<CategoryDto>
     */
    suspend fun updateCategory(id: Int, name: String): ApiResult<CategoryDto> {
        Log.d(TAG, "Updating category $id: $name")

        return safeApiCall {
            val request = UpdateCategoryRequest(name = name)
            val response = apiService.updateCategory(id, request)
            Log.d(TAG, "Update category response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val category = response.body()!!.category
                    Log.d(TAG, "Successfully updated category: ${category.name}")
                    ApiResult.Success(category)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk mengupdate kategori", statusCode = 403)
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Category not found")
                    ApiResult.Error("Kategori tidak ditemukan", statusCode = 404)
                }
                response.code() == 400 -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.w(TAG, "Validation error: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        errors = error.errors,
                        statusCode = response.code()
                    )
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to update category: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Hapus kategori (ADMIN only)
     * @param id ID kategori
     * @return ApiResult<Unit>
     */
    suspend fun deleteCategory(id: Int): ApiResult<Unit> {
        Log.d(TAG, "Deleting category: $id")

        return safeApiCall {
            val response = apiService.deleteCategory(id)
            Log.d(TAG, "Delete category response: ${response.code()}")

            when {
                response.isSuccessful -> {
                    Log.d(TAG, "Successfully deleted category: $id")
                    ApiResult.Success(Unit)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk menghapus kategori", statusCode = 403)
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Category not found")
                    ApiResult.Error("Kategori tidak ditemukan", statusCode = 404)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to delete category: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Helper function untuk menangani exception dengan aman
     * CancellationException akan di-rethrow agar coroutine tahu job dibatalkan
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResult<T>): ApiResult<T> {
        return try {
            withContext(Dispatchers.IO) {
                apiCall()
            }
        } catch (e: CancellationException) {
            // Job was cancelled (user navigated away), re-throw
            Log.d(TAG, "Job was cancelled")
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Parse error body dari response API
     */
    private fun parseError(jsonString: String?): ErrorResponse {
        return try {
            if (jsonString.isNullOrBlank()) {
                ErrorResponse("Terjadi kesalahan pada server")
            } else {
                gson.fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Exception) {
            ErrorResponse("Terjadi kesalahan pada server")
        }
    }
}

/**
 * Singleton provider
 */
object CategoryRepositoryProvider {
    fun getInstance(tokenProvider: () -> String): CategoryRepository {
        return CategoryRepository(tokenProvider)
    }
}
