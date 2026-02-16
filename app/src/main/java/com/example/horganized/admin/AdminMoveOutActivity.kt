package com.example.horganized.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminMoveOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_move_out)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roomName = intent.getStringExtra("ROOM_NAME") ?: "ห้อง xxx"
        findViewById<TextView>(R.id.tv_room_title_move_out).text = roomName

        findViewById<ImageView>(R.id.btn_back_move_out).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_confirm_move_out).setOnClickListener {
            Toast.makeText(this, "ดำเนินการย้ายออกเรียบร้อย", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}