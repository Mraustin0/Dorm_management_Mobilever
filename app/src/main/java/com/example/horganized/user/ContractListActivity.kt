package com.example.horganized.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ContractListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var contractUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_list)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // เมื่อคลิกที่ Card สัญญา ให้เปิดลิงก์
        findViewById<CardView>(R.id.card_contract_item).setOnClickListener {
            if (!contractUrl.isNullOrEmpty()) {
                openContractLink(contractUrl!!)
            } else {
                Toast.makeText(this, "ยังไม่มีไฟล์สัญญาแนบไว้ในระบบ", Toast.LENGTH_SHORT).show()
            }
        }

        loadContractData()
    }

    private fun loadContractData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val moveInTimestamp = document.getTimestamp("moveInDate")
                    val termString = document.getString("contractTerm") ?: ""
                    contractUrl = document.getString("contractUrl")

                    val tvStart = findViewById<TextView>(R.id.tv_contract_start_list)
                    val tvEnd = findViewById<TextView>(R.id.tv_contract_end_list)
                    val tvDays = findViewById<TextView>(R.id.tv_days_remaining)
                    val tvStatus = findViewById<TextView>(R.id.tv_contract_status)

                    if (moveInTimestamp != null) {
                        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                        val startDate = moveInTimestamp.toDate()
                        tvStart.text = sdf.format(startDate)

                        // คำนวณวันสิ้นสุด
                        val calendar = Calendar.getInstance()
                        calendar.time = startDate
                        val monthsToAdd = when {
                            termString.contains("3") -> 3
                            termString.contains("6") -> 6
                            termString.contains("12") -> 12
                            else -> 0
                        }
                        
                        if (monthsToAdd > 0) {
                            calendar.add(Calendar.MONTH, monthsToAdd)
                            val endDate = calendar.time
                            tvEnd.text = sdf.format(endDate)

                            // คำนวณจำนวนวันที่เหลือ (จากวันนี้ ถึง วันสิ้นสุด)
                            val today = Calendar.getInstance().time
                            val diffInMillies = endDate.time - today.time
                            val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)

                            if (diffInDays > 0) {
                                tvDays.text = "(อีก $diffInDays วัน)"
                                tvStatus.text = "กำลังดำเนินการ"
                                tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4A90E2.toInt()) // Blue
                            } else {
                                tvDays.text = "(สิ้นสุดแล้ว)"
                                tvStatus.text = "เสร็จสิ้น"
                                tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt()) // Green
                            }
                        }
                    }
                }
            }
    }

    private fun openContractLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "ไม่สามารถเปิดไฟล์ได้ กรุณาตรวจสอบลิงก์", Toast.LENGTH_SHORT).show()
        }
    }
}
