package com.example.salmaflorist.model

data class Address (
    val id: Int,
    val user_id: Int,
    val customerName: String,
    val whatsappNumber: Number,
    val addressDetail: String,
    val provinceId: Int,
    val provinceName: String,
    val cityid: Int,
    val cityName: String,
    val districId: Int,
    val districName: String,
    val postalCode: Number
)