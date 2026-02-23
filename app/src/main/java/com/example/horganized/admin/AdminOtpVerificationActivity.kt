package com.example.horganized.admin

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth

class AdminOtpVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_otp_verification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_reset_email)
        val btnSend = findViewById<AppCompatButton>(R.id.btn_verify_otp)
        val tvStatus = findViewById<TextView>(R.id.tv_resend)

        // ถ้ามี email ส่งมาจาก AdminLoginActivity ให้ prefill ไว้เลย
        val prefillEmail = intent.getStringExtra("ADMIN_EMAIL") ?: ""
        if (prefillEmail.isNotEmpty()) {
            etEmail.setText(prefillEmail)
        }

        findViewById<ImageView>(R.id.btn_back_otp).setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกอีเมล", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "รูปแบบอีเมลไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // disable ปุ่มกัน spam กด
            btnSend.isEnabled = false
            btnSend.text = "กำลังส่ง..."
            tvStatus.visibility = View.GONE

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    tvStatus.text = "✓ ส่งลิงก์รีเซ็ตรหัสผ่านไปที่ $email แล้ว\nกรุณาตรวจสอบกล่องจดหมาย"
                    tvStatus.setTextColor(0xFF1B9E44.toInt())
                    tvStatus.visibility = View.VISIBLE
                    btnSend.text = "ส่งอีกครั้ง"
                    btnSend.isEnabled = true
                    Toast.makeText(this, "ส่ง email รีเซ็ตรหัสผ่านเรียบร้อย", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    btnSend.text = "ส่งลิงก์รีเซ็ตรหัสผ่าน"
                    btnSend.isEnabled = true

                    val msg = when {
                        e.message?.contains("no user record") == true ||
                        e.message?.contains("user-not-found") == true ->
                            "ไม่พบบัญชีที่ใช้อีเมลนี้"
                        e.message?.contains("badly formatted") == true ||
                        e.message?.contains("invalid-email") == true ->
                            "รูปแบบอีเมลไม่ถูกต้อง"
                        e.message?.contains("network") == true ->
                            "ไม่มีการเชื่อมต่ออินเทอร์เน็ต"
                        else -> "เกิดข้อผิดพลาด: ${e.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }
}
