package com.example.salmaflorist.data.repository

import android.util.Log
import com.example.salmaflorist.data.api.ApiConfig
import com.example.salmaflorist.data.api.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repository untuk dashboard admin
 * Membutuhkan auth token untuk semua operasi
 */
class DashboardRepository(private val tokenProvider: () -> String) {

    private val TAG = "DashboardRepository"
    private val apiService = ApiConfig.getApiServiceWithAuth(tokenProvider)
    private val gson = com.google.gson.Gson()

    /**
     * Get dashboard data untuk ADMIN
     * @param days Jumlah hari untuk data grafik (1-30, default: 7)
     * @return ApiResult<DashboardData> berisi summary, chart data, dan recent orders
     */
    suspend fun getDashboard(days: Int? = null): ApiResult<DashboardData> {
        Log.d(TAG, "Fetching dashboard data - days: $days")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getDashboard(days)
                Log.d(TAG, "Dashboard response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val dashboardResponse = response.body()!!
                        val dashboardData = DashboardData(
                            summary = dashboardResponse.summary,
                            chartData = dashboardResponse.chartData,
                            recentOrders = dashboardResponse.recentOrders
                        )
                        Log.d(TAG, "Successfully fetched dashboard - orders: ${dashboardResponse.summary.totalOrders}, revenue: ${dashboardResponse.summary.totalRevenue}")
                        ApiResult.Success(dashboardData)
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    response.code() == 403 -> {
                        Log.w(TAG, "Forbidden - not admin")
                        ApiResult.Error("Anda tidak memiliki akses ke dashboard", statusCode = 403)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch dashboard: ${error.message}")
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
     * Get laporan bulanan untuk ADMIN
     * @param month Bulan (1-12), default: bulan saat ini
     * @param year Tahun, default: tahun saat ini
     * @return ApiResult<DashboardReportData> berisi laporan lengkap
     */
    suspend fun getDashboardReport(
        month: Int? = null,
        year: Int? = null
    ): ApiResult<DashboardReportData> {
        Log.d(TAG, "Fetching dashboard report - month: $month, year: $year")

        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getDashboardReport(month, year)
                Log.d(TAG, "Dashboard report response: ${response.code()}")

                when {
                    response.isSuccessful && response.body() != null -> {
                        val report = response.body()!!
                        val reportData = DashboardReportData(
                            month = report.month,
                            year = report.year,
                            totalRevenue = report.totalRevenue,
                            completedOrders = report.completedOrders,
                            customers = report.customers,
                            revenueTrend = report.revenueTrend,
                            topProducts = report.topProducts,
                            topCategories = report.topCategories
                        )
                        Log.d(TAG, "Successfully fetched report - revenue: ${report.totalRevenue}, orders: ${report.completedOrders}")
                        ApiResult.Success(reportData)
                    }
                    response.code() == 401 -> {
                        Log.w(TAG, "Unauthorized - token invalid")
                        ApiResult.Error("Sesi telah berakhir. Silakan login kembali.", statusCode = 401)
                    }
                    response.code() == 403 -> {
                        Log.w(TAG, "Forbidden - not admin")
                        ApiResult.Error("Anda tidak memiliki akses ke laporan", statusCode = 403)
                    }
                    else -> {
                        val error = parseError(response.errorBody()?.string())
                        Log.e(TAG, "Failed to fetch report: ${error.message}")
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
     * Parse error body dari response API
     */
    private fun parseError(jsonString: String?): ErrorResponse {
        return try {
            if (jsonString.isNullOrBlank()) {
                ErrorResponse("Terjadi kesalahan pada server")
            } else {
                gson.fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Error) {
            ErrorResponse("Terjadi kesalahan pada server")
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
}

/**
 * Data class untuk dashboard response
 * Menggabungkan summary, chart data, dan recent orders
 */
data class DashboardData(
    val summary: DashboardSummaryDto,
    val chartData: List<DashboardChartDataDto>,
    val recentOrders: List<OrderDetailDto>
)

/**
 * Data class untuk dashboard report response
 * Menggabungkan semua data laporan bulanan
 */
data class DashboardReportData(
    val month: Int,
    val year: Int,
    val totalRevenue: Int,
    val completedOrders: Int,
    val customers: Int,
    val revenueTrend: List<RevenueTrendDto>,
    val topProducts: List<TopProductDto>,
    val topCategories: List<TopCategoryDto>
)

/**
 * Singleton provider
 */
object DashboardRepositoryProvider {
    fun getInstance(tokenProvider: () -> String): DashboardRepository {
        return DashboardRepository(tokenProvider)
    }
}
