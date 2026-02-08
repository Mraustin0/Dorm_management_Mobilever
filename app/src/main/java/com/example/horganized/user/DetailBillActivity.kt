package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
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

        setupToggles()
        setupBottomNavigation()
        fetchLatestBill()

        // ปุ่มกระดิ่งแจ้งเตือน
        findViewById<ImageView>(R.id.notification_icon).setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มเมนู 3 ขีด
        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { 
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // ปุ่มจ่ายเงิน
        findViewById<Button>(R.id.btn_pay_now).setOnClickListener {
            val intent = Intent(this, PayBillActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupToggles() {
        setupSingleToggle(R.id.btn_toggle_april, R.id.layout_content_april, R.id.iv_chevron_april)
        setupSingleToggle(R.id.btn_toggle_march, R.id.layout_content_march, R.id.iv_chevron_march)
        setupSingleToggle(R.id.btn_toggle_feb, R.id.layout_content_feb, R.id.iv_chevron_feb)
    }

    private fun setupSingleToggle(btnId: Int, contentId: Int, chevronId: Int) {
        val btn = findViewById<RelativeLayout>(btnId)
        val content = findViewById<LinearLayout>(contentId)
        val chevron = findViewById<ImageView>(chevronId)

        btn?.setOnClickListener {
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                chevron.setImageResource(R.drawable.ic_chevron_down_gg)
            } else {
                content.visibility = View.VISIBLE
                chevron.setImageResource(R.drawable.ic_chevron_up_gg)
            }
        }
    }

    private fun fetchLatestBill() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("bills").whereEqualTo("userId", userId).limit(1)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val bill = snapshots.documents[0].toObject(Bill::class.java)
                    bill?.let { updateUI(it) }
                }
            }
    }

    private fun updateUI(bill: Bill) {
        val btnPay = findViewById<Button>(R.id.btn_pay_now)
        if (bill.isPaid) {
            btnPay.text = "จ่ายแล้ว"
            btnPay.setBackgroundColor(android.graphics.Color.parseColor("#1B9E44"))
            btnPay.isEnabled = false
        } else {
            btnPay.text = "จ่ายเลย"
            btnPay.setBackgroundColor(android.graphics.Color.parseColor("#E53935"))
            btnPay.isEnabled = true
        }
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
