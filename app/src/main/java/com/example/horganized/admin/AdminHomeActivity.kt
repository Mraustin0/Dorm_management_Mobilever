package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        checkUnreadAdminNotifications()

        // ไอคอนการแจ้งเตือน (Notifications) เปิดหน้า AdminNotificationActivity
        val ivNotification = findViewById<ImageView>(R.id.iv_notification)
        ivNotification.setOnClickListener {
            val intent = Intent(this, AdminNotificationActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมการ์ดห้องว่าง
        findViewById<CardView>(R.id.cv_room_vacant).setOnClickListener {
            startActivity(Intent(this, AdminVacantRoomActivity::class.java))
        }

        // เชื่อมไอคอนกลางล่างไปยังหน้าเลือกห้องพัก
        findViewById<ImageView>(R.id.iv_nav_apartment).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java))
        }

        // เชื่อมไอคอนแชท
        findViewById<ImageView>(R.id.iv_nav_chat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        // เชื่อมปุ่มประกาศ
        findViewById<CardView>(R.id.cv_announce).setOnClickListener {
            startActivity(Intent(this, AdminAddAnnouncementActivity::class.java))
        }

        // เชื่อมปุ่มจดมิเตอร์
        findViewById<CardView>(R.id.cv_meter).setOnClickListener {
            startActivity(Intent(this, AdminMeterActivity::class.java))
        }

        // เชื่อมปุ่มตรวจสอบสลิป
        findViewById<CardView>(R.id.cv_check_slip).setOnClickListener {
            val intent = Intent(this, AdminSelectRoomActivity::class.java)
            intent.putExtra("MODE", "CHECK_SLIP")
            startActivity(intent)
        }

        // เชื่อมปุ่มสร้างบิล
        findViewById<CardView>(R.id.cv_create_bill).setOnClickListener {
            val intent = Intent(this, AdminSelectRoomActivity::class.java)
            intent.putExtra("MODE", "CREATE_BILL")
            startActivity(intent)
        }

        // เชื่อมปุ่มแจ้งซ่อม
        findViewById<CardView>(R.id.cv_technician).setOnClickListener {
            startActivity(Intent(this, AdminRepairListActivity::class.java))
        }

        // เชื่อมปุ่มย้ายเข้า/ออก
        findViewById<CardView>(R.id.cv_move).setOnClickListener {
            startActivity(Intent(this, AdminMoveSelectionActivity::class.java))
        }

        // เชื่อมไอคอนตั้งค่า
        findViewById<ImageView>(R.id.iv_menu).setOnClickListener {
            startActivity(Intent(this, AdminSettingActivity::class.java))
        }
    }

    private fun checkUnreadAdminNotifications() {
        val viewNotifDot = findViewById<View>(R.id.view_notif_dot)
        // ฟังรายการแจ้งเตือนจากลูกหอที่ส่งมาหาแอดมิน (คอลเลกชัน Admin_Notifications)
        db.collection("Admin_Notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null && !snapshots.isEmpty) {
                    viewNotifDot?.visibility = View.VISIBLE
                } else {
                    viewNotifDot?.visibility = View.GONE
                }
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
                if (tvVacantCount == null) return@addOnSuccessListener
                val totalRooms = documents.size()
                val vacantRooms = documents.count { doc ->
                    doc.getBoolean("isVacant") ?: true
                }
                tvVacantCount.text = "$vacantRooms/$totalRooms"
            }
            .addOnFailureListener {
                tvVacantCount?.text = "--/--"
            }
    }
}
