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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminSelectRoomBillActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var btnBack: ImageView

    private val db = FirebaseFirestore.getInstance()
    private var roomListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_select_room_bill)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSpinners()
        setupRecyclerView()

        btnBack.setOnClickListener { finish() }

        refreshData()
    }

    private fun initViews() {
        rvRooms = findViewById(R.id.rv_rooms_bill_select)
        spinnerFloor = findViewById(R.id.spinner_floor_bill)
        spinnerMonth = findViewById(R.id.spinner_month_bill)
        spinnerYear = findViewById(R.id.spinner_year_bill)
        btnBack = findViewById(R.id.btn_back_bill_select)
    }

    private fun setupSpinners() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", 
                            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        val years = arrayOf("2024", "2025", "2026")

        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        spinnerYear.setSelection(1) // 2025

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                refreshData()
            }
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
        val floor = spinnerFloor.selectedItemPosition + 1
        listenToRoomChanges(floor)
    }

    private fun listenToRoomChanges(floor: Int) {
        roomListener?.remove()
        roomListener = db.collection("rooms")
            .whereEqualTo("floor", floor)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("SelectRoomBill", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val statusMap = snapshots?.documents?.associate { 
                    it.getString("roomNumber") to (it.getBoolean("isVacant") ?: true) 
                } ?: emptyMap()

                val rooms = List(10) { i ->
                    val roomNum = "${floor}${String.format("%02d", i + 1)}"
                    Room("ห้อง $roomNum", statusMap[roomNum] ?: true)
                }
                
                rvRooms.adapter = RoomAdapter(rooms) { room ->
                    if (room.isVacant) {
                        Toast.makeText(this, "ห้องว่าง ไม่สามารถสร้างบิลได้", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this, AdminCreateBillActivity::class.java)
                        intent.putExtra("ROOM_NAME", room.name)
                        intent.putExtra("BILL_MONTH", spinnerMonth.selectedItem.toString())
                        intent.putExtra("BILL_YEAR", spinnerYear.selectedItem.toString())
                        startActivity(intent)
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
