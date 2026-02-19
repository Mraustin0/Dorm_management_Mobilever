package com.example.horganized.model

import com.google.firebase.Timestamp

data class AdminNotification(
    // ชื่อตัวแปรตรงกับ Field ใน Firebase (Admin_Notifications)
    val title: String = "",
    val message: String = "",
    val roomNumber: String = "",
    val type: String = "",
    val userId: String = "",
    val isRead: Boolean = false,
    val timestamp: Timestamp? = null
)
