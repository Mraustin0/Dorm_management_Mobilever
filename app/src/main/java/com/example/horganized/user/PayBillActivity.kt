package com.example.horganized.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.horganized.R

class PayBillActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private lateinit var tvUploadLabel: TextView
    private var billId: String = ""
    private var billAmount: Double = 0.0
    private var billMonth: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            
            // ใช้ Glide ดึงรูปมาแสดงเพื่อให้ภาพไม่แตกและจัดการ Memory ได้ดี
            Glide.with(this)
                .load(it)
                .into(ivPreview)

            // ปรับขนาด ImageView ให้เต็ม CardView เมื่อเลือกรูปแล้ว
            val params = ivPreview.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            ivPreview.layoutParams = params
            ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            
            tvUploadLabel.text = "เปลี่ยนรูปภาพ"
            tvUploadLabel.visibility = View.GONE // ซ่อนข้อความไปเลยเพื่อให้เห็นสลิปชัดๆ
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill)

        billId = intent.getStringExtra("BILL_ID") ?: ""
        billAmount = intent.getDoubleExtra("BILL_AMOUNT", 0.0)
        billMonth = intent.getStringExtra("BILL_MONTH") ?: ""

        ivPreview = findViewById(R.id.iv_preview)
        tvUploadLabel = findViewById(R.id.tv_upload_label)
        
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnCopy = findViewById<ImageView>(R.id.btn_copy_acc)
        val btnSelectImage = findViewById<androidx.cardview.widget.CardView>(R.id.btn_select_image)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val tvAmount = findViewById<TextView>(R.id.tv_amount)

        tvAmount.text = "${String.format("%,.2f", billAmount)} บาท"

        btnBack.setOnClickListener { finish() }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Account Number", "001-443-5771")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกเลขบัญชีแล้ว", Toast.LENGTH_SHORT).show()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
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
