package com.example.horganized.user

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R

class PayBillActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private var billId: String = ""
    private var billAmount: Double = 0.0
    private var billMonth: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill)

        // รับข้อมูลบิลจากหน้าก่อนหน้า
        billId = intent.getStringExtra("BILL_ID") ?: ""
        billAmount = intent.getDoubleExtra("BILL_AMOUNT", 0.0)
        billMonth = intent.getStringExtra("BILL_MONTH") ?: ""

        ivPreview = findViewById(R.id.iv_preview)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnCopy = findViewById<ImageView>(R.id.btn_copy_acc)
        val btnSelectImage = findViewById<androidx.cardview.widget.CardView>(R.id.btn_select_image)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val tvAccNumber = findViewById<TextView>(R.id.tv_acc_number)
        val tvAmount = findViewById<TextView>(R.id.tv_amount)

        // แสดงยอดเงินจริงจากบิล (user แก้ไขไม่ได้)
        tvAmount.text = "${String.format("%,.0f", billAmount)} บาท"

        btnBack.setOnClickListener { finish() }

        // ฟังก์ชันคัดลอกเลขบัญชี
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Account Number", "001-443-5771")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกเลขบัญชีแล้ว", Toast.LENGTH_SHORT).show()
        }

        // ฟังก์ชันเลือกรูปจากเครื่อง
        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                ivPreview.setImageURI(selectedImageUri)
            }
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnNext.setOnClickListener {
            if (selectedImageUri != null) {
                val intent = Intent(this, PayBillConfirmActivity::class.java)
                intent.putExtra("BILL_ID", billId)
                intent.putExtra("IMAGE_URI", selectedImageUri.toString())
                intent.putExtra("BILL_AMOUNT", billAmount)
                intent.putExtra("BILL_MONTH", billMonth)
                startActivity(intent)
            } else {
                Toast.makeText(this, "กรุณาอัพโหลดหลักฐานการชำระเงิน", Toast.LENGTH_SHORT).show()
            }
        }
    }
}