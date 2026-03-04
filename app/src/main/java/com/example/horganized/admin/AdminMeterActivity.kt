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
    val hasTenant: Boolean, 
    var waterBefore: Int, 
    var waterLatest: Int,
    var elecBefore: Int,
    var elecLatest: Int,
    val tenantId: String? = null
)

class AdminMeterActivity : AppCompatActivity() {

    private lateinit var rvMeter: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private var isWaterTab = true
    private val db = FirebaseFirestore.getInstance()
    private val masterMeterList = mutableListOf<MeterItem>()
    private var adapter: MeterAdapter? = null

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
        
        loadFloorData(1)
    }

    private fun initViews() {
        rvMeter = findViewById(R.id.rv_meter_list)
        rvMeter.layoutManager = LinearLayoutManager(this)
        
        spinnerFloor = findViewById(R.id.spinner_floor_meter)
        spinnerMonth = findViewById(R.id.spinner_month_meter)
        spinnerYear = findViewById(R.id.spinner_year_meter)

        findViewById<ImageView>(R.id.btn_back_meter).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btn_save_meter).setOnClickListener { showSaveConfirmationDialog() }
    }

    private fun setupSpinners() {
        val calendar = Calendar.getInstance()
        
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                loadFloorData(p2 + 1)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH))

        val currentYear = calendar.get(Calendar.YEAR)
        val years = arrayOf((currentYear-1).toString(), currentYear.toString(), (currentYear+1).toString())
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
            refreshList()
        }

        tvElectric.setOnClickListener {
            isWaterTab = false
            tvElectric.setTextColor(resources.getColor(R.color.black, null))
            tvWater.setTextColor(resources.getColor(R.color.gray_text, null))
            refreshList()
        }
    }

    private fun loadFloorData(floor: Int) {
        db.collection("rooms")
            .whereEqualTo("floor", floor)
            .get()
            .addOnSuccessListener { documents ->
                masterMeterList.clear()
                val tempItems = mutableListOf<MeterItem>()
                
                for (doc in documents) {
                    val roomNum = doc.getString("roomNumber") ?: ""
                    val isVacant = doc.getBoolean("isVacant") ?: true
                    val tenantId = doc.getString("tenantId")
                    
                    val wBefore = doc.getLong("lastWaterMeter")?.toInt() ?: 0
                    val eBefore = doc.getLong("lastElectricMeter")?.toInt() ?: 0
                    
                    tempItems.add(MeterItem(
                        doc.id, roomNum, !isVacant, 
                        wBefore, wBefore, 
                        eBefore, eBefore, 
                        tenantId
                    ))
                }
                
                checkAndLoadUserMeters(tempItems)
            }
    }

    private fun checkAndLoadUserMeters(items: List<MeterItem>) {
        var processedCount = 0
        if (items.isEmpty()) {
            refreshList(items)
            return
        }

        for (item in items) {
            if (item.hasTenant && item.tenantId != null && (item.waterBefore == 0 || item.elecBefore == 0)) {
                db.collection("users").document(item.tenantId).get()
                    .addOnSuccessListener { userDoc ->
                        if (item.waterBefore == 0) {
                            val uWater = userDoc.getLong("waterMeter")?.toInt() ?: 0
                            item.waterBefore = uWater
                            item.waterLatest = uWater
                        }
                        if (item.elecBefore == 0) {
                            val uElec = userDoc.getLong("electricMeter")?.toInt() ?: 0
                            item.elecBefore = uElec
                            item.elecLatest = uElec
                        }
                        
                        processedCount++
                        if (processedCount == items.size) refreshList(items)
                    }
                    .addOnFailureListener {
                        processedCount++
                        if (processedCount == items.size) refreshList(items)
                    }
            } else {
                processedCount++
                if (processedCount == items.size) refreshList(items)
            }
        }
    }

    private fun refreshList(newList: List<MeterItem>? = null) {
        if (newList != null) {
            masterMeterList.clear()
            masterMeterList.addAll(newList.sortedBy { it.roomName })
        }
        
        if (adapter == null) {
            adapter = MeterAdapter(masterMeterList, isWaterTab)
            rvMeter.adapter = adapter
        } else {
            adapter?.isWaterMode = isWaterTab
            adapter?.notifyDataSetChanged()
        }
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลมิเตอร์ของเดือนนี้ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ -> saveMeterData() }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun saveMeterData() {
        val selectedMonth = spinnerMonth.selectedItem.toString()
        val selectedYear = spinnerYear.selectedItem.toString()
        
        var completedCount = 0
        val totalToProcess = masterMeterList.size
        if (totalToProcess == 0) return

        for (item in masterMeterList) {
            if (!item.hasTenant) {
                completedCount++
                checkAllSaved(completedCount, totalToProcess)
                continue
            }

            val batch = db.batch()
            val roomRef = db.collection("rooms").document(item.roomId)
            
            batch.update(roomRef, mapOf(
                "lastWaterMeter" to item.waterLatest,
                "lastElectricMeter" to item.elecLatest
            ))

            val wHistoryRef = db.collection("meter_history").document()
            batch.set(wHistoryRef, hashMapOf(
                "roomNumber" to item.roomName,
                "month" to selectedMonth,
                "year" to selectedYear,
                "type" to "water",
                "previousValue" to item.waterBefore.toString(),
                "currentValue" to item.waterLatest.toString(),
                "unitsUsed" to (item.waterLatest - item.waterBefore).coerceAtLeast(0).toString(),
                "timestamp" to FieldValue.serverTimestamp()
            ))

            val eHistoryRef = db.collection("meter_history").document()
            batch.set(eHistoryRef, hashMapOf(
                "roomNumber" to item.roomName,
                "month" to selectedMonth,
                "year" to selectedYear,
                "type" to "electric",
                "previousValue" to item.elecBefore.toString(),
                "currentValue" to item.elecLatest.toString(),
                "unitsUsed" to (item.elecLatest - item.elecBefore).coerceAtLeast(0).toString(),
                "timestamp" to FieldValue.serverTimestamp()
            ))

            batch.commit().addOnCompleteListener {
                completedCount++
                checkAllSaved(completedCount, totalToProcess)
            }
        }
    }

    private fun checkAllSaved(current: Int, total: Int) {
        if (current == total) {
            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว ✓", Toast.LENGTH_SHORT).show()
        }
    }
}

class MeterAdapter(private val items: List<MeterItem>, var isWaterMode: Boolean) : RecyclerView.Adapter<MeterAdapter.ViewHolder>() {
    
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
        
        // แก้ไขให้ใช้ icon dot ที่สร้างใหม่
        if (item.hasTenant) {
            holder.ivStatus.setImageResource(R.drawable.ic_dot_red)
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_dot_green)
        }
        
        // ล้าง ColorFilter เดิมออก (ถ้ามี)
        holder.ivStatus.clearColorFilter()

        holder.etLatest.isEnabled = item.hasTenant
        if (!item.hasTenant) {
            holder.etLatest.alpha = 0.5f
        } else {
            holder.etLatest.alpha = 1.0f
        }

        val beforeVal = if (isWaterMode) item.waterBefore else item.elecBefore
        val currentVal = if (isWaterMode) item.waterLatest else item.elecLatest
        
        holder.tvBefore.text = "$beforeVal"
        
        holder.etLatest.tag = null 
        holder.etLatest.setText("$currentVal")
        holder.tvUnits.text = (currentVal - beforeVal).coerceAtLeast(0).toString()

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().toIntOrNull() ?: beforeVal
                if (isWaterMode) {
                    item.waterLatest = input
                } else {
                    item.elecLatest = input
                }
                holder.tvUnits.text = (input - beforeVal).coerceAtLeast(0).toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        
        holder.etLatest.addTextChangedListener(textWatcher)
        holder.etLatest.tag = textWatcher
    }

    override fun getItemCount() = items.size
}
