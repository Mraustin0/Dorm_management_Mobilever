package com.example.horganized.model

import com.google.firebase.Timestamp

data class AdminNotification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val roomNumber: String = "",
    val type: String = "",
    val userId: String = "",
    val isRead: Boolean = false,
    val timestamp: Timestamp? = null
)
