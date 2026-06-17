package com.example.salmaflorist.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Response dari API untuk list produk
 */
data class ProductsResponse(
    @SerializedName("products")
    val products: List<ProductDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto? = null
)

/**
 * Response dari API untuk detail produk
 */
data class ProductDetailResponse(
    @SerializedName("product")
    val product: ProductDto
)

/**
 * Response dari API untuk list kategori
 */
data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>
)

/**
 * Response dari API untuk detail kategori
 */
data class CategoryDetailResponse(
    @SerializedName("category")
    val category: CategoryDto
)

/**
 * Data kategori produk
 */
data class CategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Data produk
 */
data class ProductDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("categoryId")
    val categoryId: Int? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: CategoryDto? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Data pagination
 */
data class PaginationDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

/**
 * Request untuk menambah item ke keranjang
 */
data class AddCartItemRequest(
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)

/**
 * Request untuk update quantity item keranjang
 */
data class UpdateCartItemRequest(
    @SerializedName("quantity")
    val quantity: Int
)

/**
 * Data keranjang belanja
 */
data class CartDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("cartItems")
    val cartItems: List<CartItemDto>? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Data item keranjang
 */
data class CartItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("cartId")
    val cartId: Int,
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("product")
    val product: ProductDto? = null
)

/**
 * Response setelah kosongkan keranjang
 */
data class ClearCartResponse(
    @SerializedName("message")
    val message: String? = null
)

// ==================== CART RESPONSE DTOS ====================

/**
 * Response dari GET /carts
 */
data class GetCartResponse(
    @SerializedName("cart")
    val cart: CartDto? = null
)

/**
 * Response dari POST /carts (tambah item) - returns cartItem not cart
 */
data class AddCartResponse(
    @SerializedName("cartItem")
    val cartItem: CartItemDto? = null
)

/**
 * Response dari PUT /carts/{itemId} (update quantity) - returns cartItem not cart
 */
data class UpdateCartResponse(
    @SerializedName("cartItem")
    val cartItem: CartItemDto? = null
)

/**
 * Response dari DELETE /carts/{itemId} (hapus item) - returns cartItem not cart
 */
data class DeleteCartResponse(
    @SerializedName("cartItem")
    val cartItem: CartItemDto? = null
)

/**
 * Response dari API untuk list pesanan
 */
data class OrdersResponse(
    @SerializedName("orders")
    val orders: List<OrderDetailDto>
)

/**
 * Response dari API untuk detail pesanan
 */
data class OrderDetailResponse(
    @SerializedName("order")
    val order: OrderDetailDto
)

/**
 * Data pesanan (basic)
 */
data class OrderDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("invoiceNumber")
    val invoiceNumber: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalAmount")
    val totalAmount: Int,
    @SerializedName("shippingCost")
    val shippingCost: Int,
    @SerializedName("totalPayment")
    val totalPayment: Int,
    @SerializedName("courierName")
    val courierName: String? = null,
    @SerializedName("courierCode")
    val courierCode: String? = null,
    @SerializedName("courierService")
    val courierService: String? = null,
    @SerializedName("etd")
    val etd: String? = null,
    @SerializedName("shippingNumber")
    val shippingNumber: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Data pesanan lengkap (dengan detail)
 */
data class OrderDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("invoiceNumber")
    val invoiceNumber: String? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalAmount")
    val totalAmount: Int,
    @SerializedName("shippingCost")
    val shippingCost: Int,
    @SerializedName("totalPayment")
    val totalPayment: Int,
    @SerializedName("courierName")
    val courierName: String? = null,
    @SerializedName("courierCode")
    val courierCode: String? = null,
    @SerializedName("courierService")
    val courierService: String? = null,
    @SerializedName("etd")
    val etd: String? = null,
    @SerializedName("shippingNumber")
    val shippingNumber: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("user")
    val user: OrderUserDto? = null,
    @SerializedName("address")
    val address: OrderAddressDto? = null,
    @SerializedName("orderItems")
    val orderItems: List<OrderItemDto>? = null
)

/**
 * Data user dalam pesanan
 */
data class OrderUserDto(
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null
)

/**
 * Data alamat dalam pesanan
 */
data class OrderAddressDto(
    @SerializedName("customerName")
    val customerName: String? = null,
    @SerializedName("whatsappNumber")
    val whatsappNumber: String? = null,
    @SerializedName("provinceName")
    val provinceName: String? = null,
    @SerializedName("provinceId")
    val provinceId: String? = null,
    @SerializedName("cityName")
    val cityName: String? = null,
    @SerializedName("cityId")
    val cityId: String? = null,
    @SerializedName("districtName")
    val districtName: String? = null,
    @SerializedName("districtId")
    val districtId: String? = null,
    @SerializedName("postalCode")
    val postalCode: String? = null,
    @SerializedName("addressDetail")
    val addressDetail: String? = null
)

/**
 * Data item pesanan
 */
data class OrderItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("unitPrice")
    val unitPrice: Int,
    @SerializedName("subtotal")
    val subtotal: Int,
    @SerializedName("product")
    val product: ProductDto? = null
)

/**
 * Data provinsi untuk pengiriman
 */
data class ProvinceDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

/**
 * Data kota untuk pengiriman
 */
data class CityDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("name")
    val name: String
)

/**
 * Data kecamatan untuk pengiriman
 */
data class DistrictDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

/**
 * Data ongkos kirim
 */
data class ShippingCostDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("cost")
    val cost: Int,
    @SerializedName("etd")
    val etd: String
)

// ==================== DESTINATION RESPONSE DTOS ====================

/**
 * Response dari GET /destinations/provinces
 */
data class ProvincesResponse(
    @SerializedName("provinces")
    val provinces: List<ProvinceDto>? = null,
    @SerializedName("data")
    val data: List<ProvinceDto>? = null
) {
    fun extractProvinces() = provinces ?: data ?: emptyList()
}

/**
 * Response dari GET /destinations/cities
 */
data class CitiesResponse(
    @SerializedName("cities")
    val cities: List<CityDto>? = null,
    @SerializedName("data")
    val data: List<CityDto>? = null
) {
    fun extractCities() = cities ?: data ?: emptyList()
}

/**
 * Response dari GET /destinations/districts
 */
data class DistrictsResponse(
    @SerializedName("districts")
    val districts: List<DistrictDto>? = null,
    @SerializedName("data")
    val data: List<DistrictDto>? = null
) {
    fun extractDistricts() = districts ?: data ?: emptyList()
}

/**
 * Response dari GET /destinations/costs
 */
data class ShippingCostsResponse(
    @SerializedName("costs")
    val costs: List<ShippingCostDto>? = null,
    @SerializedName("data")
    val data: List<ShippingCostDto>? = null
) {
    fun extractCosts() = costs ?: data ?: emptyList()
}

// ==================== ORDER CREATE DTOS ====================

/**
 * Request untuk membuat pesanan baru (Checkout)
 */
data class CreateOrderRequest(
    @SerializedName("address")
    val address: AddressRequest,
    @SerializedName("courierCode")
    val courierCode: String,
    @SerializedName("courierService")
    val courierService: String
)

/**
 * Data alamat untuk request create order
 */
data class AddressRequest(
    @SerializedName("customerName")
    val customerName: String,
    @SerializedName("whatsappNumber")
    val whatsappNumber: String,
    @SerializedName("provinceId")
    val provinceId: String,
    @SerializedName("cityId")
    val cityId: String,
    @SerializedName("districtId")
    val districtId: String,
    @SerializedName("postalCode")
    val postalCode: String,
    @SerializedName("addressDetail")
    val addressDetail: String
)

/**
 * Response dari POST /orders (create order)
 * Mengembalikan order detail, payment token, dan redirect URL untuk Midtrans
 */
data class CreateOrderResponse(
    @SerializedName("order")
    val order: OrderDetailDto,
    @SerializedName("paymentToken")
    val paymentToken: String? = null,
    @SerializedName("redirectUrl")
    val redirectUrl: String? = null
)

/**
 * Extension function untuk convert ProductDto ke Product model
 */
fun ProductDto.toProductModel(): com.example.salmaflorist.model.Product {
    return com.example.salmaflorist.model.Product(
        id = this.id,
        categoryId = this.categoryId ?: 0,
        name = this.name,
        price = this.price,
        description = this.description ?: "",
        weight = this.weight ?: 0,
        image = this.image ?: "",
        category = com.example.salmaflorist.model.Category(
            id = this.category?.id ?: 0,
            name = this.category?.name ?: ""
        )
    )
}
