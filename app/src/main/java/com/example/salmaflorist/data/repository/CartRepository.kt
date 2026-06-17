package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk keranjang belanja
 * Membutuhkan auth token untuk semua operasi
 */
class CartRepository(private val tokenProvider: () -> String) {

    private val TAG = "CartRepository"
    private val apiService = ApiConfig.getApiServiceWithAuth(tokenProvider)
    private val gson = com.google.gson.Gson()

    /**
     * Get keranjang user
     */
    suspend fun getCart(): ApiResult<CartDto> {
        Log.d(TAG, "Fetching cart")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getCart()
                Log.d(TAG, "Cart response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val cartResponse = response.body()!!
                        val cart = cartResponse.cart
                        if (cart != null) {
                            val items = cart.cartItems ?: emptyList()
                            Log.d(TAG, "Successfully fetched cart with ${items.size} items")
                            ApiResult.Success(cart)
                        } else {
                            Log.e(TAG, "Cart response body is null")
                            ApiResult.Error("Gagal memuat keranjang: Data kosong")
                        }
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch cart: ${error.message}")
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
     * Tambah item ke keranjang
     */
    suspend fun addCartItem(productId: Int, quantity: Int): ApiResult<CartDto> {
        Log.d(TAG, "Adding item to cart - productId: $productId, quantity: $quantity")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.addCartItem(AddCartItemRequest(productId, quantity))
                Log.d(TAG, "Add cart item response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val cartResponse = response.body()!!
                        val cart = cartResponse.cart
                        if (cart != null) {
                            Log.d(TAG, "Successfully added item to cart")
                            ApiResult.Success(cart)
                        } else {
                            Log.e(TAG, "Cart response body is null")
                            ApiResult.Error("Gagal menambah item: Data kosong")
                        }
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "Product not found")
                        ApiResult.Error("Produk tidak ditemukan", statusCode = 404)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to add cart item: ${error.message}")
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
     * Update quantity item keranjang
     */
    suspend fun updateCartItem(itemId: Int, quantity: Int): ApiResult<CartDto> {
        Log.d(TAG, "Updating cart item - itemId: $itemId, quantity: $quantity")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.updateCartItem(itemId, UpdateCartItemRequest(quantity))
                Log.d(TAG, "Update cart item response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val cartResponse = response.body()!!
                        val cart = cartResponse.cart
                        if (cart != null) {
                            Log.d(TAG, "Successfully updated cart item")
                            ApiResult.Success(cart)
                        } else {
                            Log.e(TAG, "Cart response body is null")
                            ApiResult.Error("Gagal update item: Data kosong")
                        }
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "Cart item not found")
                        ApiResult.Error("Item keranjang tidak ditemukan", statusCode = 404)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to update cart item: ${error.message}")
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
     * Hapus item dari keranjang
     */
    suspend fun deleteCartItem(itemId: Int): ApiResult<CartDto> {
        Log.d(TAG, "Deleting cart item - itemId: $itemId")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.deleteCartItem(itemId)
                Log.d(TAG, "Delete cart item response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val cartResponse = response.body()!!
                        val cart = cartResponse.cart
                        if (cart != null) {
                            Log.d(TAG, "Successfully deleted cart item")
                            ApiResult.Success(cart)
                        } else {
                            Log.e(TAG, "Cart response body is null")
                            ApiResult.Error("Gagal menghapus item: Data kosong")
                        }
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to delete cart item: ${error.message}")
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
     * Kosongkan keranjang
     */
    suspend fun clearCart(): ApiResult<String> {
        Log.d(TAG, "Clearing cart")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.clearCart()
                Log.d(TAG, "Clear cart response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val result = response.body()!!
                        Log.d(TAG, "Successfully cleared cart")
                        ApiResult.Success(result.message ?: "Keranjang berhasil dikosongkan")
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to clear cart: ${error.message}")
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
object CartRepositoryProvider {
    fun getInstance(tokenProvider: () -> String): CartRepository {
        return CartRepository(tokenProvider)
    }
}
