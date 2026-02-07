package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

        // ปุ่มกระดิ่งแจ้งเตือน
        findViewById<ImageView>(R.id.notification_icon).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // ปุ่มดูและจ่ายบิล
        val viewAndPayBillButton = findViewById<Button>(R.id.btn_view_pay_bill)
        viewAndPayBillButton.setOnClickListener {
            startActivity(Intent(this, DetailBillActivity::class.java))
        }

        // จัดการ Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_bill -> {
                    startActivity(Intent(this, DetailBillActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_notifications -> {
                    startActivity(Intent(this, DormInfoActivity::class.java))
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