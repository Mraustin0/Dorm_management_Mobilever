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

        // ปุ่มเมนู 3 ขีด (Header)
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        menuIcon?.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        menuIcon?.applyHoverAnimation()

        // ปุ่มกระดิ่งแจ้งเตือน (Header)
        val notificationIcon = findViewById<ImageView>(R.id.notification_icon)
        notificationIcon?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        notificationIcon?.applyHoverAnimation()

        // ปุ่ม "ดูและจ่ายบิล"
        findViewById<Button>(R.id.btn_view_pay_bill)?.setOnClickListener {
            val intent = Intent(this, PayBillActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Bill Card
        findViewById<CardView>(R.id.bill_card)?.applyHoverAnimation()

        // ปุ่มบริการ - พัสดุ
        val cardParcel = findViewById<CardView>(R.id.card_parcel)
        cardParcel?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        cardParcel?.applyHoverAnimation()

        // ปุ่มบริการ - แจ้งซ่อม
        val cardRepair = findViewById<CardView>(R.id.card_repair)
        cardRepair?.setOnClickListener {
            val intent = Intent(this, RepairActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        cardRepair?.applyHoverAnimation()

        // ปุ่มบริการ - ประกาศ
        val cardAnnouncement = findViewById<CardView>(R.id.card_announcement)
        cardAnnouncement?.setOnClickListener {
            val intent = Intent(this, AnnouncementActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        cardAnnouncement?.applyHoverAnimation()

        // ปุ่มบริการ - แจ้งย้ายออก
        val cardMoveOut = findViewById<CardView>(R.id.card_move_out)
        cardMoveOut?.setOnClickListener {
            val intent = Intent(this, ContractListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        cardMoveOut?.applyHoverAnimation()

        loadUserData()
        loadBillData()
        setupBottomNavigation()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.applyHoverAnimation() {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false // return false so onClickListener still works
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
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
                    bill?.let { updateBillUI(it) }
                }
            }
    }

    private fun updateBillUI(bill: Bill) {
        val btnPay = findViewById<Button>(R.id.btn_view_pay_bill)
        val tvAmount = findViewById<TextView>(R.id.outstanding_amount)

        tvAmount?.text = String.format("%,.2f บาท", bill.amount)

        // แสดงข้อมูลการใช้งาน
        findViewById<TextView>(R.id.tv_room_price)?.text = String.format("%,.0f บาท", bill.details.roomPrice)
        findViewById<TextView>(R.id.tv_electric_price)?.text = "${bill.details.electricUnit} หน่วย = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
        findViewById<TextView>(R.id.tv_water_price)?.text = String.format("%,.0f บาท", bill.details.waterPrice)
        findViewById<TextView>(R.id.tv_other_price)?.text = String.format("%,.0f บาท", bill.details.otherPrice)
        findViewById<TextView>(R.id.tv_total_price)?.text = String.format("%,.2f บาท", bill.amount)

        if (bill.isPaid) {
            btnPay?.text = "จ่ายแล้ว"
            btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#1B9E44")
            )
            btnPay?.isEnabled = false
        } else {
            btnPay?.text = "ดูและจ่ายบิล"
            btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#E53935")
            )
            btnPay?.isEnabled = true
        }
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
