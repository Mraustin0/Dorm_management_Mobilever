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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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

        // ปุ่มบริการ - เอกสารสัญญาเช่า
        findViewById<CardView>(R.id.card_parcel)?.setOnClickListener {
            startActivity(Intent(this, ContractListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // ปุ่มบริการ - แจ้งซ่อม
        findViewById<CardView>(R.id.card_repair)?.setOnClickListener {
            startActivity(Intent(this, RepairActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // ปุ่มบริการ - ประกาศ
        findViewById<CardView>(R.id.card_announcement)?.setOnClickListener {
            startActivity(Intent(this, AnnouncementActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // ปุ่มบริการ - แจ้งย้ายออก
        findViewById<CardView>(R.id.card_move_out)?.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("move_out_requests")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        startActivity(Intent(this, MoveOutHistoryActivity::class.java))
                    } else {
                        startActivity(Intent(this, UserMoveOutActivity::class.java))
                    }
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
        }

        loadUserData()
        loadBillData()
        setupBottomNavigation()
        observeUnreadChat()
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
            false
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val name = doc.getString("name") ?: ""
                val room = doc.getString("roomNumber") ?: ""
                findViewById<TextView>(R.id.user_name)?.text = "$name ห้อง $room"
                
                val photoUrl = doc.getString("photoUrl")
                val ivAvatar = findViewById<ImageView>(R.id.user_avatar)
                if (!photoUrl.isNullOrEmpty() && ivAvatar != null) {
                    Glide.with(this).load(photoUrl).transform(CircleCrop()).into(ivAvatar)
                }
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
                    bill?.let { 
                        findViewById<TextView>(R.id.tv_no_bill)?.visibility = View.GONE
                        findViewById<LinearLayout>(R.id.layout_bill_info)?.visibility = View.VISIBLE
                        updateBillUI(it) 
                    }
                } else {
                    findViewById<TextView>(R.id.tv_no_bill)?.visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.layout_bill_info)?.visibility = View.GONE
                }
            }
    }

    private fun updateBillUI(bill: Bill) {
        val btnPay  = findViewById<Button>(R.id.btn_view_pay_bill)
        val tvAmount = findViewById<TextView>(R.id.outstanding_amount)
        val tvBadge = findViewById<TextView>(R.id.tv_bill_status_badge)
        val tvLabel = findViewById<TextView>(R.id.tv_outstanding_label)

        tvAmount?.text = String.format("%,.2f บาท", bill.amount)

        // รายละเอียดการใช้งาน
        findViewById<TextView>(R.id.tv_room_price)?.text = String.format("%,.0f บาท", bill.details.roomPrice)
        findViewById<TextView>(R.id.tv_electric_price)?.text = "${bill.details.electricUnit} หน่วย = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
        findViewById<TextView>(R.id.tv_water_price)?.text = String.format("%,.0f บาท", bill.details.waterPrice)
        findViewById<TextView>(R.id.tv_other_price)?.text = String.format("%,.0f บาท", bill.details.otherPrice)
        findViewById<TextView>(R.id.tv_total_price)?.text = String.format("%,.2f บาท", bill.amount)

        tvBadge?.visibility = View.VISIBLE
        val greenColor = android.graphics.Color.parseColor("#1B9E44")
        val redColor = android.graphics.Color.parseColor("#E53935")
        val orangeColor = android.graphics.Color.parseColor("#FF9800")

        when {
            bill.isPaid -> {
                tvLabel?.visibility = View.GONE // เอา "ยอดค้างชำระ" ออก
                tvAmount?.setTextColor(greenColor) // เปลี่ยนตัวเลขเป็นสีเขียว
                
                tvBadge?.text = "ชำระแล้ว"
                setBadgeColor(tvBadge, greenColor)
                btnPay?.text = "ชำระแล้ว"
                btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(greenColor)
                btnPay?.isEnabled = false
            }
            bill.isPending -> {
                tvLabel?.visibility = View.VISIBLE
                tvLabel?.text = "รอการยืนยัน"
                tvAmount?.setTextColor(orangeColor)
                
                tvBadge?.text = "รอการยืนยัน"
                setBadgeColor(tvBadge, orangeColor)
                btnPay?.text = "รอการยืนยัน"
                btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(orangeColor)
                btnPay?.isEnabled = false
            }
            else -> {
                tvLabel?.visibility = View.VISIBLE
                tvLabel?.text = "ยอดค้างชำระ"
                tvAmount?.setTextColor(redColor)
                
                tvBadge?.text = "ค้างชำระ"
                setBadgeColor(tvBadge, redColor)
                btnPay?.text = "จ่ายเลย"
                btnPay?.backgroundTintList = android.content.res.ColorStateList.valueOf(redColor)
                btnPay?.isEnabled = true
            }
        }
    }

    private fun setBadgeColor(view: android.widget.TextView?, color: Int) {
        val density = resources.displayMetrics.density
        val bg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 20f * density
            setColor(color)
        }
        view?.background = bg
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

    private fun observeUnreadChat() {
        val uid = auth.currentUser?.uid ?: return
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        db.collection("chats").document(uid).addSnapshotListener { doc, _ ->
            if (doc?.getBoolean("hasUnreadForUser") == true) {
                bottomNav.getOrCreateBadge(R.id.navigation_chat).isVisible = true
            } else {
                bottomNav.removeBadge(R.id.navigation_chat)
            }
        }
    }
}
