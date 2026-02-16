package com.example.horganized.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class ChatDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_top_bar_detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // รับชื่อห้องจาก Intent และแสดงผล
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "ห้อง 101"
        findViewById<TextView>(R.id.tv_chat_detail_title).text = roomName
        findViewById<TextView>(R.id.tv_banner_text).text = "$roomName คุณ Tle"

        val btnBack = findViewById<ImageView>(R.id.btn_back_chat_detail)
        btnBack.setOnClickListener {
            finish()
        }
    }
}