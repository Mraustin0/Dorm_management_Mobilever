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
        loadAdminProfile()
        loadVacantRoomCount()
        checkUnreadGeneralNotifications()
        checkNewRepairRequests()
    }

    private fun setupClickListeners() {
        // Highlight home icon (active)
        setupBottomNavigation()

        // ไอคอนการแจ้งเตือนรวม
        findViewById<ImageView>(R.id.iv_notification).setOnClickListener {
            startActivity(Intent(this, AdminNotificationActivity::class.java))
        }

        // แจ้งซ่อม
        findViewById<CardView>(R.id.cv_technician).setOnClickListener {
            markRepairRequestsAsRead()
            startActivity(Intent(this, AdminRepairListActivity::class.java))
        }

        // เมนูอื่นๆ
        findViewById<CardView>(R.id.cv_room_vacant).setOnClickListener {
            startActivity(Intent(this, AdminVacantRoomActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_announce).setOnClickListener {
            startActivity(Intent(this, AdminAddAnnouncementActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_meter).setOnClickListener {
            startActivity(Intent(this, AdminMeterActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_check_slip).setOnClickListener {
            startActivity(Intent(this, AdminCheckSlipActivity::class.java))
        }
        findViewById<CardView>(R.id.cv_create_bill).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java).apply { putExtra("MODE", "CREATE_BILL") })
        }
        findViewById<CardView>(R.id.cv_move).setOnClickListener {
            startActivity(Intent(this, AdminMoveSelectionActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_menu).setOnClickListener {
            startActivity(Intent(this, AdminSettingActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        // Highlight home icon (active), gray others
        findViewById<ImageView>(R.id.iv_sav_home)
            .setColorFilter(getColor(R.color.blue_primary))
        findViewById<ImageView>(R.id.iv_nav_apartment)
            .setColorFilter(getColor(R.color.gray_text))
        findViewById<ImageView>(R.id.iv_nav_chat)
            .setColorFilter(getColor(R.color.gray_text))

        // iv_sav_home อยู่หน้านี้แล้ว ไม่ต้องทำอะไร
        findViewById<ImageView>(R.id.iv_nav_apartment).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        findViewById<ImageView>(R.id.iv_nav_chat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
    }

    private fun loadAdminProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val tvName = findViewById<TextView>(R.id.tv_admin_name)
        val ivProfile = findViewById<ImageView>(R.id.iv_admin_profile)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                // username
                val name = doc.getString("username")
                    ?: doc.getString("firstName")
                    ?: doc.getString("name")
                    ?: "Admin"
                tvName?.text = name

                // รูปโปรไฟล์
                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty() && ivProfile != null) {
                    ivProfile.setPadding(0, 0, 0, 0)
                    Glide.with(this)
                        .load(photoUrl)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfile)
                }
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
        db.collection("repair_requests")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    db.collection("repair_requests").document(doc.id).update("isRead", true)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        loadAdminProfile()
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
