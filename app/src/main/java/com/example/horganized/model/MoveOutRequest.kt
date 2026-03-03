package com.example.horganized.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class MoveOutRequest(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val roomNumber: String = "",
    val notifyDate: String = "",
    val moveOutDate: String = "",
    val status: String = "pending", // pending, approved, rejected
    val timestamp: Timestamp? = null,
    val depositAmount: Double = 0.0,
    val refundAmount: Double = 0.0,
    val damageFee: Double = 0.0,
    val returnDate: String = ""
)
