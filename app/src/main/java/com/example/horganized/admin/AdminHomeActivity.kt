package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadVacantRoomCount()

        // เชื่อมไอคอนกลางล่าง (nav_list) ไปยังหน้าเลือกห้องพัก
        val navSelectRoom = findViewById<ImageView>(R.id.iv_nav_apartment)
        navSelectRoom.setOnClickListener {
            val intent = Intent(this, AdminSelectRoomActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมไอคอนแชท (ขวาล่าง) ไปยังหน้า Chat List
        val navChat = findViewById<ImageView>(R.id.iv_nav_chat)
        navChat.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมปุ่มประกาศ
        val cvAnnounce = findViewById<CardView>(R.id.cv_announce)
        cvAnnounce.setOnClickListener {
            val intent = Intent(this, AdminAnnounceActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมปุ่มจดมิเตอร์
        val cvMeter = findViewById<CardView>(R.id.cv_meter)
        cvMeter.setOnClickListener {
            val intent = Intent(this, AdminMeterActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมปุ่มตรวจสอบสลิป
        val cvCheckSlip = findViewById<CardView>(R.id.cv_check_slip)
        cvCheckSlip.setOnClickListener {
            val intent = Intent(this, AdminCheckSlipActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมปุ่มติดต่อช่าง
        val cvTechnician = findViewById<CardView>(R.id.cv_technician)
        cvTechnician.setOnClickListener {
            val intent = Intent(this, AdminTechnicianActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมปุ่มย้ายเข้า/ออก (cv_move) ไปหน้าเลือกห้องเพื่ออัปเดตข้อมูล
        val cvMove = findViewById<CardView>(R.id.cv_move)
        cvMove.setOnClickListener {
            val intent = Intent(this, AdminMoveSelectionActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมไอคอนตั้งค่า (ขวาบน) ไปยังหน้า AdminSettingActivity
        val ivSetting = findViewById<ImageView>(R.id.iv_menu)
        ivSetting.setOnClickListener {
            val intent = Intent(this, AdminSettingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVacantRoomCount()
    }

    private fun loadVacantRoomCount() {
        val tvVacantCount = findViewById<TextView>(R.id.tv_vacant_count)

        db.collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                val totalRooms = documents.size()
                val vacantRooms = documents.count { doc ->
                    doc.getBoolean("isVacant") ?: true
                }
                tvVacantCount.text = "$vacantRooms/$totalRooms"
            }
            .addOnFailureListener {
                tvVacantCount.text = "--/--"
            }
    }
}