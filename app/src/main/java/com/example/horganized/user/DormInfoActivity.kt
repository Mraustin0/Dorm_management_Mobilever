package com.example.horganized.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DormInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dorm_info)

        val tvAddress = findViewById<TextView>(R.id.tv_address)
        val btnCall = findViewById<LinearLayout>(R.id.btn_call)

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { 
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มกระดิ่งแจ้งเตือน
        findViewById<ImageView>(R.id.notification_icon)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มเมนู 3 ขีด
        findViewById<ImageView>(R.id.menu_icon)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ฟังก์ชันคัดลอกที่อยู่ (เปลี่ยนมาใช้กดที่ตัวที่อยู่แทนเนื่องจากลบปุ่มเก่าออก)
        tvAddress?.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Dorm Address", tvAddress.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกที่อยู่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
        }

        btnCall?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0922787188")
            startActivity(intent)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = 0 // ลบการเลือกสถานะหากหน้านี้ไม่ได้อยู่ในเมนูหลัก หรือเลือกไอคอนที่เหมาะสม
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeUserActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_bill -> {
                    startActivity(Intent(this, DetailBillActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_notifications -> {
                    // ถ้าคุณอยากให้ไอคอนแจ้งเตือนใน Nav เปิดหน้า NotificationActivity
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}