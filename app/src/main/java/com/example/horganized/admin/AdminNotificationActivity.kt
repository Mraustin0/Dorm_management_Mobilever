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
        
        // แก้ไข: เพิ่ม lambda สำหรับ onItemClick เพื่อแก้บัค No value passed for parameter
        adapter = AdminNotificationAdapter(notificationList) { item ->
            // เมื่อกดที่แจ้งเตือน (สามารถเพิ่ม logic การทำงานต่อได้ที่นี่)
            Log.d("AdminNotif", "Clicked on: ${item.message}")
        }
        recyclerView.adapter = adapter
    }

    private fun fetchNotifications() {
        // ดึงข้อมูลจากคอลเลกชัน Admin_Notifications ตามที่ระบุ
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
                        notificationList.add(item)
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
