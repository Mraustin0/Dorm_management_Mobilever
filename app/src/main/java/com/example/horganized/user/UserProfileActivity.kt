package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
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
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // เมนูแก้ไขข้อมูลส่วนตัว
        findViewById<CardView>(R.id.menu_edit_profile).setOnClickListener {
            startActivity(Intent(this, UserProfileEditActivity::class.java))
        }

        // เมนูเปลี่ยนรหัสผ่าน (Placeholder)
        findViewById<CardView>(R.id.menu_change_password).setOnClickListener {
            Toast.makeText(this, "ฟีเจอร์เปลี่ยนรหัสผ่านกำลังตามมาเร็วๆ นี้", Toast.LENGTH_SHORT).show()
        }

        // เมนูเอกสาร (Placeholder)
        findViewById<CardView>(R.id.menu_documents).setOnClickListener {
            Toast.makeText(this, "กำลังเปิดเอกสารสัญญาเช่า...", Toast.LENGTH_SHORT).show()
        }

        // เมนูแจ้งย้ายออก (เชื่อมกับหน้าแจ้งซ่อมชั่วคราว หรือแสดงข้อความ)
        findViewById<CardView>(R.id.menu_move_out).setOnClickListener {
            Toast.makeText(this, "ติดต่อเจ้าหน้าที่เพื่อแจ้งย้ายออก", Toast.LENGTH_SHORT).show()
        }

        // ปุ่มออกจากระบบ
        findViewById<CardView>(R.id.btn_logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}