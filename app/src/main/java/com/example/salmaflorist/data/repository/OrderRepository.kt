package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk pesanan (view only untuk user)
 * Membutuhkan auth token untuk semua operasi
 */
class OrderRepository(private val tokenProvider: () -> String) {

    private val TAG = "OrderRepository"
    private val apiService = ApiConfig.getApiServiceWithAuth(tokenProvider)
    private val gson = com.google.gson.Gson()

    /**
     * Get semua pesanan user
     */
    suspend fun getOrders(
        status: String? = null,
        year: Int? = null,
        month: Int? = null
    ): ApiResult<List<OrderDetailDto>> {
        Log.d(TAG, "Fetching orders - status: $status, year: $year, month: $month")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getOrders(status, year, month)
                Log.d(TAG, "Orders response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val orders = response.body()!!.orders
                        Log.d(TAG, "Successfully fetched ${orders.size} orders")
                        ApiResult.Success(orders)
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch orders: ${error.message}")
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
     * Buat pesanan baru (Checkout)
     * Mengembalikan order detail dan redirectUrl untuk pembayaran Midtrans
     */
    suspend fun createOrder(
        customerName: String,
        whatsappNumber: String,
        provinceId: String,
        cityId: String,
        districtId: String,
        postalCode: String,
        addressDetail: String,
        courierCode: String,
        courierService: String
    ): ApiResult<CreateOrderResponse> {
        Log.d(TAG, "Creating order - customer: $customerName, courier: $courierCode")

        return try {
            withContext(Dispatchers.IO) {
                val request = CreateOrderRequest(
                    address = AddressRequest(
                        customerName = customerName,
                        whatsappNumber = whatsappNumber,
                        provinceId = provinceId,
                        cityId = cityId,
                        districtId = districtId,
                        postalCode = postalCode,
                        addressDetail = addressDetail
                    ),
                    courierCode = courierCode,
                    courierService = courierService
                )

                val response = apiService.createOrder(request)
                Log.d(TAG, "Create order response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val createOrderResponse = response.body()!!
                        Log.d(TAG, "Order created successfully: ${createOrderResponse.order.invoiceNumber}, redirectUrl: ${createOrderResponse.redirectUrl}")
                        ApiResult.Success(createOrderResponse)
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to create order: ${error.message}")
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
     * Get detail pesanan by ID
     */
    suspend fun getOrderById(id: Int): ApiResult<OrderDetailDto> {
        Log.d(TAG, "Fetching order detail for id: $id")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getOrderById(id)
                Log.d(TAG, "Order detail response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val order = response.body()!!.order
                        Log.d(TAG, "Successfully fetched order: ${order.invoiceNumber}")
                        ApiResult.Success(order)
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    response.code() == 403 -> {
                        Log.w(TAG, "Forbidden - not owner of this order")
                        ApiResult.Error("Anda tidak memiliki akses ke pesanan ini", statusCode = 403)
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "Order not found")
                        ApiResult.Error("Pesanan tidak ditemukan", statusCode = 404)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch order: ${error.message}")
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
     * Helper untuk mendapatkan status dalam format yang user-friendly
     */
    fun getStatusText(status: String): String {
        return when (status.uppercase()) {
            "PENDING" -> "Menunggu Pembayaran"
            "PAID" -> "Sudah Dibayar"
            "PROCESSING" -> "Diproses"
            "DELIVERED" -> "Dalam Pengiriman"
            "COMPLETED" -> "Selesai"
            "CANCELLED" -> "Dibatalkan"
            else -> status
        }
    }

    /**
     * Helper untuk mendapatkan warna status
     */
    fun getStatusColor(status: String): Int {
        return when (status.uppercase()) {
            "PENDING" -> android.graphics.Color.parseColor("#FFA500")
            "PAID" -> android.graphics.Color.parseColor("#2196F3")
            "PROCESSING" -> android.graphics.Color.parseColor("#9C27B0")
            "DELIVERED" -> android.graphics.Color.parseColor("#00BCD4")
            "COMPLETED" -> android.graphics.Color.parseColor("#4CAF50")
            "CANCELLED" -> android.graphics.Color.parseColor("#F44336")
            else -> android.graphics.Color.parseColor("#757575")
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
object OrderRepositoryProvider {
    fun getInstance(tokenProvider: () -> String): OrderRepository {
        return OrderRepository(tokenProvider)
    }
}
