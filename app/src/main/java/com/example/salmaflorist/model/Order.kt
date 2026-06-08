package com.example.salmaflorist.model

import java.util.Date

enum class OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    DELIVERED,
    COMPLETED,
    CANCELLED
}

data class Order (
    val id: Int,
    val userId: Int,
    val addressId: Int,
    val invoiceNumber: String,
    val shippingNumber: String,
    val status: OrderStatus,
    val totalAmount: Int,
    val shippingCost: Int,
    val courierName: String,
    val courierCode: String,
    val courierService: String,
    val etd: String,
    val deliveryStartTime: Date?,
    val deliveryEndTime: Date?,
    val createdAt: Date
)
