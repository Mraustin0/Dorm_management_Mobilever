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
import java.util.Calendar

data class MeterItem(
    val roomId: String, 
    val roomName: String, 
    val status: Boolean, 
    var before: Int, 
    var latest: Int,
    val tenantId: String? = null
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
        updateList(1)
    }

    private fun initViews() {
        rvMeter = findViewById(R.id.rv_meter_list)
        spinnerFloor = findViewById(R.id.spinner_floor_meter)
        spinnerMonth = findViewById(R.id.spinner_month_meter)
        spinnerYear = findViewById(R.id.spinner_year_meter)

        findViewById<ImageView>(R.id.btn_back_meter).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btn_save_meter).setOnClickListener { showSaveConfirmationDialog() }
    }

    private fun setupSpinners() {
        val calendar = Calendar.getInstance()
        
        // Floor Spinner
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                updateList(p2 + 1)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Month Spinner (เลือกเดือนปัจจุบันให้)
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH))

        // Year Spinner (เลือกปีปัจจุบันให้)
        val currentYear = calendar.get(Calendar.YEAR)
        val years = arrayOf((currentYear-1).toString(), currentYear.toString(), (currentYear+1).toString())
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        spinnerYear.setSelection(1) // เลือกปีปัจจุบัน
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
                val tempItems = mutableListOf<MeterItem>()
                
                for (doc in documents) {
                    val roomNum = doc.getString("roomNumber") ?: ""
                    val isVacant = doc.getBoolean("isVacant") ?: true
                    val tenantId = doc.getString("tenantId")
                    
                    // ค่าเริ่มต้นจาก Room
                    val beforeVal = if (isWaterTab) {
                        doc.getLong("lastWaterMeter")?.toInt() ?: 0
                    } else {
                        doc.getLong("lastElectricMeter")?.toInt() ?: 0
                    }
                    
                    tempItems.add(MeterItem(doc.id, roomNum, !isVacant, beforeVal, beforeVal, tenantId))
                }
                
                // ตรวจสอบห้องที่ไม่ว่าง เพื่อดึงมิเตอร์เริ่มต้นจาก User หากใน Room เป็น 0
                checkAndLoadUserMeters(tempItems)
            }
    }

    private fun checkAndLoadUserMeters(items: List<MeterItem>) {
        var processedCount = 0
        if (items.isEmpty()) {
            displayList()
            return
        }

        for (item in items) {
            if (item.status && item.tenantId != null && item.before == 0) {
                // ถ้าห้องไม่ว่าง และมิเตอร์ในห้องเป็น 0 (อาจจะเพิ่งย้ายเข้า) ให้ไปดึงจาก User
                db.collection("users").document(item.tenantId).get()
                    .addOnSuccessListener { userDoc ->
                        val userMeter = if (isWaterTab) {
                            userDoc.getLong("waterMeter")?.toInt() ?: 0
                        } else {
                            userDoc.getLong("electricMeter")?.toInt() ?: 0
                        }
                        item.before = userMeter
                        item.latest = userMeter
                        
                        processedCount++
                        if (processedCount == items.size) displayList(items)
                    }
                    .addOnFailureListener {
                        processedCount++
                        if (processedCount == items.size) displayList(items)
                    }
            } else {
                processedCount++
                if (processedCount == items.size) displayList(items)
            }
        }
    }

    private fun displayList(items: List<MeterItem> = meterList) {
        meterList.clear()
        meterList.addAll(items.sortedBy { it.roomName })
        rvMeter.layoutManager = LinearLayoutManager(this)
        rvMeter.adapter = MeterAdapter(meterList)
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลมิเตอร์ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ -> saveMeterData() }
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

        if (totalToProcess == 0) return

        for (item in meterList) {
            val batch = db.batch()
            val roomRef = db.collection("rooms").document(item.roomId)
            batch.update(roomRef, roomFieldName, item.latest)

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

            batch.commit().addOnSuccessListener {
                completedCount++
                if (completedCount == totalToProcess) {
                    Toast.makeText(this, "บันทึกข้อมูลมิเตอร์เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
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
