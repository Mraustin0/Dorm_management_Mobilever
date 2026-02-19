package com.example.horganized.model

data class RepairRequest(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val roomNumber: String = "",
    val userPhone: String = "",
    val repairType: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val status: String = "pending", // pending, in_progress, completed
    val timestamp: Long = 0
)
