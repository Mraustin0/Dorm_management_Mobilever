package com.example.horganized.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var btnSave: TextView
    private var isWaterTab = true
    private var isReadOnly = false
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
        setupSpinnerData()       // ตั้งค่า adapter + default selection (ไม่มี listener ยัง)
        loadDataForSelectedMonth() // โหลดครั้งแรก
        setupSpinnerListeners()  // ตั้ง listener หลังโหลดแรกเสร็จ
    }

    private fun initViews() {
        rvMeter = findViewById(R.id.rv_meter_list)
        rvMeter.layoutManager = LinearLayoutManager(this)
        spinnerFloor = findViewById(R.id.spinner_floor_meter)
        spinnerMonth = findViewById(R.id.spinner_month_meter)
        spinnerYear = findViewById(R.id.spinner_year_meter)
        btnSave = findViewById(R.id.btn_save_meter)

        findViewById<ImageView>(R.id.btn_back_meter).setOnClickListener { finish() }
        btnSave.setOnClickListener { showSaveConfirmationDialog() }
    }

    private fun setupSpinnerData() {
        val calendar = Calendar.getInstance()

        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        spinnerFloor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val months = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH))

        val currentYear = calendar.get(Calendar.YEAR)
        val years = arrayOf(
            (currentYear - 1).toString(),
            currentYear.toString(),
            (currentYear + 1).toString()
        )
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerYear.setSelection(1)
    }

    private fun setupSpinnerListeners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                loadDataForSelectedMonth()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spinnerFloor.onItemSelectedListener = listener
        spinnerMonth.onItemSelectedListener = listener
        spinnerYear.onItemSelectedListener = listener
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

    // ─── Entry point: โหลดข้อมูลตาม spinner ที่เลือก ───────────────────────────
    private fun loadDataForSelectedMonth() {
        val selectedMonth = spinnerMonth.selectedItem?.toString() ?: return
        val selectedYear = spinnerYear.selectedItem?.toString() ?: return
        val floor = spinnerFloor.selectedItemPosition + 1

        // ตรวจว่าเป็นเดือนปัจจุบันหรือไม่ → ถ้าใช่ แก้ไขได้เสมอ
        val cal = Calendar.getInstance()
        val monthNames = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )
        val isCurrentMonth = selectedMonth == monthNames[cal.get(Calendar.MONTH)] &&
                selectedYear == cal.get(Calendar.YEAR).toString()

        // Step 1: โหลดรายการห้องในชั้นนี้
        db.collection("rooms")
            .whereEqualTo("floor", floor)
            .get()
            .addOnSuccessListener { roomDocs ->
                val roomNumbers = roomDocs.documents.mapNotNull { it.getString("roomNumber") }
                if (roomNumbers.isEmpty()) {
                    isReadOnly = false
                    refreshList(emptyList())
                    updateSaveButton()
                    return@addOnSuccessListener
                }

                // Step 2: ตรวจ meter_history ของเดือน/ปีที่เลือก
                db.collection("meter_history")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener { historyDocs ->
                        val historyForFloor = historyDocs.documents.filter {
                            roomNumbers.contains(it.getString("roomNumber"))
                        }

                        if (historyForFloor.isNotEmpty()) {
                            // มีข้อมูล history อยู่แล้ว:
                            // - เดือนปัจจุบัน → โหลด history มา pre-fill แต่ยังแก้ไขได้
                            // - เดือนที่ผ่านมา → readonly
                            isReadOnly = !isCurrentMonth
                            buildListFromHistory(roomDocs.documents, historyDocs.documents, roomNumbers, selectedMonth, selectedYear)
                        } else {
                            // ยังไม่มีข้อมูล → แก้ไขได้ทุกกรณี โหลดจาก rooms
                            isReadOnly = false
                            buildListFromRooms(roomDocs.documents)
                        }
                        updateSaveButton()
                    }
                    .addOnFailureListener {
                        isReadOnly = false
                        buildListFromRooms(roomDocs.documents)
                        updateSaveButton()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดข้อมูลห้องไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── สร้างรายการจาก meter_history ───────────────────────────────────────────
    private fun buildListFromHistory(
        roomDocs: List<DocumentSnapshot>,
        historyDocs: List<DocumentSnapshot>,
        roomNumbers: List<String>,
        selectedMonth: String,
        selectedYear: String
    ) {
        val waterMap = mutableMapOf<String, Pair<Int, Int>>() // roomNum → (before, current)
        val elecMap = mutableMapOf<String, Pair<Int, Int>>()

        for (doc in historyDocs) {
            val roomNum = doc.getString("roomNumber") ?: continue
            if (!roomNumbers.contains(roomNum)) continue
            val prev = doc.getString("previousValue")?.toIntOrNull() ?: 0
            val curr = doc.getString("currentValue")?.toIntOrNull() ?: 0
            val type = doc.getString("type") ?: continue

            // fixed-ID document มี priority สูงกว่า auto-ID เสมอ
            val isFixedId = doc.id == "${roomNum}_${selectedMonth}_${selectedYear}_${type}"
            when (type) {
                "water" -> if (isFixedId || !waterMap.containsKey(roomNum)) waterMap[roomNum] = prev to curr
                "electric" -> if (isFixedId || !elecMap.containsKey(roomNum)) elecMap[roomNum] = prev to curr
            }
        }

        val items = mutableListOf<MeterItem>()
        for (roomDoc in roomDocs) {
            val roomNum = roomDoc.getString("roomNumber") ?: ""
            val isVacant = roomDoc.getBoolean("isVacant") ?: true
            val tenantId = roomDoc.getString("tenantId")
            val water = waterMap[roomNum]
            val elec = elecMap[roomNum]

            items.add(
                MeterItem(
                    roomDoc.id, roomNum, !isVacant,
                    water?.first ?: 0, water?.second ?: 0,
                    elec?.first ?: 0, elec?.second ?: 0,
                    tenantId
                )
            )
        }
        refreshList(items)
    }

    // ─── สร้างรายการจาก rooms (แก้ไขได้) ────────────────────────────────────────
    // ค่า "ก่อน" = lastWaterMeter/lastElectricMeter ที่บันทึกไว้ล่าสุด
    private fun buildListFromRooms(roomDocs: List<DocumentSnapshot>) {
        val tempItems = mutableListOf<MeterItem>()
        for (doc in roomDocs) {
            val roomNum = doc.getString("roomNumber") ?: ""
            val isVacant = doc.getBoolean("isVacant") ?: true
            val tenantId = doc.getString("tenantId")
            val wBefore = doc.getLong("lastWaterMeter")?.toInt() ?: 0
            val eBefore = doc.getLong("lastElectricMeter")?.toInt() ?: 0

            tempItems.add(
                MeterItem(
                    doc.id, roomNum, !isVacant,
                    wBefore, wBefore,
                    eBefore, eBefore,
                    tenantId
                )
            )
        }
        checkAndLoadUserMeters(tempItems)
    }

    // fallback: ถ้า lastMeter ยังเป็น 0 ให้ดึงค่าเริ่มต้นจาก users collection
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
            adapter = MeterAdapter(masterMeterList, isWaterTab, isReadOnly)
            rvMeter.adapter = adapter
        } else {
            adapter?.isWaterMode = isWaterTab
            adapter?.isReadOnly = isReadOnly
            adapter?.notifyDataSetChanged()
        }
    }

    private fun updateSaveButton() {
        if (isReadOnly) {
            btnSave.isEnabled = false
            btnSave.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        } else {
            btnSave.isEnabled = true
            btnSave.setTextColor(android.graphics.Color.parseColor("#3366CC"))
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

            // อัปเดต lastMeter ในห้อง → ใช้เป็นค่า "ก่อน" ของเดือนถัดไป
            val roomRef = db.collection("rooms").document(item.roomId)
            batch.set(
                roomRef,
                mapOf(
                    "lastWaterMeter" to item.waterLatest,
                    "lastElectricMeter" to item.elecLatest
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            // ใช้ fixed ID = "{room}_{month}_{year}_{type}" → บันทึกซ้ำจะ overwrite ไม่ duplicate
            val wHistoryRef = db.collection("meter_history")
                .document("${item.roomName}_${selectedMonth}_${selectedYear}_water")
            batch.set(
                wHistoryRef, hashMapOf(
                    "roomNumber" to item.roomName,
                    "month" to selectedMonth,
                    "year" to selectedYear,
                    "type" to "water",
                    "previousValue" to item.waterBefore.toString(),
                    "currentValue" to item.waterLatest.toString(),
                    "unitsUsed" to (item.waterLatest - item.waterBefore).coerceAtLeast(0).toString(),
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )

            val eHistoryRef = db.collection("meter_history")
                .document("${item.roomName}_${selectedMonth}_${selectedYear}_electric")
            batch.set(
                eHistoryRef, hashMapOf(
                    "roomNumber" to item.roomName,
                    "month" to selectedMonth,
                    "year" to selectedYear,
                    "type" to "electric",
                    "previousValue" to item.elecBefore.toString(),
                    "currentValue" to item.elecLatest.toString(),
                    "unitsUsed" to (item.elecLatest - item.elecBefore).coerceAtLeast(0).toString(),
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )

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

class MeterAdapter(
    private val items: List<MeterItem>,
    var isWaterMode: Boolean,
    var isReadOnly: Boolean = false
) : RecyclerView.Adapter<MeterAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvRoom: TextView = v.findViewById(R.id.tv_meter_room_name)
        val ivStatus: ImageView = v.findViewById(R.id.iv_meter_status)
        val tvBefore: TextView = v.findViewById(R.id.tv_meter_before)
        val etLatest: EditText = v.findViewById(R.id.et_meter_latest)
        val tvUnits: TextView = v.findViewById(R.id.tv_meter_units)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_meter, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRoom.text = item.roomName

        if (item.hasTenant) {
            holder.ivStatus.setImageResource(R.drawable.ic_dot_red)
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_dot_green)
        }
        holder.ivStatus.clearColorFilter()

        val isEditable = item.hasTenant && !isReadOnly
        holder.etLatest.isEnabled = isEditable
        holder.etLatest.alpha = if (isEditable) 1.0f else 0.5f

        val beforeVal = if (isWaterMode) item.waterBefore else item.elecBefore
        val currentVal = if (isWaterMode) item.waterLatest else item.elecLatest

        holder.tvBefore.text = "$beforeVal"

        // ลบ TextWatcher เก่าก่อน (ป้องกัน double-trigger เวลา recycler view reuse)
        val oldWatcher = holder.etLatest.tag as? TextWatcher
        if (oldWatcher != null) holder.etLatest.removeTextChangedListener(oldWatcher)
        holder.etLatest.tag = null

        holder.etLatest.setText("$currentVal")
        holder.tvUnits.text = (currentVal - beforeVal).coerceAtLeast(0).toString()

        if (isEditable) {
            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val input = s.toString().toIntOrNull() ?: beforeVal
                    if (isWaterMode) item.waterLatest = input else item.elecLatest = input
                    holder.tvUnits.text = (input - beforeVal).coerceAtLeast(0).toString()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
            holder.etLatest.addTextChangedListener(textWatcher)
            holder.etLatest.tag = textWatcher
        }
    }

    override fun getItemCount() = items.size
}
