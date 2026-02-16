package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R

class AdminMoveSelectionActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_move_selection)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRooms = findViewById(R.id.rv_rooms_move_selection)
        spinnerFloor = findViewById(R.id.spinner_floor_move_selection)
        btnBack = findViewById(R.id.btn_back_move_selection)

        btnBack.setOnClickListener { finish() }

        setupSpinner()
        setupRecyclerView()
        
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
        rvRooms.layoutManager = GridLayoutManager(this, 2)
    }

    private fun updateRoomList(floor: Int) {
        val rooms = List(10) { i ->
            val roomNumber = floor * 100 + (i + 1)
            val isVacant = when (floor) {
                1 -> listOf(0, 3, 7, 8, 9).contains(i)
                2 -> i % 3 == 0
                3 -> i % 2 != 0
                else -> i < 5
            }
            Room("ห้อง $roomNumber", isVacant)
        }

        rvRooms.adapter = RoomAdapter(rooms) { room ->
            if (room.isVacant) {
                // ห้องว่าง -> ไปหน้าย้ายเข้า
                val intent = Intent(this, AdminMoveInActivity::class.java)
                intent.putExtra("ROOM_NAME", room.name)
                startActivity(intent)
            } else {
                // ห้องไม่ว่าง -> ไปหน้าย้ายออก
                val intent = Intent(this, AdminMoveOutActivity::class.java)
                intent.putExtra("ROOM_NAME", room.name)
                startActivity(intent)
            }
        }
    }
}
