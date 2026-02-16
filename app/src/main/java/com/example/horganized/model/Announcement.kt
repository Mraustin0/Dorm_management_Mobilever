package com.example.horganized.model

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val detail: String = "",
    val imageUrl: String = "",
    val date: String = "",
    val timestamp: Long = 0
)
