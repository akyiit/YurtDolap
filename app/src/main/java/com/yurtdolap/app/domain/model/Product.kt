package com.yurtdolap.app.domain.model

data class Product(
    val id: String,
    val title: String,
    val price: String,
    val imageUrl: String?,
    val tag: String, // e.g. "Satılık" or "Kiralık"
    val categoryId: String? = null, // e.g. "1" for Elektronik
    val sellerName: String,
    val sellerId: String,
    val dormitory: String,
    val isAvailable: Boolean
)
