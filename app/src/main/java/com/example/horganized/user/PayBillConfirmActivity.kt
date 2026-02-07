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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayBillConfirmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill_confirm)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val ivSlip = findViewById<ImageView>(R.id.iv_slip_preview)

        btnBack.setOnClickListener { finish() }

        // รับข้อมูลจากหน้าก่อนหน้า
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val billAmount = intent.getDoubleExtra("BILL_AMOUNT", 0.0)

        if (imageUriString != null) {
            ivSlip.setImageURI(Uri.parse(imageUriString))
        }

        // อัพเดท UI ด้วยข้อมูลจริง
        updateBillInfo(billAmount)

        btnConfirm.setOnClickListener {
            // ในอนาคตสามารถใส่โค้ดอัพโหลดรูปขึ้น Firebase Storage ได้ที่นี่
            Toast.makeText(this, "แจ้งชำระเงินเรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()

            // กลับไปหน้า Home และล้างหน้าจอเก่าออก
            val intent = Intent(this, HomeUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun updateBillInfo(amount: Double) {
        // แสดงยอดเงิน
        findViewById<TextView>(R.id.tv_amount_value).text = "${amount.toInt()} บาท"

        // แสดงวันที่และเวลาปัจจุบัน
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())

        findViewById<TextView>(R.id.tv_date_value).text = dateFormat.format(currentDate)
        findViewById<TextView>(R.id.tv_time_value).text = "${timeFormat.format(currentDate)} น."
    }
}