package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // ดึงบิลทั้งหมดของ User โดยไม่ใช้ orderBy ใน Query (เพื่อป้องกันปัญหาเอกสารหายถ้าไม่มีฟิลด์นั้น)
        db.collection("bills")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("DetailBill", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                billsContainer.removeAllViews()

                val billDocs = snapshots.documents
                val allBillsWithIds = billDocs.mapNotNull { doc ->
                    val bill = doc.toObject(Bill::class.java)
                    if (bill != null) Pair(bill, doc.id) else null
                }

                val layoutNoBill = findViewById<View>(R.id.layout_no_bill)
                
                if (allBillsWithIds.isEmpty()) {
                    layoutNoBill?.visibility = View.VISIBLE
                    billsContainer.visibility = View.GONE
                } else {
                    layoutNoBill?.visibility = View.GONE
                    billsContainer.visibility = View.VISIBLE

                    // เรียงลำดับบิลในเครื่อง (งวดล่าสุดขึ้นก่อน)
                    val sortedBills = allBillsWithIds.sortedByDescending { it.first.dueDate?.seconds ?: 0L }

                    // กรองให้แสดงเพียง 1 บิล ต่อเดือน และ ต่อปี (เอางวดที่อัปเดตล่าสุด)
                    val filteredBills = mutableListOf<Pair<Bill, String>>()
                    val seenMonthYear = mutableSetOf<String>()

                    for (pair in sortedBills) {
                        val bill = pair.first
                        // ตัดช่องว่างทิ้งเพื่อความแม่นยำในการเปรียบเทียบ
                        val month = bill.month.trim()
                        val year = bill.year.trim()
                        val key = "${month}_${year}"
                        
                        if (month.isNotEmpty() && !seenMonthYear.contains(key)) {
                            filteredBills.add(pair)
                            seenMonthYear.add(key)
                        }
                    }

                    filteredBills.forEachIndexed { index, pair ->
                        val isLatest = index == 0
                        addBillCard(pair.first, pair.second, isLatest)
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

        // ข้อมูลการใช้งาน
        val tvRoomPrice = cardView.findViewById<TextView>(R.id.tv_room_price)
        val tvElectricPrice = cardView.findViewById<TextView>(R.id.tv_electric_price)
        val tvWaterPrice = cardView.findViewById<TextView>(R.id.tv_water_price)
        val tvOtherPrice = cardView.findViewById<TextView>(R.id.tv_other_price)
        val tvTotalPrice = cardView.findViewById<TextView>(R.id.tv_total_price)

        val monthName = bill.month.trim()
        val yearName = bill.year.trim()
        
        tvMonthTitle.text = "บิลประจำเดือน $monthName $yearName"
        tvSubtitle.text = "ยอดค่าบริการ เดือน$monthName"
        tvAmount.text = String.format("%,.2f บาท", bill.amount)

        if (bill.dueDate != null) {
            val sdf = SimpleDateFormat("d MMM yyyy", Locale("th", "TH"))
            val dueDateStr = sdf.format(bill.dueDate.toDate())
            if (bill.isPaid) {
                tvDue.text = "ชำระแล้ว"
                tvDue.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            } else {
                tvDue.text = "เกินกำหนดชำระ $dueDateStr"
            }
        } else {
            tvDue.text = "รอดำเนินการ..."
        }

        if (bill.isPaid) {
            btnPay.text = "จ่ายแล้ว"
            btnPay.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1B9E44"))
            btnPay.isEnabled = false
        } else {
            btnPay.text = "จ่ายเลย"
            btnPay.setOnClickListener {
                val payIntent = Intent(this, PayBillActivity::class.java)
                payIntent.putExtra("BILL_ID", billId)
                payIntent.putExtra("BILL_AMOUNT", bill.amount)
                payIntent.putExtra("BILL_MONTH", bill.month)
                startActivity(payIntent)
            }
        }

        tvRoomPrice.text = String.format("%,.0f บาท", bill.details.roomPrice)
        tvElectricPrice.text = "${bill.details.electricUnit} หน่วย = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
        tvWaterPrice.text = "${bill.details.waterUnit} หน่วย = ${String.format("%,.0f", bill.details.waterPrice)} บาท"
        tvOtherPrice.text = String.format("%,.0f บาท", bill.details.otherPrice)
        tvTotalPrice.text = String.format("%,.2f บาท", bill.amount)

        if (isLatest) {
            layoutContent.visibility = View.VISIBLE
            ivChevron.setImageResource(R.drawable.ic_chevron_up_gg)
        } else {
            layoutContent.visibility = View.GONE
            ivChevron.setImageResource(R.drawable.ic_chevron_down_gg)
        }

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
                    finish()
                    true
                }
                R.id.navigation_bill -> true
                R.id.navigation_notifications -> {
                    startActivity(Intent(this, DormInfoActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}