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

        setupClickListeners()
        loadVacantRoomCount()
        
        // เริ่มต้นฟังการแจ้งเตือนแบบ Real-time
        checkUnreadGeneralNotifications()
        checkNewRepairRequests()
    }

    private fun setupClickListeners() {
        // ไอคอนการแจ้งเตือนรวม
        findViewById<ImageView>(R.id.iv_notification).setOnClickListener {
            startActivity(Intent(this, AdminNotificationActivity::class.java))
        }

        // เมนูแจ้งซ่อม
        findViewById<CardView>(R.id.cv_technician).setOnClickListener {
            markRepairRequestsAsRead()
            startActivity(Intent(this, AdminRepairListActivity::class.java))
        }

        // คืนค่าเดิม: ปุ่มย้ายเข้า/ออก ให้ไปหน้าเลือกห้องเพื่อจัดการรายห้อง
        findViewById<CardView>(R.id.cv_move).setOnClickListener {
            startActivity(Intent(this, AdminMoveSelectionActivity::class.java))
        }

        // เมนูอื่นๆ
        findViewById<CardView>(R.id.cv_room_vacant).setOnClickListener {
            startActivity(Intent(this, AdminVacantRoomActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_nav_apartment).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_nav_chat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_announce).setOnClickListener {
            startActivity(Intent(this, AdminAddAnnouncementActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_meter).setOnClickListener {
            startActivity(Intent(this, AdminMeterActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_check_slip).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java).apply { putExtra("MODE", "CHECK_SLIP") })
        }
        findViewById<CardView>(R.id.cv_create_bill).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java).apply { putExtra("MODE", "CREATE_BILL") })
        }
        findViewById<ImageView>(R.id.iv_menu).setOnClickListener {
            startActivity(Intent(this, AdminSettingActivity::class.java))
        }
    }

    private fun checkUnreadGeneralNotifications() {
        val viewNotifDot = findViewById<View>(R.id.view_notif_dot)
        db.collection("Admin_Notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, _ ->
                viewNotifDot?.visibility = if (snapshots != null && !snapshots.isEmpty) View.VISIBLE else View.GONE
            }
    }

    private fun checkNewRepairRequests() {
        val viewRepairDot = findViewById<View>(R.id.view_repair_card_dot)
        db.collection("repair_requests")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, _ ->
                viewRepairDot?.visibility = if (snapshots != null && !snapshots.isEmpty) View.VISIBLE else View.GONE
            }
    }

    private fun markRepairRequestsAsRead() {
        db.collection("repair_requests").whereEqualTo("isRead", false).get().addOnSuccessListener { docs ->
            for (doc in docs) {
                db.collection("repair_requests").document(doc.id).update("isRead", true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadVacantRoomCount()
    }

    private fun loadVacantRoomCount() {
        val tvVacantCount = findViewById<TextView>(R.id.tv_vacant_count)
        db.collection("rooms").get().addOnSuccessListener { documents ->
            if (tvVacantCount == null) return@addOnSuccessListener
            val total = documents.size()
            val vacant = documents.count { it.getBoolean("isVacant") ?: true }
            tvVacantCount.text = "$vacant/$total"
        }
    }
}
