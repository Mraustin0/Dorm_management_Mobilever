package com.example.horganized.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

data class MeterItem(
    val roomId: String, 
    val roomName: String, 
    val status: Boolean, 
    val before: Int, 
    var latest: Int
)

class AdminMeterActivity : AppCompatActivity() {

    private lateinit var rvMeter: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private var isWaterTab = false
    private val db = FirebaseFirestore.getInstance()
    private val meterList = mutableListOf<MeterItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_meter)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupTabs()
        setupSpinners()
        
        initializeMeterFields()
        updateList(1)
    }

    private fun initViews() {
        rvMeter = findViewById(R.id.rv_meter_list)
        spinnerFloor = findViewById(R.id.spinner_floor_meter)
        spinnerMonth = findViewById(R.id.spinner_month_meter)
        spinnerYear = findViewById(R.id.spinner_year_meter)

        findViewById<ImageView>(R.id.btn_back_meter).setOnClickListener { finish() }
        
        findViewById<TextView>(R.id.btn_save_meter).setOnClickListener {
            showSaveConfirmationDialog()
        }
    }

    private fun initializeMeterFields() {
        db.collection("rooms").get().addOnSuccessListener { documents ->
            val batch = db.batch()
            var needsUpdate = false
            for (document in documents) {
                if (!document.contains("lastElectricMeter") || !document.contains("lastWaterMeter")) {
                    val ref = db.collection("rooms").document(document.id)
                    batch.update(ref, mapOf(
                        "lastElectricMeter" to 0,
                        "lastWaterMeter" to 0
                    ))
                    needsUpdate = true
                }
            }
            if (needsUpdate) batch.commit()
        }
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลมิเตอร์และอัปเดตไปยังระบบบิลใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                saveMeterData()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun saveMeterData() {
        val selectedMonth = spinnerMonth.selectedItem.toString()
        val selectedYear = spinnerYear.selectedItem.toString()
        val type = if (isWaterTab) "water" else "electric"
        val roomFieldName = if (isWaterTab) "lastWaterMeter" else "lastElectricMeter"
        
        var completedCount = 0
        val totalToProcess = meterList.size

        for (item in meterList) {
            val batch = db.batch()
            
            // 1. อัปเดตค่าล่าสุดในคอลเลกชัน rooms
            val roomRef = db.collection("rooms").document(item.roomId)
            batch.update(roomRef, roomFieldName, item.latest)

            // 2. บันทึกลงในคอลเลกชัน meter_history
            val historyRef = db.collection("meter_history").document()
            val unitsUsed = (item.latest - item.before).coerceAtLeast(0)
            
            val historyData = hashMapOf(
                "roomNumber" to item.roomName,
                "month" to selectedMonth,
                "year" to selectedYear,
                "type" to type,
                "previousValue" to item.before.toString(),
                "currentValue" to item.latest.toString(),
                "unitsUsed" to unitsUsed.toString(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            batch.set(historyRef, historyData)

            // 3. ค้นหาและอัปเดต/สร้างบิล (Collection: bills)
            db.collection("bills")
                .whereEqualTo("roomNumber", item.roomName)
                .whereEqualTo("month", selectedMonth)
                .whereEqualTo("year", selectedYear)
                .get()
                .addOnSuccessListener { billDocs ->
                    if (!billDocs.isEmpty) {
                        // มีบิลอยู่แล้ว -> อัปเดตข้อมูลมิเตอร์ในบิลเดิม
                        val billRef = db.collection("bills").document(billDocs.documents[0].id)
                        val billUpdate = if (isWaterTab) {
                            mapOf("details.waterUnit" to unitsUsed.toString())
                        } else {
                            mapOf("details.electricUnit" to unitsUsed.toString())
                        }
                        billRef.update(billUpdate)
                    } else {
                        // ยังไม่มีบิล -> สร้างร่างบิลใหม่ (เฉพาะกรณีห้องไม่ว่าง)
                        if (item.status) {
                            val newBillRef = db.collection("bills").document()
                            val initialDetails = if (isWaterTab) {
                                mapOf("waterUnit" to unitsUsed.toString(), "electricUnit" to "0")
                            } else {
                                mapOf("electricUnit" to unitsUsed.toString(), "waterUnit" to "0")
                            }
                            
                            val newBillData = hashMapOf(
                                "roomNumber" to item.roomName,
                                "month" to selectedMonth,
                                "year" to selectedYear,
                                "status" to "pending",
                                "details" to initialDetails,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            newBillRef.set(newBillData)
                        }
                    }
                    
                    batch.commit().addOnSuccessListener {
                        completedCount++
                        if (completedCount == totalToProcess) {
                            Toast.makeText(this, "บันทึกข้อมูลและส่งไปยังระบบบิลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
        }
    }

    private fun setupSpinners() {
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateList(p2 + 1)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)

        val years = arrayOf("2024", "2025", "2026")
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        spinnerYear.setSelection(1)
    }

    private fun setupTabs() {
        val tvWater = findViewById<TextView>(R.id.tv_tab_water)
        val tvElectric = findViewById<TextView>(R.id.tv_tab_electric)

        tvWater.setOnClickListener {
            isWaterTab = true
            tvWater.setTextColor(resources.getColor(R.color.black, null))
            tvElectric.setTextColor(resources.getColor(R.color.gray_text, null))
            updateList(spinnerFloor.selectedItemPosition + 1)
        }

        tvElectric.setOnClickListener {
            isWaterTab = false
            tvElectric.setTextColor(resources.getColor(R.color.black, null))
            tvWater.setTextColor(resources.getColor(R.color.gray_text, null))
            updateList(spinnerFloor.selectedItemPosition + 1)
        }
    }

    private fun updateList(floor: Int) {
        db.collection("rooms")
            .whereEqualTo("floor", floor)
            .get()
            .addOnSuccessListener { documents ->
                meterList.clear()
                for (doc in documents) {
                    val roomNum = doc.getString("roomNumber") ?: ""
                    val isVacant = doc.getBoolean("isVacant") ?: true
                    val beforeVal = if (isWaterTab) {
                        doc.getLong("lastWaterMeter")?.toInt() ?: 0
                    } else {
                        doc.getLong("lastElectricMeter")?.toInt() ?: 0
                    }
                    
                    meterList.add(MeterItem(doc.id, roomNum, !isVacant, beforeVal, beforeVal))
                }
                meterList.sortBy { it.roomName }
                
                rvMeter.layoutManager = LinearLayoutManager(this)
                rvMeter.adapter = MeterAdapter(meterList)
            }
    }
}

class MeterAdapter(private val items: List<MeterItem>) : RecyclerView.Adapter<MeterAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvRoom: TextView = v.findViewById(R.id.tv_meter_room_name)
        val ivStatus: ImageView = v.findViewById(R.id.iv_meter_status)
        val tvBefore: TextView = v.findViewById(R.id.tv_meter_before)
        val etLatest: EditText = v.findViewById(R.id.et_meter_latest)
        val tvUnits: TextView = v.findViewById(R.id.tv_meter_units)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_meter, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRoom.text = item.roomName
        holder.ivStatus.setColorFilter(if (item.status) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
        holder.tvBefore.text = "${item.before}"
        holder.etLatest.setText("${item.latest}")
        holder.tvUnits.text = "0"

        holder.etLatest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().toIntOrNull() ?: item.before
                item.latest = input
                holder.tvUnits.text = (item.latest - item.before).coerceAtLeast(0).toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = items.size
}
