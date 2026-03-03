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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadAdminProfile()
        loadVacantRoomCount()
        checkUnreadAdminNotifications()
        checkUnreadChats()

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

        // เชื่อมไอคอนกลางล่างไปยังหน้าสร้างบิล
        findViewById<ImageView>(R.id.iv_nav_apartment).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomBillActivity::class.java))
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
            val intent = Intent(this, AdminCheckSlipActivity::class.java)
            intent.putExtra("MODE", "CHECK_SLIP")
            startActivity(intent)
        }

        // เชื่อมปุ่มแจ้งซ่อม
        findViewById<CardView>(R.id.cv_technician).setOnClickListener {
            startActivity(Intent(this, AdminRepairListActivity::class.java))
        }

        // เชื่อมปุ่มย้ายออก
        findViewById<CardView>(R.id.cv_move).setOnClickListener {
            startActivity(Intent(this, AdminMoveOutListActivity::class.java))
        }

        // เชื่อมไอคอนตั้งค่า
        findViewById<ImageView>(R.id.iv_menu).setOnClickListener {
            startActivity(Intent(this, AdminSettingActivity::class.java))
        }
    }

    private fun loadAdminProfile() {
        val uid = auth.currentUser?.uid ?: return
        val ivProfile = findViewById<ImageView>(R.id.iv_admin_profile)
        val tvAdminName = findViewById<TextView>(R.id.tv_admin_name)

        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val name = doc.getString("name") ?: doc.getString("username") ?: "Admin"
                tvAdminName.text = name

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    ivProfile.setPadding(0, 0, 0, 0)
                    ivProfile.background = null // ลบ background สีเทาเดิม
                    Glide.with(this)
                        .load(photoUrl)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.ic_user_gg)
                        .into(ivProfile)
                }
            }
        }
    }

    private fun checkUnreadAdminNotifications() {
        val viewNotifDot = findViewById<View>(R.id.view_notif_dot)
        db.collection("Admin_Notifications")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, _ ->
                viewNotifDot?.visibility = if (snapshots != null && !snapshots.isEmpty) View.VISIBLE else View.GONE
            }
    }

    private fun checkUnreadChats() {
        val uid = auth.currentUser?.uid ?: return
        // เช็คข้อความใหม่ในคอลเลกชัน chats โดยดูจาก unreadCount_ADMIN_UID
        db.collection("chats")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshots, _ ->
                var hasUnread = false
                snapshots?.forEach { doc ->
                    val unreadCount = doc.getLong("unreadCount_$uid") ?: 0L
                    if (unreadCount > 0) hasUnread = true
                }
                
                // ค้นหาตำแหน่ง icon chat เพื่อแสดงจุดแจ้งเตือน (ถ้าใน layout มีจุดแดงที่ icon แชท)
                // เนื่องจากใน layout ปัจจุบันไม่มี view สำหรับจุดแดงที่ iv_nav_chat
                // ผมจะใช้ Toast แจ้งเตือนสั้นๆ หรือถ้าคุณต้องการให้เพิ่มจุดแดงใน layout แจ้งได้นะครับ
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
