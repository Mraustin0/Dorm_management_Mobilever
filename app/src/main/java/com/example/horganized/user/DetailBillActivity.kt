package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class DetailBillActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var billsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_bill)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        billsContainer = findViewById(R.id.bills_container)

        // Header icons
        findViewById<ImageView>(R.id.notification_icon).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        loadUserData()
        loadAllBills()
        setupBottomNavigation()
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

    private fun loadAllBills() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("bills")
            .whereEqualTo("userId", uid)
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots == null) return@addSnapshotListener

                billsContainer.removeAllViews()

                val billDocs = snapshots.documents
                val bills = billDocs.mapNotNull { it.toObject(Bill::class.java) }
                val billIds = billDocs.map { it.id }

                val layoutNoBill = findViewById<View>(R.id.layout_no_bill)
                if (bills.isEmpty()) {
                    layoutNoBill?.visibility = View.VISIBLE
                    billsContainer.visibility = View.GONE
                } else {
                    layoutNoBill?.visibility = View.GONE
                    billsContainer.visibility = View.VISIBLE
                    bills.forEachIndexed { index, bill ->
                        val isLatest = index == 0
                        addBillCard(bill, billIds[index], isLatest)
                    }
                }
            }
    }

    private fun addBillCard(bill: Bill, billId: String, isLatest: Boolean) {
        val cardView = LayoutInflater.from(this)
            .inflate(R.layout.item_bill_card, billsContainer, false)

        val btnToggle = cardView.findViewById<RelativeLayout>(R.id.btn_toggle)
        val ivChevron = cardView.findViewById<ImageView>(R.id.iv_chevron)
        val layoutContent = cardView.findViewById<LinearLayout>(R.id.layout_content)
        val tvMonthTitle = cardView.findViewById<TextView>(R.id.tv_bill_month_title)
        val tvSubtitle = cardView.findViewById<TextView>(R.id.tv_bill_subtitle)
        val tvAmount = cardView.findViewById<TextView>(R.id.tv_bill_amount)
        val tvDue = cardView.findViewById<TextView>(R.id.tv_bill_due)
        val btnPay = cardView.findViewById<Button>(R.id.btn_pay)

        // Usage details
        val tvRoomPrice = cardView.findViewById<TextView>(R.id.tv_room_price)
        val tvElectricPrice = cardView.findViewById<TextView>(R.id.tv_electric_price)
        val tvWaterPrice = cardView.findViewById<TextView>(R.id.tv_water_price)
        val tvOtherPrice = cardView.findViewById<TextView>(R.id.tv_other_price)
        val tvTotalPrice = cardView.findViewById<TextView>(R.id.tv_total_price)

        // Month title
        val monthName = bill.month.ifEmpty { "ไม่ระบุ" }
        tvMonthTitle.text = "บิลประจำเดือน $monthName"

        // Subtitle
        tvSubtitle.text = "ยอดค่าบริการ เดือน$monthName"

        // Amount
        tvAmount.text = String.format("%,.2f บาท", bill.amount)

        // Due date
        if (bill.dueDate != null) {
            val sdf = SimpleDateFormat("d MMM yyyy", Locale("th", "TH"))
            val dueDateStr = sdf.format(bill.dueDate.toDate())
            when {
                bill.isPaid -> {
                    tvDue.text = "ชำระแล้ว"
                    tvDue.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                }
                bill.isPending -> {
                    tvDue.text = "รอ admin ยืนยัน"
                    tvDue.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                }
                bill.status == "rejected" -> {
                    tvDue.text = "สลิปถูกปฏิเสธ กรุณาจ่ายใหม่"
                    tvDue.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                }
                else -> {
                    tvDue.text = "กำหนดชำระ $dueDateStr"
                }
            }
        }

        // Pay button
        when {
            bill.isPaid -> {
                btnPay.text = "จ่ายแล้ว"
                btnPay.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1B9E44")
                )
                btnPay.isEnabled = false
            }
            bill.isPending -> {
                // ส่งสลิปแล้ว รอ admin ยืนยัน → สีเหลืองเข้ม
                btnPay.text = "รอการยืนยัน"
                btnPay.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#F9A825")
                )
                btnPay.isEnabled = false
            }
            bill.status == "rejected" -> {
                // admin ปฏิเสธสลิป → สีแดง ให้จ่ายใหม่
                btnPay.text = "สลิปถูกปฏิเสธ จ่ายใหม่"
                btnPay.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E53935")
                )
                btnPay.setOnClickListener {
                    val payIntent = Intent(this, PayBillActivity::class.java)
                    payIntent.putExtra("BILL_ID", billId)
                    payIntent.putExtra("BILL_AMOUNT", bill.amount)
                    payIntent.putExtra("BILL_MONTH", bill.month)
                    startActivity(payIntent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
            else -> {
                // ยังไม่จ่าย → สีแดง
                btnPay.text = "จ่ายเลย"
                btnPay.setOnClickListener {
                    val payIntent = Intent(this, PayBillActivity::class.java)
                    payIntent.putExtra("BILL_ID", billId)
                    payIntent.putExtra("BILL_AMOUNT", bill.amount)
                    payIntent.putExtra("BILL_MONTH", bill.month)
                    startActivity(payIntent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }

        // Usage details
        tvRoomPrice.text = String.format("%,.0f บาท", bill.details.roomPrice)
        tvElectricPrice.text = "${bill.details.electricUnit} = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
        tvWaterPrice.text = String.format("%,.0f บาท", bill.details.waterPrice)
        tvOtherPrice.text = String.format("%,.0f บาท", bill.details.otherPrice)
        tvTotalPrice.text = String.format("%,.0f บาท", bill.amount)

        // Latest bill: expanded, others: collapsed
        if (isLatest) {
            layoutContent.visibility = View.VISIBLE
            ivChevron.setImageResource(R.drawable.ic_chevron_up_gg)
        } else {
            layoutContent.visibility = View.GONE
            ivChevron.setImageResource(R.drawable.ic_chevron_down_gg)
        }

        // Toggle
        btnToggle.setOnClickListener {
            if (layoutContent.visibility == View.VISIBLE) {
                layoutContent.visibility = View.GONE
                ivChevron.setImageResource(R.drawable.ic_chevron_down_gg)
            } else {
                layoutContent.visibility = View.VISIBLE
                ivChevron.setImageResource(R.drawable.ic_chevron_up_gg)
            }
        }

        billsContainer.addView(cardView)
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
