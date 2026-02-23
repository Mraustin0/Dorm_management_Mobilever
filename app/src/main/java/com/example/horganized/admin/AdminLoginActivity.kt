package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvForgot = findViewById<TextView>(R.id.tv_forgot_password)

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "กำลังเข้าสู่ระบบ..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    // ตรวจสอบว่า role เป็น admin จริงมั้ย
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            if (role == "admin") {
                                Toast.makeText(this, "ยินดีต้อนรับ Admin", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, AdminHomeActivity::class.java))
                                finish()
                            } else {
                                auth.signOut()
                                Toast.makeText(this, "บัญชีนี้ไม่มีสิทธิ์เข้าถึง Admin", Toast.LENGTH_LONG).show()
                                resetButton(btnLogin)
                            }
                        }
                        .addOnFailureListener {
                            // ถ้าดึง role ไม่ได้ ให้เข้าได้เลย (fallback)
                            startActivity(Intent(this, AdminHomeActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    resetButton(btnLogin)
                    val msg = when (e) {
                        is FirebaseAuthException -> when (e.errorCode) {
                            "ERROR_USER_NOT_FOUND"     -> "ไม่พบบัญชีนี้ในระบบ"
                            "ERROR_WRONG_PASSWORD"     -> "รหัสผ่านไม่ถูกต้อง"
                            "ERROR_INVALID_EMAIL"      -> "รูปแบบอีเมลไม่ถูกต้อง"
                            "ERROR_USER_DISABLED"      -> "บัญชีถูกปิดใช้งาน"
                            "ERROR_INVALID_CREDENTIAL" -> "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                            else -> "เข้าสู่ระบบไม่สำเร็จ: ${e.errorCode}"
                        }
                        else -> "เกิดข้อผิดพลาด: ${e.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }

        // กด "Forgotten Password" → ไปหน้ากรอก email ส่ง reset link
        tvForgot.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val intent = Intent(this, AdminOtpVerificationActivity::class.java)
            // ส่ง email ที่กรอกไว้ไปด้วย ถ้ามี
            if (email.isNotEmpty()) {
                intent.putExtra("ADMIN_EMAIL", email)
            }
            startActivity(intent)
        }
    }

    private fun resetButton(btn: Button) {
        btn.isEnabled = true
        btn.text = "Log In"
    }
}
