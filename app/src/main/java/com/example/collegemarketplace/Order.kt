package com.example.collegemarketplace

import com.google.firebase.firestore.PropertyName

data class Order(
    val productTitle: String = "",
    val productPrice: String = "",
    val productImageUrl: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val buyerId: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("isDelivered")
    @set:PropertyName("isDelivered")
    var isDelivered: Boolean = false
)