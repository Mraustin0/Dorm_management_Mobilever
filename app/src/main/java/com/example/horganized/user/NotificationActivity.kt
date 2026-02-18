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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
                // อัปเดตสถานะเป็นอ่านแล้วใน Firestore (ถ้ามี ID)
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
        val userId = auth.currentUser?.uid ?: return

        // ดึงแจ้งเตือนที่เจาะจงถึงเรา (userId) และ ประกาศทั่วไป (userId == "all")
        db.collection("notifications")
            .whereIn("userId", listOf(userId, "all"))
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("NotificationActivity", "Listen failed.", e)
                    updateEmptyState()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    notificationList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(Notification::class.java)
                        notificationList.add(item)
                    }

                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                    updateNotifCount()
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
