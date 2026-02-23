package com.example.horganized.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val etCurrent = findViewById<EditText>(R.id.et_current_password)
        val etNew = findViewById<EditText>(R.id.et_new_password)
        val etConfirm = findViewById<EditText>(R.id.et_confirm_password)
        val btnSave = findViewById<AppCompatButton>(R.id.btn_get_otp)

        findViewById<ImageView>(R.id.btn_back_change_password).setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val currentPass = etCurrent.text.toString().trim()
            val newPass = etNew.text.toString().trim()
            val confirmPass = etConfirm.text.toString().trim()

            // Validate
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                Toast.makeText(this, "รหัสผ่านใหม่ต้องมีอย่างน้อย 6 ตัวอักษร", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                Toast.makeText(this, "รหัสผ่านใหม่ไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val email = user?.email

            if (user == null || email == null) {
                Toast.makeText(this, "ไม่พบข้อมูลผู้ใช้ กรุณา login ใหม่", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "กำลังบันทึก..."

            // Re-authenticate ก่อน เพราะ Firebase ต้องการยืนยันตัวตนก่อนเปลี่ยน password
            val credential = EmailAuthProvider.getCredential(email, currentPass)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Re-auth สำเร็จ → เปลี่ยน password ได้เลย
                    user.updatePassword(newPass)
                        .addOnSuccessListener {
                            Toast.makeText(this, "เปลี่ยนรหัสผ่านเรียบร้อยแล้ว", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            resetButton(btnSave)
                            Toast.makeText(this, "เปลี่ยนรหัสผ่านไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    resetButton(btnSave)
                    val msg = when {
                        e.message?.contains("password is invalid") == true ||
                        e.message?.contains("INVALID_PASSWORD") == true ||
                        e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                            "รหัสผ่านปัจจุบันไม่ถูกต้อง"
                        e.message?.contains("network") == true ->
                            "ไม่มีการเชื่อมต่ออินเทอร์เน็ต"
                        else -> "เกิดข้อผิดพลาด: ${e.message}"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun resetButton(btn: AppCompatButton) {
        btn.isEnabled = true
        btn.text = "บันทึกรหัสผ่านใหม่"
    }
}
