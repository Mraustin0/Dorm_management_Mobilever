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

        // ปุ่มคัดลอกที่อยู่
        findViewById<ImageView>(R.id.btn_copy_address)?.setOnClickListener {
            val tvAddress = findViewById<TextView>(R.id.tv_address)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Dorm Address", tvAddress.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกที่อยู่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
        }

        // ปุ่มโทร
        findViewById<LinearLayout>(R.id.btn_call)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0922787188")
            startActivity(intent)
        }

        // ปุ่มอีเมล
        findViewById<LinearLayout>(R.id.btn_email)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:goldenkeyapartment@gmail.com")
            startActivity(intent)
        }

        // ปุ่มดูเอกสารสัญญาเช่า
        findViewById<TextView>(R.id.btn_view_contract)?.setOnClickListener {
            val intent = Intent(this, ContractListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_notifications
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
                R.id.navigation_notifications -> true
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
