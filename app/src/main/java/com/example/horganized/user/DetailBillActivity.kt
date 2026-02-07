package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.example.horganized.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailBillActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_bill)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ปุ่มกระดิ่งแจ้งเตือน
        findViewById<ImageView>(R.id.notification_icon)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // ปุ่มจ่ายเงิน
        findViewById<Button>(R.id.btn_pay_now)?.setOnClickListener {
            startActivity(Intent(this, PayBillActivity::class.java))
        }

        setupBottomNavigation()
        fetchLatestBill()
    }

    private fun fetchLatestBill() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("bills")
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val bill = document.toObject(Bill::class.java)
                    updateUI(bill)
                }
            }
    }

    private fun updateUI(bill: Bill) {
        findViewById<TextView>(R.id.tv_bill_month_title).text = "บิลประจำเดือน ${bill.monthYear}"
        findViewById<TextView>(R.id.tv_total_amount_red).text = "${bill.totalAmount} บาท"
        // ... (ส่วนอื่นๆ ของ UI)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_bill
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeUserActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_bill -> true
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