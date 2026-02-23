package com.example.horganized.model

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
