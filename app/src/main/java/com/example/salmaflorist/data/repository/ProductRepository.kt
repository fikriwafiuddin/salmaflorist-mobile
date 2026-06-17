package com.example.salmaflorist.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Repository untuk produk dan kategori
 */
class ProductRepository(private val tokenProvider: () -> String) {

    private val TAG = "ProductRepository"
    private val apiService = ApiConfig.getApiServiceWithAuth(tokenProvider)

    /**
     * Get semua kategori
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
     * Get produk dengan filter opsional
     */
    suspend fun getProducts(
        categoryId: Int? = null,
        search: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): ApiResult<List<ProductDto>> {
        Log.d(TAG, "Fetching products - categoryId: $categoryId, search: $search, page: $page")

        return safeApiCall {
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
    }

    /**
     * Get detail produk by ID
     */
    suspend fun getProductById(id: Int): ApiResult<ProductDto> {
        Log.d(TAG, "Fetching product detail for id: $id")

        return safeApiCall {
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
    }

    /**
     * Buat produk baru (ADMIN only)
     * @param categoryId ID kategori
     * @param name Nama produk
     * @param price Harga produk
     * @param weight Berat produk (gram)
     * @param description Deskripsi produk
     * @param imageUri URI gambar produk (opsional)
     * @param context Context untuk membaca file
     * @return ApiResult<ProductDto>
     */
    suspend fun createProduct(
        categoryId: Int,
        name: String,
        price: Int,
        weight: Int,
        description: String,
        imageUri: Uri? = null,
        context: Context? = null
    ): ApiResult<ProductDto> {
        Log.d(TAG, "Creating product: $name")

        return safeApiCall {
            // Prepare image part if provided
            val imagePart = if (imageUri != null && context != null) {
                createMultipartBodyFromUri(imageUri, context)
            } else null

            // Create request parts
            val categoryIdPart = categoryId.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val namePart = name.toRequestBody(MEDIA_TYPE_TEXT)
            val pricePart = price.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val weightPart = weight.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val descriptionPart = description.toRequestBody(MEDIA_TYPE_TEXT)

            val response = apiService.createProduct(
                categoryId = categoryIdPart,
                name = namePart,
                price = pricePart,
                weight = weightPart,
                description = descriptionPart,
                image = imagePart
            )
            Log.d(TAG, "Create product response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val product = response.body()!!.product
                    Log.d(TAG, "Successfully created product: ${product.name}")
                    ApiResult.Success(product)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk membuat produk", statusCode = 403)
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
                    Log.e(TAG, "Failed to create product: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Update produk (ADMIN only)
     * @param id ID produk
     * @param categoryId ID kategori
     * @param name Nama produk
     * @param price Harga produk
     * @param weight Berat produk (gram)
     * @param description Deskripsi produk
     * @param imageUri URI gambar produk (opsional)
     * @param context Context untuk membaca file
     * @return ApiResult<ProductDto>
     */
    suspend fun updateProduct(
        id: Int,
        categoryId: Int,
        name: String,
        price: Int,
        weight: Int,
        description: String,
        imageUri: Uri? = null,
        context: Context? = null
    ): ApiResult<ProductDto> {
        Log.d(TAG, "Updating product $id: $name")

        return safeApiCall {
            // Prepare image part if provided
            val imagePart = if (imageUri != null && context != null) {
                createMultipartBodyFromUri(imageUri, context)
            } else null

            // Create request parts
            val categoryIdPart = categoryId.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val namePart = name.toRequestBody(MEDIA_TYPE_TEXT)
            val pricePart = price.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val weightPart = weight.toString().toRequestBody(MEDIA_TYPE_TEXT)
            val descriptionPart = description.toRequestBody(MEDIA_TYPE_TEXT)

            val response = apiService.updateProduct(
                id = id,
                categoryId = categoryIdPart,
                name = namePart,
                price = pricePart,
                weight = weightPart,
                description = descriptionPart,
                image = imagePart
            )
            Log.d(TAG, "Update product response: ${response.code()}")

            when {
                response.isSuccessful && response.body() != null -> {
                    val product = response.body()!!.product
                    Log.d(TAG, "Successfully updated product: ${product.name}")
                    ApiResult.Success(product)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk mengupdate produk", statusCode = 403)
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Product not found")
                    ApiResult.Error("Produk tidak ditemukan", statusCode = 404)
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
                    Log.e(TAG, "Failed to update product: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Hapus produk (soft delete) (ADMIN only)
     * @param id ID produk
     * @return ApiResult<Unit>
     */
    suspend fun deleteProduct(id: Int): ApiResult<Unit> {
        Log.d(TAG, "Deleting product: $id")

        return safeApiCall {
            val response = apiService.deleteProduct(id)
            Log.d(TAG, "Delete product response: ${response.code()}")

            when {
                response.isSuccessful -> {
                    Log.d(TAG, "Successfully deleted product: $id")
                    ApiResult.Success(Unit)
                }
                response.code() == 401 -> {
                    Log.w(TAG, "Unauthorized - token invalid")
                    ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                }
                response.code() == 403 -> {
                    Log.w(TAG, "Forbidden - not admin")
                    ApiResult.Error("Anda tidak memiliki akses untuk menghapus produk", statusCode = 403)
                }
                response.code() == 404 -> {
                    Log.w(TAG, "Product not found")
                    ApiResult.Error("Produk tidak ditemukan", statusCode = 404)
                }
                else -> {
                    val error = parseError(response.errorBody()?.string())
                    Log.e(TAG, "Failed to delete product: ${error.message}")
                    ApiResult.Error(
                        message = error.message,
                        statusCode = response.code()
                    )
                }
            }
        }
    }

    /**
     * Membuat MultipartBody.Part dari Uri
     * @param uri Uri dari file gambar
     * @param context Context
     * @return MultipartBody.Part
     */
    private fun createMultipartBodyFromUri(uri: Uri, context: Context): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody(getMimeType(uri)?.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating multipart body from uri: ${e.message}", e)
            null
        }
    }

    /**
     * Mendapatkan MIME type dari Uri
     * @param uri Uri dari file
     * @return String MIME type
     */
    private fun getMimeType(uri: Uri): String? {
        return try {
            // Default to image/jpeg for now
            "image/jpeg"
        } catch (e: Exception) {
            "image/jpeg"
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
}

/**
 * Singleton provider
 */
object ProductRepositoryProvider {
    fun getInstance(tokenProvider: () -> String): ProductRepository {
        return ProductRepository(tokenProvider)
    }
}

/**
 * Companion object untuk MEDIA_TYPE_TEXT
 */
private val MEDIA_TYPE_TEXT = "text/plain".toMediaTypeOrNull()
