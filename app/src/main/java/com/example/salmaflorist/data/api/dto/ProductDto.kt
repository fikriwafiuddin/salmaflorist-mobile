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
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("product")
    val product: ProductDto? = null
)

/**
 * Response setelah operasi cart (tambah/update/delete)
 */
data class CartResponse(
    @SerializedName("cart")
    val cart: CartDto? = null,
    @SerializedName("message")
    val message: String? = null
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
