package com.example.horganized.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayBillConfirmActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var billId = ""
    private var imageUriString: String? = null
    private var billAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill_confirm)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val ivSlip = findViewById<ImageView>(R.id.iv_slip_preview)

        btnBack.setOnClickListener { finish() }

        billId = intent.getStringExtra("BILL_ID") ?: ""
        imageUriString = intent.getStringExtra("IMAGE_URI")
        billAmount = intent.getDoubleExtra("BILL_AMOUNT", 0.0)

        if (imageUriString != null) {
            ivSlip.setImageURI(Uri.parse(imageUriString))
        }

        updateBillInfo(billAmount)

        btnConfirm.setOnClickListener {
            btnConfirm.isEnabled = false
            btnConfirm.text = "กำลังอัพโหลด..."
            uploadSlipAndUpdateBill()
        }
    }

    private fun updateBillInfo(amount: Double) {
        findViewById<TextView>(R.id.tv_amount_value).text = "${amount.toInt()} บาท"

        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())

        findViewById<TextView>(R.id.tv_date_value).text = dateFormat.format(currentDate)
        findViewById<TextView>(R.id.tv_time_value).text = "${timeFormat.format(currentDate)} น."
    }

    private fun uploadSlipAndUpdateBill() {
        val uri = imageUriString?.let { Uri.parse(it) }
        if (uri == null || billId.isEmpty()) {
            Toast.makeText(this, "ข้อมูลไม่ครบ กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
            resetButton()
            return
        }

        // อัพรูปไป Firebase Storage
        val fileName = "slips/${billId}_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                // ดึง download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // อัปเดต bill ใน Firestore
                    db.collection("bills").document(billId)
                        .update(
                            mapOf(
                                "slipUrl" to downloadUrl.toString(),
                                "paymentDate" to Timestamp.now(),
                                "status" to "pending"
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "แจ้งชำระเงินเรียบร้อยแล้ว!",
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(this, HomeUserActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "อัปเดตบิลไม่สำเร็จ: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            resetButton()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "อัพโหลดสลิปไม่สำเร็จ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetButton()
            }
    }

    private fun resetButton() {
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        btnConfirm.isEnabled = true
        btnConfirm.text = "ยืนยันการชำระเงิน"
    }
}
