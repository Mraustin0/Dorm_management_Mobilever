package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R

class HomeUserActivity : AppCompatActivity() {
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