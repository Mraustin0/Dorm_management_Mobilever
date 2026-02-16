package com.example.horganized.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminAnnounceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_announce)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_announce).setOnClickListener {
            finish()
        }

        val etContent = findViewById<EditText>(R.id.et_announce_content)
        val btnSend = findViewById<ImageView>(R.id.btn_send_announce)

        btnSend.setOnClickListener {
            val content = etContent.text.toString()
            if (content.isNotEmpty()) {
                // Mock การส่งประกาศไปยัง Firebase/User
                Toast.makeText(this, "ส่งประกาศเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "กรุณากรอกข้อความประกาศ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}