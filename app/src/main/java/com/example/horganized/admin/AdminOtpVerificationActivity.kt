package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminOtpVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_otp_verification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_otp).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_verify_otp).setOnClickListener {
            // จำลองการตรวจสอบ OTP สำเร็จ
            val intent = Intent(this, AdminResetPasswordActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.tv_resend).setOnClickListener {
            Toast.makeText(this, "OTP has been resent", Toast.LENGTH_SHORT).show()
        }
    }
}