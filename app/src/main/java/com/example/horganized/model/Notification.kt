package com.example.horganized.model

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val senderName: String = "ADMIN1",
    val timestamp: Long = 0,
    val time: String = "", // เพิ่มฟิลด์นี้สำหรับ Mock data เช่น "1m ago."
    val isRead: Boolean = false
)