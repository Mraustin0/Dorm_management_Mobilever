package com.example.horganized.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = AdminNotificationAdapter(notificationList) { item ->
            // แก้ไข: ตรวจสอบ notificationId และอัปเดต Firebase
            if (!item.isRead && item.notificationId.isNotEmpty()) {
                db.collection("Admin_Notifications").document(item.notificationId)
                    .update("isRead", true)
                    .addOnSuccessListener {
                        Log.d("AdminNotif", "Successfully marked as read")
                    }
                    .addOnFailureListener { e ->
                        Log.e("AdminNotif", "Error marking as read", e)
                    }
            }
        }
        recyclerView.adapter = adapter
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
                        // สำคัญ: ต้องเก็บ ID ของ Document ไว้เพื่อใช้อัปเดตตอนกดอ่าน
                        notificationList.add(item.copy(notificationId = doc.id))
                    }
                    llEmptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                } else {
                    llEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }
    }
}
