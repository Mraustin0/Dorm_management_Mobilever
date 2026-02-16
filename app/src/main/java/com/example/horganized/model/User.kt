package com.example.horganized.model

data class User(
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImage: String = "",
    val role: String = "user",
    val roomNumber: String = "",
    val contractTerm: String = "",
    val waterMeter: Int = 0,
    val electricMeter: Int = 0,
    val moveInDate: com.google.firebase.Timestamp? = null
)
