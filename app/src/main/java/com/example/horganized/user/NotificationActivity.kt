package com.example.horganized.user

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
import com.example.horganized.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvNotifications: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvNotifCount: TextView
    private val notificationList = mutableListOf<Notification>()
    private lateinit var adapter: NotificationAdapter
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        rvNotifications = findViewById(R.id.rv_notifications)
        llEmptyState = findViewById(R.id.ll_empty_state)
        tvNotifCount = findViewById(R.id.tv_notif_count)

        btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        fetchNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList) { position ->
            val item = notificationList[position]
            if (!item.isRead) {
                if (item.notificationId.isNotEmpty()) {
                    db.collection("notifications").document(item.notificationId)
                        .update("isRead", true)
                }
                notificationList[position] = item.copy(isRead = true)
                adapter.notifyItemChanged(position)
                updateNotifCount()
            }
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun fetchNotifications() {
        // สร้าง Query พื้นฐานโดยไม่ใส่ OrderBy เพื่อเลี่ยงปัญหาเรื่อง Index ในช่วงแรก
        val baseQuery = if (isAdmin) {
            db.collection("notifications").whereEqualTo("userId", "admin")
        } else {
            val currentUserId = auth.currentUser?.uid ?: ""
            if (currentUserId.isEmpty()) {
                updateEmptyState()
                return
            }
            db.collection("notifications").whereIn("userId", listOf(currentUserId, "all"))
        }

        baseQuery.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("NotificationActivity", "Error: ${e.message}")
                updateEmptyState()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val items = snapshots.documents.mapNotNull { it.toObject(Notification::class.java) }
                
                // เรียงลำดับตาม Timestamp ในโค้ด (จากใหม่ไปเก่า)
                val sortedItems = items.sortedByDescending { it.timestamp }
                
                notificationList.clear()
                notificationList.addAll(sortedItems)

                adapter.notifyDataSetChanged()
                updateEmptyState()
                updateNotifCount()
                Log.d("NotificationActivity", "Data loaded: ${notificationList.size} items")
            }
        }
    }

    private fun updateEmptyState() {
        if (notificationList.isEmpty()) {
            rvNotifications.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE
            tvNotifCount.visibility = View.GONE
        } else {
            rvNotifications.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }

    private fun updateNotifCount() {
        val unreadCount = notificationList.count { !it.isRead }
        if (unreadCount > 0) {
            tvNotifCount.visibility = View.VISIBLE
            tvNotifCount.text = "$unreadCount new"
        } else {
            tvNotifCount.visibility = View.GONE
        }
    }
}
