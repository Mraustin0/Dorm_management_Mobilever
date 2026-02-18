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
        notificationList.addAll(listOf(
            Notification(senderName = "ADMIN1", message = "แอดมินได้ส่งร่างสัญญาฉบับที่ 2 ให้คุณแล้ว กรุณาตรวจสอบรายละเอียดและเซ็นสัญญาภายในวันที่ 01/06/2025", time = "1m ago.", isRead = false),
            Notification(senderName = "ADMIN1", message = "ใบแจ้งหนี้ใหม่ค่ะ ตรวจสอบยอดและชำระเงินได้ภายในวันที่ 05/05/2026", time = "3d ago.", isRead = false),
            Notification(senderName = "ADMIN1", message = "ชำระเงินสำเร็จ แอดมินตรวจสอบยอดเงินเรียบร้อยแล้ว ขอบคุณค่ะ", time = "1w ago.", isRead = true),
            Notification(senderName = "ADMIN1", message = "รับเรื่องแจ้งซ่อมเรียบร้อย คำขอซ่อมหลอดไฟ ของคุณอยู่ระหว่างดำเนินการ", time = "1w ago.", isRead = true)
        ))

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