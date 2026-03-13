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
import java.util.Calendar

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
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)   // 0-based (มกราคม = 0)
        val currentYear  = cal.get(Calendar.YEAR)

        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
                             "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        val years = Array(3) { (currentYear - 1 + it).toString() }  // ปีก่อน, ปีนี้, ปีหน้า

        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                refreshData()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerFloor.onItemSelectedListener = listener
        spinnerMonth.onItemSelectedListener = listener
        spinnerYear.onItemSelectedListener = listener

        // post() เพื่อให้ทำงานหลัง adapter's initial onItemSelected(0) ที่ Android post ไว้
        spinnerMonth.post { spinnerMonth.setSelection(currentMonth) }
        spinnerYear.post { spinnerYear.setSelection(1) }
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

                val selectedMonth = spinnerMonth.selectedItem.toString()
                val selectedYear  = spinnerYear.selectedItem.toString()

                // ดึงบิลของเดือน/ปีที่เลือก เพื่อเช็คว่าห้องไหนส่งบิลไปแล้ว
                db.collection("bills")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener { billSnapshots ->
                        // billMap: roomNumber → Pair(billId, status)
                        val billMap = billSnapshots.documents.associate { doc ->
                            (doc.getString("roomNumber") ?: "") to Pair(
                                doc.id,
                                doc.getString("status") ?: ""
                            )
                        }

                        val rooms = List(10) { i ->
                            val roomNum = "${floor}${String.format("%02d", i + 1)}"
                            val billInfo = billMap[roomNum]
                            Room(
                                name       = "ห้อง $roomNum",
                                isVacant   = statusMap[roomNum] ?: true,
                                hasBill    = billInfo != null,
                                billId     = billInfo?.first ?: "",
                                billStatus = billInfo?.second ?: ""
                            )
                        }

                        rvRooms.adapter = RoomAdapter(rooms) { room ->
                            when {
                                room.isVacant -> Toast.makeText(
                                    this, "ห้องว่าง ไม่สามารถสร้างบิลได้", Toast.LENGTH_SHORT
                                ).show()
                                room.hasBill -> {
                                    val intent = Intent(this, AdminViewBillActivity::class.java)
                                    intent.putExtra("BILL_ID", room.billId)
                                    intent.putExtra("ROOM_NAME", room.name)
                                    startActivity(intent)
                                }
                                else -> {
                                    val intent = Intent(this, AdminCreateBillActivity::class.java)
                                    intent.putExtra("ROOM_NAME", room.name)
                                    intent.putExtra("BILL_MONTH", selectedMonth)
                                    intent.putExtra("BILL_YEAR", selectedYear)
                                    startActivity(intent)
                                }
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
