package com.example.horganized.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminTechnicianActivity : AppCompatActivity() {

    private lateinit var llDynamicTechContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_technician)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        llDynamicTechContainer = findViewById(R.id.ll_dynamic_tech_container)

        findViewById<ImageView>(R.id.btn_back_tech).setOnClickListener {
            finish()
        }

        // เชื่อมปุ่มบันทึก (มุมขวาบน)
        findViewById<TextView>(R.id.btn_save_tech).setOnClickListener {
            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            finish()
        }

        // เชื่อมปุ่มเพิ่มรายการ (กล่องด้านล่าง)
        findViewById<LinearLayout>(R.id.btn_add_tech_box).setOnClickListener {
            addNewTechRow()
        }
    }

    private fun addNewTechRow() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.item_dynamic_tech, null)
        
        // เพิ่มรายการใหม่เข้าไปที่ตำแหน่ง 0 (บนสุด) ของ Container
        llDynamicTechContainer.addView(rowView, 0)
    }
}