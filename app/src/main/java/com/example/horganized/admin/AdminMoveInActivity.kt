package com.example.horganized.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

class AdminMoveInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_move_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roomName = intent.getStringExtra("ROOM_NAME") ?: "ห้อง xxx"
        findViewById<TextView>(R.id.tv_room_title_move_in).text = roomName

        setupContractSpinner()

        findViewById<ImageView>(R.id.btn_back_move_in).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_save_move_in).setOnClickListener {
            showSaveConfirmationDialog()
        }
    }

    private fun setupContractSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinner_contract_term)
        val terms = arrayOf("3 เดือน", "6 เดือน", "12 เดือน")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลการย้ายเข้าใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                Toast.makeText(this, "บันทึกข้อมูลย้ายเข้าเรียบร้อย", Toast.LENGTH_SHORT).show()
                finish() // กลับไปยังหน้าเลือกห้อง
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}