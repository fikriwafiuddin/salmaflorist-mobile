package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk produk dan kategori
 */
class ProductRepository(private val tokenProvider: () -> String?) {

    private val TAG = "ProductRepository"
    private val apiService = tokenProvider?.let {
        ApiConfig.getApiServiceWithAuth(it)
    } ?: ApiConfig.getApiService()

    /**
     * Get semua kategori
     */
    suspend fun getCategories(): ApiResult<List<CategoryDto>> {
        Log.d(TAG, "Fetching categories")

        return try {
            withContext(Dispatchers.IO) {
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
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Get produk dengan filter opsional
     */
    suspend fun getProducts(
        categoryId: Int? = null,
        search: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): ApiResult<List<ProductDto>> {
        Log.d(TAG, "Fetching products - categoryId: $categoryId, search: $search, page: $page")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getProducts(categoryId, search, page, limit)
                Log.d(TAG, "Products response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val products = response.body()!!.products
                        Log.d(TAG, "Successfully fetched ${products.size} products")
                        ApiResult.Success(products)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch products: ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    /**
     * Get detail produk by ID
     */
    suspend fun getProductById(id: Int): ApiResult<ProductDto> {
        Log.d(TAG, "Fetching product detail for id: $id")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getProductById(id)
                Log.d(TAG, "Product detail response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val product = response.body()!!.product
                        Log.d(TAG, "Successfully fetched product: ${product.name}")
                        ApiResult.Success(product)
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "Product not found")
                        ApiResult.Error("Produk tidak ditemukan", statusCode = 404)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch product: ${error.message}")
                        ApiResult.Error(
                            message = error.message,
                            statusCode = response.code()
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}", e)
            ApiResult.Error("Koneksi internet bermasalah")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            ApiResult.Error("Terjadi kesalahan tak terduga")
        }
    }

    private fun parseError(jsonString: String?): ErrorResponse {
        return try {
            if (jsonString.isNullOrBlank()) {
                ErrorResponse("Terjadi kesalahan pada server")
            } else {
                com.google.gson.Gson().fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Exception) {
            ErrorResponse("Terjadi kesalahan pada server")
        }
    }
}

/**
 * Singleton provider
 */
object ProductRepositoryProvider {
    fun getInstance(tokenProvider: () -> String?): ProductRepository {
        return ProductRepository(tokenProvider)
    }
}
