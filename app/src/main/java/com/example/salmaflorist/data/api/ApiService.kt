package com.example.salmaflorist.data.api

import com.example.salmaflorist.data.api.dto.*
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

    // ==================== PRODUCTS ====================

    /**
     * Get semua produk dengan filter opsional
     * GET /products
     * Query params: categoryId, search, page, limit
     */
    @GET("products")
    suspend fun getProducts(
        @Query("categoryId") categoryId: Int? = null,
        @Query("search") search: String? = null,
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

    // ==================== CARTS ====================

    /**
     * Get keranjang user
     * GET /carts
     * Membutuhkan auth token
     */
    @GET("carts")
    suspend fun getCart(): Response<CartResponse>

    /**
     * Tambah item ke keranjang
     * POST /carts
     * Membutuhkan auth token
     */
    @POST("carts")
    suspend fun addCartItem(
        @Body request: AddCartItemRequest
    ): Response<CartResponse>

    /**
     * Update quantity item keranjang
     * PUT /carts/{itemId}
     * Membutuhkan auth token
     */
    @PUT("carts/{itemId}")
    suspend fun updateCartItem(
        @Path("itemId") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<CartResponse>

    /**
     * Hapus item dari keranjang
     * DELETE /carts/{itemId}
     * Membutuhkan auth token
     */
    @DELETE("carts/{itemId}")
    suspend fun deleteCartItem(
        @Path("itemId") itemId: Int
    ): Response<CartResponse>

    /**
     * Kosongkan keranjang
     * DELETE /carts
     * Membutuhkan auth token
     */
    @DELETE("carts")
    suspend fun clearCart(): Response<CartResponse>

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

    // ==================== DESTINATIONS ====================

    /**
     * Get semua provinsi
     * GET /destinations/provinces
     */
    @GET("destinations/provinces")
    suspend fun getProvinces(): Response<List<ProvinceDto>>

    /**
     * Get kota berdasarkan provinsi
     * GET /destinations/cities
     */
    @GET("destinations/cities")
    suspend fun getCities(
        @Query("provinceId") provinceId: String
    ): Response<List<CityDto>>

    /**
     * Get kecamatan berdasarkan kota
     * GET /destinations/districts
     */
    @GET("destinations/districts")
    suspend fun getDistricts(
        @Query("cityId") cityId: String
    ): Response<List<DistrictDto>>

    /**
     * Hitung ongkos kirim
     * GET /destinations/costs
     */
    @GET("destinations/costs")
    suspend fun getShippingCosts(
        @Query("districtId") districtId: String
    ): Response<List<ShippingCostDto>>
}
