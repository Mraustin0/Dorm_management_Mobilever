package com.example.horganized.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R

class DormInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dorm_info)

        val tvAddress = findViewById<TextView>(R.id.tv_address)
        val btnCopy = findViewById<ImageView>(R.id.btn_copy_address)
        val btnCall = findViewById<LinearLayout>(R.id.btn_call)
        val btnEmail = findViewById<LinearLayout>(R.id.btn_email)

        // ฟังก์ชันคัดลอกที่อยู่
        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Dorm Address", tvAddress.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกที่อยู่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
        }

        // ฟังก์ชันโทรออก
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0922787188")
            startActivity(intent)
        }

        // ฟังก์ชันส่ง Email
        btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:goldenkeyapartment@gmail.com")
            startActivity(intent)
        }
    }
}