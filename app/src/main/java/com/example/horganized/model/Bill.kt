package com.example.horganized.model

data class Bill(
    val billId: String = "",
    val userId: String = "",
    val monthYear: String = "", // เช่น "เมษายน 2026"
    val dueDate: String = "",    // เช่น "5 พ.ค. 2026"
    val roomRent: Double = 0.0,
    val electricityBill: Double = 0.0,
    val electricityUnits: Int = 0,
    val waterBill: Double = 0.0,
    val additionalFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val isPaid: Boolean = false
)