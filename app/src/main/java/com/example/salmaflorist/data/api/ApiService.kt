package com.example.salmaflorist.data.api

import com.example.salmaflorist.data.api.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * REST API Service untuk semua endpoint
 * Menggunakan Retrofit untuk HTTP requests
 */
interface ApiService {

    // ==================== AUTH ====================

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

    // ==================== CATEGORIES ====================

    /**
     * Get semua kategori
     * GET /categories
     */
    @GET("categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    /**
     * Get kategori by ID
     * GET /categories/{id}
     */
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Int
    ): Response<CategoryDetailResponse>

    /**
     * Buat kategori baru (ADMIN only)
     * POST /categories
     * Membutuhkan auth token
     */
    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): Response<CategoryDetailResponse>

    /**
     * Update kategori (ADMIN only)
     * PUT /categories/{id}
     * Membutuhkan auth token
     */
    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: UpdateCategoryRequest
    ): Response<CategoryDetailResponse>

    /**
     * Hapus kategori (ADMIN only)
     * DELETE /categories/{id}
     * Membutuhkan auth token
     */
    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<Unit>

    // ==================== PRODUCTS ====================

    /**
     * Get semua produk dengan filter opsional
     * GET /products
     * Query params: categoryId, search, page, limit
     */
    @GET("products")
    suspend fun getProducts(
        @Query("categoryId") categoryId: Int? = null,
        @Query("name") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ProductsResponse>

    /**
     * Get produk by ID
     * GET /products/{id}
     */
    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): Response<ProductDetailResponse>

    /**
     * Buat produk baru (ADMIN only)
     * POST /products
     * Membutuhkan auth token
     * Menggunakan multipart/form-data untuk upload gambar
     */
    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Part("categoryId") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductDetailResponse>

    /**
     * Update produk (ADMIN only)
     * PUT /products/{id}
     * Membutuhkan auth token
     * Menggunakan multipart/form-data untuk upload gambar
     */
    @Multipart
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("categoryId") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ProductDetailResponse>

    /**
     * Hapus produk (soft delete) (ADMIN only)
     * DELETE /products/{id}
     * Membutuhkan auth token
     */
    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): Response<Unit>

    // ==================== CARTS ====================

    /**
     * Get keranjang user
     * GET /carts
     * Membutuhkan auth token
     */
    @GET("carts")
    suspend fun getCart(): Response<GetCartResponse>

    /**
     * Tambah item ke keranjang
     * POST /carts
     * Membutuhkan auth token
     */
    @POST("carts")
    suspend fun addCartItem(
        @Body request: AddCartItemRequest
    ): Response<AddCartResponse>

    /**
     * Update quantity item keranjang
     * PUT /carts/{itemId}
     * Membutuhkan auth token
     */
    @PUT("carts/{itemId}")
    suspend fun updateCartItem(
        @Path("itemId") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<UpdateCartResponse>

    /**
     * Hapus item dari keranjang
     * DELETE /carts/{itemId}
     * Membutuhkan auth token
     */
    @DELETE("carts/{itemId}")
    suspend fun deleteCartItem(
        @Path("itemId") itemId: Int
    ): Response<DeleteCartResponse>

    /**
     * Kosongkan keranjang
     * DELETE /carts
     * Membutuhkan auth token
     */
    @DELETE("carts")
    suspend fun clearCart(): Response<ClearCartResponse>

    // ==================== ORDERS ====================

    /**
     * Get semua pesanan user
     * GET /orders
     * Membutuhkan auth token
     */
    @GET("orders")
    suspend fun getOrders(
        @Query("status") status: String? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<OrdersResponse>

    /**
     * Get detail pesanan
     * GET /orders/{id}
     * Membutuhkan auth token
     */
    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Int
    ): Response<OrderDetailResponse>

    /**
     * Buat pesanan baru (Checkout)
     * POST /orders
     * Membutuhkan auth token
     * Mengembalikan order detail dan redirectUrl untuk pembayaran Midtrans
     */
    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<CreateOrderResponse>

    /**
     * Update status pesanan (ADMIN only)
     * PUT /orders/{id}/status
     * Membutuhkan auth token
     * @param id ID pesanan
     * @param request Request berisi status dan shippingNumber (opsional)
     */
    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderDetailResponse>

    // ==================== DASHBOARD ====================

    /**
     * Get dashboard data untuk ADMIN
     * GET /dashboard
     * Membutuhkan auth token
     * @param days Jumlah hari untuk data grafik (1-30, default: 7)
     */
    @GET("dashboard")
    suspend fun getDashboard(
        @Query("days") days: Int? = null
    ): Response<DashboardResponse>

    /**
     * Get laporan bulanan untuk ADMIN
     * GET /dashboard/report
     * Membutuhkan auth token
     * @param month Bulan (1-12), default: bulan saat ini
     * @param year Tahun, default: tahun saat ini
     */
    @GET("dashboard/report")
    suspend fun getDashboardReport(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<DashboardReportResponse>

    // ==================== DESTINATIONS ====================

    /**
     * Get semua provinsi
     * GET /destinations/provinces
     */
    @GET("destinations/provinces")
    suspend fun getProvinces(): Response<ProvincesResponse>

    /**
     * Get kota berdasarkan provinsi
     * GET /destinations/cities
     */
    @GET("destinations/cities")
    suspend fun getCities(
        @Query("province") province: String
    ): Response<CitiesResponse>

    /**
     * Get kecamatan berdasarkan kota
     * GET /destinations/districts
     */
    @GET("destinations/districts")
    suspend fun getDistricts(
        @Query("city") city: String
    ): Response<DistrictsResponse>

    /**
     * Hitung ongkos kirim
     * GET /destinations/costs
     */
    @GET("destinations/costs")
    suspend fun getShippingCosts(
        @Query("destination") destination: String,
        @Query("weight") weight: Int
    ): Response<ShippingCostsResponse>
}
