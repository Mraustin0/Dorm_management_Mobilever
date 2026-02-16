package com.example.horganized.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.LoginActivity
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // ปุ่มย้อนกลับ
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
        btnBack.applyHoverAnimation()

        // เมนูแก้ไขข้อมูลส่วนตัว
        val menuEditProfile = findViewById<CardView>(R.id.menu_edit_profile)
        menuEditProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileEditActivity::class.java))
        }
        menuEditProfile.applyHoverAnimation()

        // เมนูเปลี่ยนรหัสผ่าน (Placeholder)
        val menuChangePassword = findViewById<CardView>(R.id.menu_change_password)
        menuChangePassword.setOnClickListener {
            Toast.makeText(this, "ฟีเจอร์เปลี่ยนรหัสผ่านกำลังตามมาเร็วๆ นี้", Toast.LENGTH_SHORT).show()
        }
        menuChangePassword.applyHoverAnimation()

        // เมนูเอกสาร (Placeholder)
        val menuDocuments = findViewById<CardView>(R.id.menu_documents)
        menuDocuments.setOnClickListener {
            Toast.makeText(this, "กำลังเปิดเอกสารสัญญาเช่า...", Toast.LENGTH_SHORT).show()
        }
        menuDocuments.applyHoverAnimation()

        // เมนูแจ้งย้ายออก (เชื่อมกับหน้าแจ้งซ่อมชั่วคราว หรือแสดงข้อความ)
        val menuMoveOut = findViewById<CardView>(R.id.menu_move_out)
        menuMoveOut.setOnClickListener {
            Toast.makeText(this, "ติดต่อเจ้าหน้าที่เพื่อแจ้งย้ายออก", Toast.LENGTH_SHORT).show()
        }
        menuMoveOut.applyHoverAnimation()

        // ปุ่มออกจากระบบ
        val btnLogout = findViewById<CardView>(R.id.btn_logout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        btnLogout.applyHoverAnimation()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.applyHoverAnimation() {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
    }
}