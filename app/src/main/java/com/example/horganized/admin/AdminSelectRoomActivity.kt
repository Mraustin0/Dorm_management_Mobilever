package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R

class AdminSelectRoomActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_select_room)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_select_room)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRooms = findViewById(R.id.rv_rooms)
        spinnerFloor = findViewById(R.id.spinner_floor)
        btnBack = findViewById(R.id.btn_back_select_room)

        btnBack.setOnClickListener { finish() }

        setupSpinner()
        setupRecyclerView()
        
        // เริ่มต้นแสดงผลชั้น 1 ทันที
        updateRoomList(1)
    }

    private fun setupSpinner() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = adapter

        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateRoomList(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        // ตั้งค่าให้แสดงผล 2 คอลัมน์ (5 แถว รวมเป็น 10 ช่อง)
        rvRooms.layoutManager = GridLayoutManager(this, 2)
    }

    private fun updateRoomList(floor: Int) {
        // สร้างรายการห้อง 10 ห้องสำหรับแต่ละชั้น (เช่น ชั้น 1: 101-110)
        val rooms = List(10) { i ->
            val roomNumber = floor * 100 + (i + 1)
            // กำหนดสถานะว่าง/ไม่ว่าง ตัวอย่าง (สามารถปรับเปลี่ยนตามข้อมูลจริงได้)
            val isVacant = when (floor) {
                1 -> listOf(0, 3, 7, 8, 9).contains(i)
                2 -> i % 3 == 0
                3 -> i % 2 != 0
                else -> i < 5
            }
            Room("ห้อง $roomNumber", isVacant)
        }

        rvRooms.adapter = RoomAdapter(rooms) { room ->
            if (!room.isVacant) {
                val intent = Intent(this, AdminCreateBillActivity::class.java)
                intent.putExtra("ROOM_NAME", room.name)
                startActivity(intent)
            }
        }
    }
}