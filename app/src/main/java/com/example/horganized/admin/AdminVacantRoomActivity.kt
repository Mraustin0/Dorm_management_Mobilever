package com.example.horganized.admin

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore

class AdminVacantRoomActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var tvVacantCount: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_vacant_room)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvRooms = findViewById(R.id.rv_rooms)
        spinnerFloor = findViewById(R.id.spinner_floor)
        tvVacantCount = findViewById(R.id.tv_vacant_count)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        rvRooms.layoutManager = GridLayoutManager(this, 2)
        setupSpinner()
        loadVacantRooms(1)
    }

    private fun setupSpinner() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = adapter

        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadVacantRooms(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadVacantRooms(floor: Int) {
        db.collection("rooms")
            .whereEqualTo("floor", floor)
            .get()
            .addOnSuccessListener { snapshots ->
                val firestoreStatus = mutableMapOf<String, Boolean>()
                for (doc in snapshots) {
                    val roomNum = doc.getString("roomNumber") ?: continue
                    firestoreStatus[roomNum] = doc.getBoolean("isVacant") ?: true
                }

                // สร้างเฉพาะห้องว่าง
                val vacantRooms = List(10) { i ->
                    val roomNumber = "${floor}${String.format("%02d", i + 1)}"
                    val isVacant = firestoreStatus[roomNumber] ?: true
                    Room("ห้อง $roomNumber", isVacant)
                }.filter { it.isVacant }

                tvVacantCount.text = "${vacantRooms.size} ห้อง"

                rvRooms.adapter = RoomAdapter(vacantRooms) { /* ไม่ต้องทำอะไรเมื่อกด */ }
            }
            .addOnFailureListener { e ->
                Log.e("VacantRoom", "Error loading rooms", e)
            }
    }
}
