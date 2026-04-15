package com.example.salmaflorist

data class Product (
    val id: Int,
    val categoryId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val weight: Int,
    val image: String,
    val category: Category
)