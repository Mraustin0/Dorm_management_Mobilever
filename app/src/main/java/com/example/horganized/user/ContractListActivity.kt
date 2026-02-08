package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R

class ContractListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_list)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // กดที่ Card สัญญาเพื่อไปดูรายละเอียด
        findViewById<CardView>(R.id.card_contract_item).setOnClickListener {
            startActivity(Intent(this, ContractDetailActivity::class.java))
        }
    }
}