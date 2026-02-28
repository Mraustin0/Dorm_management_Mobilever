package com.example.horganized.model

import com.google.firebase.Timestamp

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val senderName: String = "ระบบ",
    // type: "payment_approved" | "payment_rejected" | "repair_update" | "new_bill" | "chat"
    val type: String = "",
    val timestamp: Long = 0,          // Long สำหรับ System.currentTimeMillis() (legacy)
    val firestoreTimestamp: Timestamp? = null,  // Firestore Timestamp (ใหม่)
    val isRead: Boolean = false
)