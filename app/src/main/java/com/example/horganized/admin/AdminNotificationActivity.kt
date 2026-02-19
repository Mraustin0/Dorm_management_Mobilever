package com.example.horganized.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.AdminNotificationAdapter
import com.example.horganized.model.AdminNotificationModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminNotificationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvNotifications: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private val notificationList = mutableListOf<AdminNotificationModel>()
    private lateinit var adapter: AdminNotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notification)

        initViews()
        fetchAdminNotifications()
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        rvNotifications = findViewById(R.id.rv_admin_notifications)
        llEmptyState = findViewById(R.id.ll_empty_state)
        
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = AdminNotificationAdapter(notificationList) { item ->
            // จัดการเมื่อกดที่แจ้งเตือน (เช่น เปลี่ยนสถานะเป็นอ่านแล้ว)
            if (!item.isRead) {
                db.collection("notifications").document(item.notificationId)
                    .update("isRead", true)
            }
            // หากต้องการให้ไปหน้าจัดการซ่อม หรือหน้าอื่นๆ สามารถเพิ่ม Intent ตรงนี้ได้
            Toast.makeText(this, "กดดู: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        rvNotifications.adapter = adapter
    }

    private fun fetchAdminNotifications() {
        // ดึงแจ้งเตือนที่ส่งมาหา Admin (userId == "admin")
        db.collection("notifications")
            .whereEqualTo("userId", "admin")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminNotification", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val items = snapshots.documents.mapNotNull { doc ->
                        // แปลงข้อมูลจาก Firestore เป็น Model
                        val model = doc.toObject(AdminNotificationModel::class.java)
                        model?.copy(notificationId = doc.id)
                    }

                    notificationList.clear()
                    // เรียงตามเวลาล่าสุด
                    notificationList.addAll(items.sortedByDescending { it.timestamp })
                    
                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                }
            }
    }

    private fun updateEmptyState() {
        if (notificationList.isEmpty()) {
            rvNotifications.visibility = View.GONE
            llEmptyState.visibility = View.VISIBLE
        } else {
            rvNotifications.visibility = View.VISIBLE
            llEmptyState.visibility = View.GONE
        }
    }
}