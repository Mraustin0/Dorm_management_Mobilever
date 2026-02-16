package com.example.horganized.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminCheckSlipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_check_slip)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_check_slip).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_view_slip).setOnClickListener {
            Toast.makeText(this, "กำลังเปิดไฟล์สลิป...", Toast.LENGTH_SHORT).show()
        }

        findViewById<AppCompatButton>(R.id.btn_confirm_payment).setOnClickListener {
            Toast.makeText(this, "ยืนยันการชำระเงินเรียบร้อย", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}