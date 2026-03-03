package com.example.horganized.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DormInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var contractUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dorm_info)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
        findViewById<LinearLayout>(R.id.btn_view_contract)?.setOnClickListener {
            if (!contractUrl.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contractUrl))
                startActivity(intent)
            } else {
                // ถ้าไม่มี URL เฉพาะรายบุคคล ให้ไปหน้าแสดงรายการทั่วไป
                val intent = Intent(this, ContractListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        loadDormInfo()
        setupBottomNavigation()
    }

    private fun loadDormInfo() {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val roomNumber = document.getString("roomNumber") ?: "-"
                    val moveInTimestamp = document.getTimestamp("moveInDate")
                    val termString = document.getString("contractTerm") ?: ""
                    contractUrl = document.getString("contractUrl")

                    findViewById<TextView>(R.id.tv_room_number)?.text = "ห้อง $roomNumber"

                    if (moveInTimestamp != null) {
                        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                        val startDate = moveInTimestamp.toDate()
                        findViewById<TextView>(R.id.tv_contract_start)?.text = sdf.format(startDate)

                        // คำนวณวันสิ้นสุดจาก contractTerm (เช่น "6 เดือน")
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
                            findViewById<TextView>(R.id.tv_contract_end)?.text = sdf.format(calendar.time)
                        } else {
                            findViewById<TextView>(R.id.tv_contract_end)?.text = "-"
                        }
                    } else {
                        findViewById<TextView>(R.id.tv_contract_start)?.text = "-"
                        findViewById<TextView>(R.id.tv_contract_end)?.text = "-"
                    }

                    // (btn_view_contract เป็น LinearLayout แล้ว ไม่ต้องเปลี่ยนข้อความ)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "โหลดข้อมูลไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
