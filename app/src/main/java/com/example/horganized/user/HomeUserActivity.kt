package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

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
                    true
                }
                R.id.navigation_notifications -> { // สมมติว่าเป็นหน้าข้อมูลหอพัก
                    startActivity(Intent(this, DormInfoActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}