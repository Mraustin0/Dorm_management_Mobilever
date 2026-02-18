package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
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
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner

    private val db = FirebaseFirestore.getInstance()
    private var roomListener: ListenerRegistration? = null
    private var mode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_select_room)

        mode = intent.getStringExtra("MODE") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSpinners()
        setupRecyclerView()
        setupBottomNavigation()

        refreshData()
    }

    private fun initViews() {
        rvRooms = findViewById(R.id.rv_rooms)
        spinnerFloor = findViewById(R.id.spinner_floor)
        spinnerMonth = findViewById(R.id.spinner_month_select)
        spinnerYear = findViewById(R.id.spinner_year_select)
    }

    private fun setupBottomNavigation() {
        findViewById<ImageView>(R.id.iv_nav_home).setOnClickListener {
            startActivity(Intent(this, AdminHomeActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
        }
        findViewById<ImageView>(R.id.iv_nav_chat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
    }

    private fun setupSpinners() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        val years = arrayOf("2024", "2025", "2026")

        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerYear.setSelection(1)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { refreshData() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spinnerFloor.onItemSelectedListener = listener
        spinnerMonth.onItemSelectedListener = listener
        spinnerYear.onItemSelectedListener = listener
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = GridLayoutManager(this, 2)
    }

    private fun refreshData() {
        listenToRoomChanges(spinnerFloor.selectedItemPosition + 1)
    }

    private fun listenToRoomChanges(floor: Int) {
        roomListener?.remove()
        roomListener = db.collection("rooms").whereEqualTo("floor", floor)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { Log.e("SelectRoom", "Listen failed.", e); return@addSnapshotListener }

                val statusMap = snapshots?.documents?.associate { it.getString("roomNumber") to (it.getBoolean("isVacant") ?: true) } ?: emptyMap()
                val rooms = List(10) { Room("ห้อง ${floor}${String.format("%02d", it + 1)}", statusMap["${floor}${String.format("%02d", it + 1)}"] ?: true) }
                
                showRooms(rooms)
            }
    }

    private fun showRooms(rooms: List<Room>) {
        rvRooms.adapter = RoomAdapter(rooms) { room ->
            if (room.isVacant) {
                Toast.makeText(this, "${room.name} เป็นห้องว่าง", Toast.LENGTH_SHORT).show()
            } else {
                // ไม่มี Pop-up: ทำงานตามโหมดที่ได้รับมาทันที
                when (mode) {
                    "CREATE_BILL" -> {
                        val intent = Intent(this, AdminCreateBillActivity::class.java).putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                    "CHECK_SLIP" -> {
                        val intent = Intent(this, AdminCheckSlipActivity::class.java).putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                    else -> { // กรณีอื่นๆ หรือกดจากเมนูย้ายเข้า/ออกโดยตรง
                        val intent = Intent(this, AdminMoveOutActivity::class.java).putExtra("ROOM_NAME", room.name)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
