package com.example.horganized.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R
import com.example.horganized.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

        // ปุ่มเมนู 3 ขีด
        findViewById<ImageView>(R.id.menu_icon)?.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // ปุ่มกระดิ่งแจ้งเตือน
        findViewById<ImageView>(R.id.notification_icon)?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // ปุ่มบริการ - แจ้งซ่อม
        findViewById<CardView>(R.id.card_repair)?.setOnClickListener {
            startActivity(Intent(this, RepairActivity::class.java))
        }

        // ปุ่มบริการ - ประกาศ
        findViewById<CardView>(R.id.card_announcement)?.setOnClickListener {
            startActivity(Intent(this, AnnouncementActivity::class.java))
        }

        // ปุ่มบริการ - แจ้งย้ายออก
        findViewById<CardView>(R.id.card_move_out)?.setOnClickListener {
            startActivity(Intent(this, UserMoveOutActivity::class.java))
        }

        // ปุ่มดูและจ่ายบิล
        findViewById<Button>(R.id.btn_view_pay_bill)?.setOnClickListener {
            startActivity(Intent(this, DetailBillActivity::class.java))
        }

        loadUserData()
        loadBillData()
        setupBottomNavigation()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val name = doc.getString("name") ?: ""
                val room = doc.getString("roomNumber") ?: ""
                findViewById<TextView>(R.id.user_name)?.text = "$name ห้อง $room"
            }
        }
    }

    private fun loadBillData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("bills").whereEqualTo("userId", uid)
            .orderBy("dueDate", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val bill = snapshots.documents[0].toObject(Bill::class.java)
                    if (bill != null) {
                        showHasBill()
                        updateBillUI(bill)
                    } else {
                        showNoBill()
                    }
                } else {
                    showNoBill()
                }
            }
    }

    private fun showNoBill() {
        findViewById<View>(R.id.tv_no_bill)?.visibility = View.VISIBLE
        findViewById<View>(R.id.layout_bill_info)?.visibility = View.GONE
        findViewById<View>(R.id.btn_toggle_usage)?.visibility = View.GONE
        findViewById<View>(R.id.layout_usage_details)?.visibility = View.GONE
    }

    private fun showHasBill() {
        findViewById<View>(R.id.tv_no_bill)?.visibility = View.GONE
        findViewById<View>(R.id.layout_bill_info)?.visibility = View.VISIBLE
        findViewById<View>(R.id.btn_toggle_usage)?.visibility = View.VISIBLE
    }

    private fun updateBillUI(bill: Bill) {
        val btnPay = findViewById<Button>(R.id.btn_view_pay_bill)
        val tvAmount = findViewById<TextView>(R.id.outstanding_amount)

        tvAmount?.text = String.format("%,.2f บาท", bill.amount)

        // แสดงรายละเอียดการใช้งาน
        findViewById<TextView>(R.id.tv_room_price)?.text = String.format("%,.0f บาท", bill.details.roomPrice)
        findViewById<TextView>(R.id.tv_electric_price)?.text = "${bill.details.electricUnit} หน่วย = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
        findViewById<TextView>(R.id.tv_water_price)?.text = "${bill.details.waterUnit} หน่วย = ${String.format("%,.0f", bill.details.waterPrice)} บาท"
        findViewById<TextView>(R.id.tv_other_price)?.text = String.format("%,.0f บาท", bill.details.otherPrice)
        findViewById<TextView>(R.id.tv_total_price)?.text = String.format("%,.2f บาท", bill.amount)

        if (bill.isPaid) {
            btnPay?.text = "จ่ายแล้ว"
            btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1B9E44"))
            btnPay?.isEnabled = false
        } else {
            // แก้ไขข้อความปุ่มถ้ายังไม่ชำระ
            btnPay?.text = "ยังไม่ชำระ"
            btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935"))
            btnPay?.isEnabled = true
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_bill -> { startActivity(Intent(this, DetailBillActivity::class.java)); true }
                R.id.navigation_notifications -> { startActivity(Intent(this, DormInfoActivity::class.java)); true }
                R.id.navigation_chat -> { startActivity(Intent(this, ChatActivity::class.java)); true }
                else -> false
            }
        }
    }
}
