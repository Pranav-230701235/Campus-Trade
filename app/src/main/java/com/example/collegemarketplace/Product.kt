package com.example.collegemarketplace

import com.google.firebase.firestore.PropertyName

data class Product(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "", // <--- NEW: Stores the category (e.g., "Lab Kits")
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerDept: String = "",
    val sellerYear: String = "",
    val sellerPhone: String = "",
    val buyerId: String = "",

    // Ensures Firebase matches the exact field name "isSold"
    @get:PropertyName("isSold")
    @set:PropertyName("isSold")
    var isSold: Boolean = false,

    // Ensures Firebase matches the exact field name "isDelivered"
    @get:PropertyName("isDelivered")
    @set:PropertyName("isDelivered")
    var isDelivered: Boolean = false
)