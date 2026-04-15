package com.example.salmaflorist

data class CartItem(
    val cartId: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Int,
    val productImage: String,
    var quantity: Int
)