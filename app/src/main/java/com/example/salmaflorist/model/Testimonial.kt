package com.example.salmaflorist.model

import java.util.Date

data class Testimonial (
    val id: Int,
    val userId: Int,
    val orderId: Int,
    val review: String,
    val rating: Int,
    val createdAt: Date
)
