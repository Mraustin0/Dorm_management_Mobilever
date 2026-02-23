package com.example.horganized.model

data class BillDetails(
    val electricPrice: Double = 0.0,
    val electricUnit: String = "",
    val otherPrice: Double = 0.0,
    val roomPrice: Double = 0.0,
    val waterPrice: Double = 0.0,
    val waterUnit: String = ""
)

data class Bill(
    val userId: String = "",
    val amount: Double = 0.0,
    val details: BillDetails = BillDetails(),
    val dueDate: com.google.firebase.Timestamp? = null,
    val month: String = "",
    val year: String = "",
    val status: String = "",
    val slipUrl: String = "",
    val paymentDate: com.google.firebase.Timestamp? = null,
    val roomNumber: String = ""
) {
    val isPaid: Boolean get() = status == "paid"
    val isPending: Boolean get() = status == "pending"
}
