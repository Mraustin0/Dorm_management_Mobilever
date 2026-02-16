package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class ChangePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_change_password).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_get_otp).setOnClickListener {
            Toast.makeText(this, "OTP sent to your phone number", Toast.LENGTH_SHORT).show()
            
            // เชื่อมต่อไปยังหน้ากรอก OTP
            val intent = Intent(this, AdminOtpVerificationActivity::class.java)
            startActivity(intent)
        }
    }
}