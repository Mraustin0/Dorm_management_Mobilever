package com.example.horganized

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.user.HomeUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<RelativeLayout>(R.id.btn_login_submit_custom)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // บัญชี Mock สำหรับทดสอบหน้าจอ
            if (email == "tin" && password == "1234") {
                Toast.makeText(this, "Mock Login Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeUserActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkUserRole(auth.currentUser?.uid)
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserRole(uid: String?) {
        if (uid == null) return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show()
                    } else {
                        startActivity(Intent(this, HomeUserActivity::class.java))
                    }
                    finish()
                }
            }
    }
}