package com.example.horganized.user

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R

class UserProfileEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_edit)

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // ปุ่ม Submit
        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            // จำลองการบันทึกข้อมูล
            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            finish() // ย้อนกลับไปหน้า Profile
        }
    }
}