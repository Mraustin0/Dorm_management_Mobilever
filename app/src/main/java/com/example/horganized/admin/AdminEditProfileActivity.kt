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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AdminEditProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private lateinit var etUsername  : EditText
    private lateinit var etFirstName : EditText
    private lateinit var etLastName  : EditText
    private lateinit var etEmail     : EditText
    private lateinit var etTel       : EditText
    private lateinit var btnSubmit   : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etUsername  = findViewById(R.id.et_edit_username)
        etFirstName = findViewById(R.id.et_edit_first_name)
        etLastName  = findViewById(R.id.et_edit_last_name)
        etEmail     = findViewById(R.id.et_edit_email)
        etTel       = findViewById(R.id.et_edit_tel)
        btnSubmit   = findViewById(R.id.btn_submit_profile)

        findViewById<ImageView>(R.id.btn_back_edit_profile).setOnClickListener {
            finish()
        }

        loadProfile()

        btnSubmit.setOnClickListener {
            saveProfile()
        }
    }

    /** โหลดข้อมูล admin จาก Firestore แล้วแสดงในช่อง */
    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        btnSubmit.isEnabled = false

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etUsername.setText(doc.getString("username") ?: "")
                    etFirstName.setText(doc.getString("firstName") ?: doc.getString("name") ?: "")
                    etLastName.setText(doc.getString("lastName") ?: "")
                    etEmail.setText(doc.getString("email") ?: auth.currentUser?.email ?: "")
                    etTel.setText(doc.getString("tel") ?: doc.getString("phone") ?: "")
                } else {
                    // ยังไม่มี doc → prefill email จาก Auth
                    etEmail.setText(auth.currentUser?.email ?: "")
                }
                btnSubmit.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
            }
    }

    /** บันทึกข้อมูลกลับ Firestore */
    private fun saveProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "กรุณา login ก่อน", Toast.LENGTH_SHORT).show()
            return
        }

        val username  = etUsername.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName  = etLastName.text.toString().trim()
        val email     = etEmail.text.toString().trim()
        val tel       = etTel.text.toString().trim()

        if (firstName.isEmpty()) {
            etFirstName.error = "กรุณากรอกชื่อ"
            etFirstName.requestFocus()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "กำลังบันทึก..."

        val data = mapOf(
            "username"  to username,
            "firstName" to firstName,
            "lastName"  to lastName,
            "name"      to "$firstName $lastName".trim(),  // เก็บ name รวมเพื่อ backward compat
            "email"     to email,
            "tel"       to tel,
            "phone"     to tel  // เก็บทั้ง 2 ชื่อ field เผื่อ code อื่นใช้
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())   // merge = ไม่ลบ field อื่นที่มีอยู่
            .addOnSuccessListener {
                Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "บันทึกไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit"
            }
    }
}