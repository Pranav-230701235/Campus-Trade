package com.example.collegemarketplace

data class Request(
    val itemName: String = "",
    val description: String = "",
    val requesterName: String = "",
    val requesterDept: String = "",
    val requesterYear: String = "",
    val requesterId: String = "",
    val requesterPhone: String = "",
    val timestamp: Long = System.currentTimeMillis()
)