package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

        // ระบบซ่อน/แสดงข้อมูลการใช้งาน
        val btnToggle = findViewById<RelativeLayout>(R.id.btn_toggle_usage)
        val layoutDetails = findViewById<LinearLayout>(R.id.layout_usage_details)
        val ivChevron = findViewById<ImageView>(R.id.iv_usage_chevron)

        btnToggle?.setOnClickListener {
            if (layoutDetails?.visibility == View.GONE) {
                layoutDetails.visibility = View.VISIBLE
                ivChevron?.setImageResource(R.drawable.ic_chevron_up_gg)
            } else {
                layoutDetails?.visibility = View.GONE
                ivChevron?.setImageResource(R.drawable.ic_chevron_down_gg)
            }
        }

        // ปุ่มเมนู 3 ขีด (Header)
        findViewById<ImageView>(R.id.menu_icon)?.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มกระดิ่งแจ้งเตือน (Header)
        findViewById<ImageView>(R.id.notification_icon)?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่ม "ดูและจ่ายบิล" ในหน้าหลัก -> ให้ไปหน้าแนบสลิปทันที
        findViewById<Button>(R.id.btn_view_pay_bill)?.setOnClickListener {
            val intent = Intent(this, PayBillActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
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