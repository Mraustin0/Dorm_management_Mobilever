package com.example.horganized.model

data class MoveOutRequest(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val roomNumber: String = "",
    val notifyDate: String = "",
    val moveOutDate: String = "",
    val status: String = "pending", // pending, approved, rejected
    val timestamp: Long = 0,
    val depositAmount: Double = 0.0,
    val refundAmount: Double = 0.0,
    val damageFee: Double = 0.0,
    val returnDate: String = ""
)
