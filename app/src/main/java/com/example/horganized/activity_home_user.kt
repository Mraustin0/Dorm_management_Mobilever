package com.example.horganized

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class activity_home_user : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)

        val viewAndPayBillButton = findViewById<Button>(R.id.btn_view_pay_bill)
        viewAndPayBillButton.setOnClickListener {
            val intent = Intent(this, DetailBillActivity::class.java)
            startActivity(intent)
        }
    }
}