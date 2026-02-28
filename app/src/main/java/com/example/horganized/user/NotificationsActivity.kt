package com.example.horganized.user

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Notification

class NotificationsActivity : AppCompatActivity() {
    
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rv_notifications)
        rv.layoutManager = LinearLayoutManager(this)

        // เตรียมข้อมูลจำลอง (ใช้ชื่อฟิลด์ให้ตรงกับ Model)



        adapter = NotificationAdapter(notificationList) { position ->
            // Logic เมื่อกดอ่าน: เปลี่ยนสถานะเป็นอ่านแล้ว
            val item = notificationList[position]
            if (!item.isRead) {
                notificationList[position] = item.copy(isRead = true)
                adapter.notifyItemChanged(position)
            }
        }
        
        rv.adapter = adapter
    }
}