package com.example.horganized.user

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Notification

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rv_notifications)
        rv.layoutManager = LinearLayoutManager(this)

        // ข้อมูลจำลองตามรูปใน Figma
        val mockData = listOf(
            Notification("ADMIN1", "แอดมินได้ส่งร่างสัญญาฉบับที่ 2 ให้คุณแล้ว กรุณาตรวจสอบรายละเอียดและเซ็นสัญญาภายในวันที่ 01/06/2025", "1m ago."),
            Notification("ADMIN1", "ใบแจ้งหนี้ใหม่ค่ะ ตรวจสอบยอดและชำระเงินได้ภายในวันที่ 05/05/2026", "3d ago."),
            Notification("ADMIN1", "ชำระเงินสำเร็จ แอดมินตรวจสอบยอดเงินเรียบร้อยแล้ว ขอบคุณค่ะ", "1w ago."),
            Notification("ADMIN1", "รับเรื่องแจ้งซ่อมเรียบร้อย คำขอซ่อมหลอดไฟ ของคุณอยู่ระหว่างดำเนินการ", "1w ago.")
        )

        rv.adapter = NotificationAdapter(mockData)
    }
}