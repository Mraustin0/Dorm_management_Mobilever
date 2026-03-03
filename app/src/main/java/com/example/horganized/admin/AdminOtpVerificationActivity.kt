package com.example.horganized.admin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
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
        val tvError = findViewById<TextView>(R.id.tv_resend)
        val cardSuccess = findViewById<CardView>(R.id.card_success)
        val layoutInput = findViewById<LinearLayout>(R.id.ll_email_input)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle_otp)

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

            btnSend.isEnabled = false
            btnSend.text = "กำลังส่งลิงก์..."
            tvError.visibility = View.GONE

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    // แสดง UI สำเร็จแบบสวยงาม
                    cardSuccess.visibility = View.VISIBLE
                    
                    // ซ่อนส่วน Input เพื่อให้ดูคลีนขึ้น
                    layoutInput.visibility = View.GONE
                    btnSend.visibility = View.GONE
                    tvSubtitle.visibility = View.GONE
                    
                    // หน่วงเวลา 3 วินาทีแล้วกลับหน้า Login
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!isFinishing) {
                            finish()
                        }
                    }, 3500)
                }
                .addOnFailureListener { e ->
                    btnSend.isEnabled = true
                    btnSend.text = "ส่งลิงก์รีเซ็ตรหัสผ่าน"
                    val msg = when {
                        e.message?.contains("network") == true -> "ไม่มีการเชื่อมต่ออินเทอร์เน็ต"
                        e.message?.contains("user-not-found") == true -> "ไม่พบอีเมลนี้ในระบบ"
                        else -> "เกิดข้อผิดพลาด: ${e.message}"
                    }
                    tvError.text = "✗ $msg"
                    tvError.visibility = View.VISIBLE
                }
        }
    }
}
