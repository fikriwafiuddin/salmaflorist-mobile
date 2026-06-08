package com.example.salmaflorist.model

data class CartItem(
    val cartId: Int,
    val productId: Int,
    var quantity: Int,
    val product: Product? = null
)
