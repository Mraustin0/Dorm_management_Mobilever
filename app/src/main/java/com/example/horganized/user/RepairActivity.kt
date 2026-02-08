package com.example.horganized.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R

class RepairActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair)

        // แก้ไข ID ให้ตรงกับไฟล์ activity_repair.xml
        ivPreview = findViewById(R.id.iv_preview)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSend = findViewById<TextView>(R.id.btn_send_repair)
        val btnSelectImage = findViewById<CardView>(R.id.btn_select_image)

        btnBack.setOnClickListener { finish() }

        // เลือกรูปภาพ
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

        // ปุ่มส่งแจ้งซ่อม
        btnSend.setOnClickListener {
            Toast.makeText(this, "ส่งข้อมูลแจ้งซ่อมเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}