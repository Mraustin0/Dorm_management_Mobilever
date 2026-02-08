package com.example.horganized.user

import android.os.Bundle
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
        adapter = NotificationAdapter(notificationList)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter
    }

    private fun fetchNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notificationList.clear()
                for (document in documents) {
                    val notification = document.toObject(Notification::class.java)
                    notificationList.add(notification)
                }

                if (notificationList.isEmpty()) {
                    addSampleNotifications()
                }

                adapter.notifyDataSetChanged()
                updateEmptyState()
                updateNotifCount()
            }
            .addOnFailureListener {
                addSampleNotifications()
                adapter.notifyDataSetChanged()
                updateEmptyState()
                updateNotifCount()
            }
    }

    private fun addSampleNotifications() {
        notificationList.add(
            Notification(
                senderName = "ADMIN1",
                message = "แอดมินได้ส่งร่างสัญญาฉบับที่ 2 ให้คุณแล้ว กรุณาตรวจสอบรายละเอียดและเซ็นสัญญาภายในวันที่ 01/06/2025",
                timestamp = System.currentTimeMillis() - 60000,
                isRead = false
            )
        )
        notificationList.add(
            Notification(
                senderName = "ADMIN1",
                message = "ใบแจ้งหนี้ใหม่ค่ะ ตรวจสอบยอดและชำระเงินได้ภายในวันที่ 05/05/2026",
                timestamp = System.currentTimeMillis() - 259200000,
                isRead = false
            )
        )
        notificationList.add(
            Notification(
                senderName = "ADMIN1",
                message = "ชำระเงินสำเร็จ แอดมินตรวจสอบยอดเงินเรียบร้อยแล้ว ขอบคุณค่ะ",
                timestamp = System.currentTimeMillis() - 604800000,
                isRead = true
            )
        )
        notificationList.add(
            Notification(
                senderName = "ADMIN1",
                message = "รับเรื่องแจ้งซ่อมเรียบร้อย คำขอซ่อมหลอดไฟ ของคุณอยู่ระหว่างดำเนินการ",
                timestamp = System.currentTimeMillis() - 604800000,
                isRead = true
            )
        )
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
