package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminSelectRoomActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private val db = FirebaseFirestore.getInstance()
    private var roomListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_select_room)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRooms = findViewById(R.id.rv_rooms)
        spinnerFloor = findViewById(R.id.spinner_floor)

        setupSpinner()
        setupRecyclerView()
        setupBottomNavigation()

        // เริ่มต้นฟังการเปลี่ยนแปลงข้อมูลที่ชั้น 1
        listenToRoomChanges(1)
    }

    private fun setupBottomNavigation() {
        val navHome = findViewById<ImageView>(R.id.iv_nav_home)
        val navChat = findViewById<ImageView>(R.id.iv_nav_chat)

        navHome.setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        navChat.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSpinner() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = adapter

        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                listenToRoomChanges(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = GridLayoutManager(this, 2)
    }

    private fun listenToRoomChanges(floor: Int) {
        // ยกเลิก Listener ตัวเก่าก่อน (ถ้ามี)
        roomListener?.remove()

        // ฟังการเปลี่ยนแปลงข้อมูลแบบ Real-time
        roomListener = db.collection("rooms")
            .whereEqualTo("floor", floor)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("SelectRoom", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val firestoreStatus = mutableMapOf<String, Boolean>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val roomNum = doc.getString("roomNumber") ?: continue
                        firestoreStatus[roomNum] = doc.getBoolean("isVacant") ?: true
                    }
                }

                // สร้างรายการห้องและอัปเดตสี
                val updatedRooms = List(10) { i ->
                    val roomNumber = "${floor}${String.format("%02d", i + 1)}"
                    val isVacant = firestoreStatus[roomNumber] ?: true
                    Room("ห้อง $roomNumber", isVacant)
                }
                showRooms(updatedRooms)
            }
    }

    private fun showRooms(rooms: List<Room>) {
        rvRooms.adapter = RoomAdapter(rooms) { room ->
            if (room.isVacant) {
                // ห้องว่าง (สีเขียว) -> ไปหน้าย้ายเข้า
                val intent = Intent(this, AdminMoveInActivity::class.java)
                intent.putExtra("ROOM_NAME", room.name)
                startActivity(intent)
            } else {
                // ห้องมีคนพัก (สีแดง) -> เลือกทำกิจกรรม
                showOccupiedRoomOptions(room)
            }
        }
    }

    private fun showOccupiedRoomOptions(room: Room) {
        val options = arrayOf("สร้างบิลค่าเช่า", "ตรวจสอบสลิป", "ทำเรื่องย้ายออก")
        AlertDialog.Builder(this)
            .setTitle(room.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AdminCreateBillActivity::class.java)
                        intent.putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this, AdminCheckSlipActivity::class.java)
                        intent.putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this, AdminMoveOutActivity::class.java)
                        intent.putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove() // หยุดฟังเมื่อปิดหน้าจอ
    }
}
