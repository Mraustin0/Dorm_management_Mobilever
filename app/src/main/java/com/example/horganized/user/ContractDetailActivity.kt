package com.example.horganized.user

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R

class ContractDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_detail)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<CardView>(R.id.menu_view_contract).setOnClickListener {
            Toast.makeText(this, "กำลังเปิดไฟล์สัญญาเช่า...", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.menu_history).setOnClickListener {
            Toast.makeText(this, "กำลังโหลดประวัติการทำสัญญา...", Toast.LENGTH_SHORT).show()
        }
    }
}