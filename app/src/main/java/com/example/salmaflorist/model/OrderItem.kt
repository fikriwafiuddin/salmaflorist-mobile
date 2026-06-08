package com.example.salmaflorist.model

data class OrderItem (
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: Int,
    val subTotal: Int
)