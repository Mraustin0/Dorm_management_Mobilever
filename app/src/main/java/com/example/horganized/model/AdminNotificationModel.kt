package com.example.horganized.model

data class AdminNotificationModel(
    val notificationId: String = "",
    val userId: String = "", // ผู้ส่ง (user id)
    val userName: String = "",
    val roomNumber: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // repair, payment, etc.
    val timestamp: Long = 0,
    val isRead: Boolean = false
)