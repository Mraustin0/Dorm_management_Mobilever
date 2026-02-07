package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R

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

        // ปุ่มออกจากระบบ (จำลองกลับไปหน้า Home)
        findViewById<CardView>(R.id.btn_logout).setOnClickListener {
            val intent = Intent(this, HomeUserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}