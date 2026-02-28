package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.AdminNotificationAdapter
import com.example.horganized.model.AdminNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminNotificationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvNotifCount: TextView
    private val notificationList = mutableListOf<AdminNotification>()
    private lateinit var adapter: AdminNotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notification)

        initViews()
        fetchNotifications()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_admin_notifications)
        llEmptyState = findViewById(R.id.ll_empty_state)
        tvNotifCount = findViewById(R.id.tv_notif_count)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminNotificationAdapter(notificationList) { item ->
            // Mark as read in Firestore
            if (!item.isRead && item.notificationId.isNotEmpty()) {
                db.collection("Admin_Notifications").document(item.notificationId)
                    .update("isRead", true)
                    .addOnSuccessListener {
                        Log.d("AdminNotif", "Marked as read")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AdminNotif", "Error marking as read", e)
                    }
            }
            // Navigate by type
            navigateByType(item)
        }
        recyclerView.adapter = adapter
    }

    private fun navigateByType(item: AdminNotification) {
        when (item.type) {
            "chat" -> {
                // ไปหน้า ChatDetailActivity พร้อมส่ง userId ของ user เป็น CHAT_ROOM_ID
                val intent = Intent(this, ChatDetailActivity::class.java).apply {
                    putExtra("CHAT_ROOM_ID", item.userId)
                    putExtra("ROOM_NAME", "ห้อง ${item.roomNumber}")
                    putExtra("USER_NAME", item.roomNumber)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            "repair" -> {
                // ไปหน้ารายการแจ้งซ่อม
                val intent = Intent(this, AdminRepairListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            "payment" -> {
                // ไปหน้าตรวจสลิป
                val intent = Intent(this, AdminCheckSlipActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else -> {
                // ประเภทอื่น: ไม่ navigate
            }
        }
    }

    private fun fetchNotifications() {
        db.collection("Admin_Notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminNotif", "Listen failed.", e)
                    return@addSnapshotListener
                }

                notificationList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (doc in snapshots) {
                        val item = doc.toObject(AdminNotification::class.java)
                        notificationList.add(item.copy(notificationId = doc.id))
                    }
                    llEmptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    llEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
                updateNotifCount()
            }
    }

    private fun updateNotifCount() {
        val unreadCount = notificationList.count { !it.isRead }
        if (unreadCount > 0) {
            tvNotifCount.visibility = View.VISIBLE
            tvNotifCount.text = "$unreadCount ใหม่"
        } else {
            tvNotifCount.visibility = View.GONE
        }
    }
}
