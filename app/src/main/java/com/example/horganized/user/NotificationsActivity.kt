package com.example.horganized.user

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
import com.example.horganized.adapter.NotificationAdapter
import com.example.horganized.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: NotificationAdapter
    private lateinit var tvNotifCount: TextView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private val notificationList = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView   = findViewById(R.id.rv_notifications)
        llEmptyState   = findViewById(R.id.ll_empty_state)
        tvNotifCount   = findViewById(R.id.tv_notif_count)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // แก้ไข onItemClick: เพิ่ม Logic การเปิดหน้าบิล
        adapter = NotificationAdapter(notificationList) { item ->
            // 1. ทำเครื่องหมายว่าอ่านแล้ว
            if (!item.isRead && item.notificationId.isNotEmpty()) {
                db.collection("notifications").document(item.notificationId)
                    .update("isRead", true)
            }

            // 2. ไปยังหน้าบิลถ้าเป็นแจ้งเตือนบิลใหม่
            if (item.type == "new_bill") {
                val intent = Intent(this, DetailBillActivity::class.java)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING) // เรียงลำดับตามเวลาล่าสุด
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("UserNotif", "Listen failed.", e)
                    return@addSnapshotListener
                }
                
                notificationList.clear()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        try {
                            val item = doc.toObject(Notification::class.java)
                            notificationList.add(item.copy(notificationId = doc.id))
                        } catch (ex: Exception) {
                            Log.e("UserNotif", "Skip doc ${doc.id}: ${ex.message}")
                        }
                    }
                }

                if (notificationList.isEmpty()) {
                    recyclerView.visibility  = View.GONE
                    llEmptyState.visibility  = View.VISIBLE
                } else {
                    recyclerView.visibility  = View.VISIBLE
                    llEmptyState.visibility  = View.GONE
                }
                adapter.notifyDataSetChanged()
                updateNotifCount()
            }
    }

    private fun updateNotifCount() {
        val unread = notificationList.count { !it.isRead }
        if (unread > 0) {
            tvNotifCount.visibility = View.VISIBLE
            tvNotifCount.text = "$unread ใหม่"
        } else {
            tvNotifCount.visibility = View.GONE
        }
    }
}
