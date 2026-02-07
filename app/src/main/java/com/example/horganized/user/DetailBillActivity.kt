package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.example.horganized.model.Bill
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DetailBillActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_bill)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupBottomNavigation()
        fetchLatestBill()
    }

    private fun fetchLatestBill() {
        val userId = auth.currentUser?.uid ?: return

        // ดึงข้อมูลบิลล่าสุดโดยเรียงจากเดือน/ปี หรือ Timestamp (ถ้ามี)
        db.collection("bills")
            .whereEqualTo("userId", userId)
            // .orderBy("timestamp", Query.Direction.DESCENDING) // เปิดใช้ถ้าคุณมีฟิลด์ timestamp
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val bill = document.toObject(Bill::class.java)
                    updateUI(bill)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching bill: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(bill: Bill) {
        findViewById<TextView>(R.id.tv_bill_month_title).text = "บิลประจำเดือน ${bill.monthYear}"
        findViewById<TextView>(R.id.tv_service_month_label).text = "ยอดค่าบริการ เดือน${bill.monthYear}"
        findViewById<TextView>(R.id.tv_total_amount_red).text = "${bill.totalAmount} บาท"
        findViewById<TextView>(R.id.tv_due_date).text = "เกินกำหนดชำระ: ${bill.dueDate}"
        findViewById<TextView>(R.id.tv_room_rent).text = "${bill.roomRent} บาท"
        findViewById<TextView>(R.id.tv_water_bill).text = "${bill.waterBill} บาท"
        findViewById<TextView>(R.id.tv_additional_fee).text = "${bill.additionalFee} บาท"
        findViewById<TextView>(R.id.tv_total_amount_summary).text = "${bill.totalAmount} บาท"
        
        // ตัวอย่างการแสดงรายละเอียดค่าไฟ
        findViewById<TextView>(R.id.tv_electricity_detail).text = "${bill.electricityUnits} หน่วย = ${bill.electricityBill} บาท"
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