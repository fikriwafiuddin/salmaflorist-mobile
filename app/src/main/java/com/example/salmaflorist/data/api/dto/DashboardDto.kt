package com.example.salmaflorist.data.api.dto

import com.google.gson.annotations.SerializedName

// ==================== DASHBOARD RESPONSE DTOS ====================

/**
 * Response dari GET /dashboard
 * Data dashboard untuk ADMIN (statistik hari ini, grafik N hari, pesanan terbaru)
 */
data class DashboardResponse(
    @SerializedName("summary")
    val summary: DashboardSummaryDto,
    @SerializedName("chartData")
    val chartData: List<DashboardChartDataDto>,
    @SerializedName("recentOrders")
    val recentOrders: List<OrderDetailDto>
)

/**
 * Ringkasan statistik dashboard
 */
data class DashboardSummaryDto(
    @SerializedName("totalOrders")
    val totalOrders: Int,
    @SerializedName("totalRevenue")
    val totalRevenue: Int
)

/**
 * Data untuk grafik chart
 */
data class DashboardChartDataDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("count")
    val count: Int
)

// ==================== DASHBOARD REPORT RESPONSE DTOS ====================

/**
 * Response dari GET /dashboard/report
 * Laporan bulanan untuk ADMIN
 */
data class DashboardReportResponse(
    @SerializedName("month")
    val month: Int,
    @SerializedName("year")
    val year: Int,
    @SerializedName("totalRevenue")
    val totalRevenue: Int,
    @SerializedName("completedOrders")
    val completedOrders: Int,
    @SerializedName("customers")
    val customers: Int,
    @SerializedName("revenueTrend")
    val revenueTrend: List<RevenueTrendDto>,
    @SerializedName("topProducts")
    val topProducts: List<TopProductDto>,
    @SerializedName("topCategories")
    val topCategories: List<TopCategoryDto>
)

/**
 * Data tren pendapatan per tanggal
 */
data class RevenueTrendDto(
    @SerializedName("date")
    val date: String,
    @SerializedName("revenue")
    val revenue: Int
)

/**
 * Data produk terlaris
 */
data class TopProductDto(
    @SerializedName("product")
    val product: ProductDto,
    @SerializedName("totalQuantity")
    val totalQuantity: Int,
    @SerializedName("totalOrders")
    val totalOrders: Int
)

/**
 * Data kategori terlaris
 */
data class TopCategoryDto(
    @SerializedName("category")
    val category: CategoryDto,
    @SerializedName("totalQuantity")
    val totalQuantity: Int
)
