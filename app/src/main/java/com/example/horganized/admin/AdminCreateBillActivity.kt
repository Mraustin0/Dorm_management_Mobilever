package com.example.horganized.admin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminCreateBillActivity : AppCompatActivity() {

    private lateinit var etElecAfter: EditText
    private lateinit var etElecBefore: EditText
    private lateinit var tvElecUnits: TextView
    private lateinit var spinnerElecRate: Spinner
    private lateinit var tvElecTotal: TextView

    private lateinit var etWaterAfter: EditText
    private lateinit var etWaterBefore: EditText
    private lateinit var tvWaterUnits: TextView
    private lateinit var etWaterRate: EditText
    private lateinit var tvWaterTotal: TextView

    private lateinit var etAdditional: EditText
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvRoomPrice: TextView
    private lateinit var tvBillRoomNumber: TextView
    private lateinit var tvAdminUserName: TextView
    private lateinit var tvAdminUserPhone: TextView
    private lateinit var llDynamicItemsContainer: LinearLayout
    private lateinit var btnAddItem: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private var selectedElecRate: Int = 8
    private val dynamicItemPrices = mutableMapOf<View, Int>()

    private var roomNumber = ""
    private var tenantId = ""
    private var billMonth = ""
    private var billYear = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_create_bill)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // รับเดือน/ปีที่ admin เลือกจากหน้าก่อน
        val calendar = Calendar.getInstance()
        val months = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )
        billMonth = intent.getStringExtra("BILL_MONTH") ?: months[calendar.get(Calendar.MONTH)]
        billYear = intent.getStringExtra("BILL_YEAR") ?: calendar.get(Calendar.YEAR).toString()

        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        if (roomName.isNotEmpty()) {
            tvBillRoomNumber.text = "บิลค่าเช่า $roomName ($billMonth $billYear)"
            roomNumber = roomName.replace("ห้อง ", "").trim()
            fetchTenantInfo(roomNumber)
        }

        setupListeners()
        setupElecRateSpinner()
        calculateTotal()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<Button>(R.id.btn_send_bill).setOnClickListener {
            if (tenantId.isEmpty()) {
                Toast.makeText(this, "ไม่พบข้อมูลผู้เช่าในห้องนี้", Toast.LENGTH_SHORT).show()
            } else {
                showConfirmDialog()
            }
        }

        btnAddItem.setOnClickListener { addNewItemRow() }
    }

    private fun initViews() {
        tvBillRoomNumber = findViewById(R.id.tv_bill_room_number)
        tvAdminUserName = findViewById(R.id.tv_admin_user_name)
        tvAdminUserPhone = findViewById(R.id.tv_admin_user_phone)
        etElecAfter = findViewById(R.id.et_elec_after)
        etElecBefore = findViewById(R.id.et_elec_before)
        tvElecUnits = findViewById(R.id.tv_elec_units_result)
        spinnerElecRate = findViewById(R.id.spinner_elec_rate)
        tvElecTotal = findViewById(R.id.tv_elec_total_price)
        etWaterAfter = findViewById(R.id.et_water_after)
        etWaterBefore = findViewById(R.id.et_water_before)
        tvWaterUnits = findViewById(R.id.tv_water_units_result)
        etWaterRate = findViewById(R.id.et_water_rate)
        tvWaterTotal = findViewById(R.id.tv_water_total_price)
        etAdditional = findViewById(R.id.et_additional_fee)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        tvRoomPrice = findViewById(R.id.tv_room_price)
        llDynamicItemsContainer = findViewById(R.id.ll_dynamic_items_container)
        btnAddItem = findViewById(R.id.btn_add_item)
    }

    private fun fetchTenantInfo(roomNum: String) {
        db.collection("rooms").document(roomNum).get()
            .addOnSuccessListener { doc ->
                // โหลดค่าห้องถ้ามีใน Firestore
                val price = doc.getLong("price")?.toInt()
                if (price != null) {
                    tvRoomPrice.text = String.format("%,d บาท", price)
                }

                val uid = doc.getString("tenantId")
                if (uid != null) {
                    tenantId = uid
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "-"
                            val surname = userDoc.getString("surname") ?: ""
                            val phone = userDoc.getString("phone") ?: "-"
                            tvAdminUserName.text = "$name $surname"
                            tvAdminUserPhone.text = phone
                        }
                }

                // ดึงข้อมูลมิเตอร์จาก meter_history ของเดือนที่เลือก
                fetchMeterHistory(roomNum)
            }
    }

    private fun fetchMeterHistory(roomNum: String) {
        val col = db.collection("meter_history")
        val waterRef = col.document("${roomNum}_${billMonth}_${billYear}_water")
        val elecRef  = col.document("${roomNum}_${billMonth}_${billYear}_electric")

        var waterDone = false
        var elecDone  = false
        var anyFound  = false

        fun tryShowToast() {
            if (waterDone && elecDone && anyFound) {
                Toast.makeText(
                    this,
                    "ดึงข้อมูลมิเตอร์เดือน $billMonth $billYear แล้ว",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        waterRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                anyFound = true
                etWaterBefore.setText(doc.getString("previousValue") ?: "0")
                etWaterAfter.setText(doc.getString("currentValue") ?: "0")
            }
            waterDone = true
            tryShowToast()
        }

        elecRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                anyFound = true
                etElecBefore.setText(doc.getString("previousValue") ?: "0")
                etElecAfter.setText(doc.getString("currentValue") ?: "0")
            }
            elecDone = true
            tryShowToast()
        }
    }

    private fun setupElecRateSpinner() {
        val rates = arrayOf("4", "5", "6", "7", "8", "อื่นๆ")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerElecRate.adapter = adapter
        spinnerElecRate.setSelection(4)

        spinnerElecRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = rates[position]
                if (selected == "อื่นๆ") showCustomRateDialog() else {
                    selectedElecRate = selected.toInt()
                    calculateTotal()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showCustomRateDialog() {
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this).setTitle("กรอกค่าไฟเอง").setView(input)
            .setPositiveButton("ตกลง") { _, _ ->
                selectedElecRate = input.text.toString().toIntOrNull() ?: 8
                calculateTotal()
            }.setNegativeButton("ยกเลิก", null).show()
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { calculateTotal() }
            override fun afterTextChanged(s: Editable?) {}
        }
        etElecAfter.addTextChangedListener(watcher)
        etElecBefore.addTextChangedListener(watcher)
        etWaterAfter.addTextChangedListener(watcher)
        etWaterBefore.addTextChangedListener(watcher)
        etWaterRate.addTextChangedListener(watcher)
        etAdditional.addTextChangedListener(watcher)
    }

    private fun addNewItemRow() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.item_dynamic_bill, null)
        val etPrice = rowView.findViewById<EditText>(R.id.et_dynamic_item_price)
        etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dynamicItemPrices[rowView] = s.toString().toIntOrNull() ?: 0
                calculateTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        llDynamicItemsContainer.addView(rowView)
    }

    private fun calculateTotal() {
        val roomPriceString = tvRoomPrice.text.toString().replace(Regex("[^0-9]"), "")
        val roomPrice = roomPriceString.toIntOrNull() ?: 0
        val elecAfter = etElecAfter.text.toString().toIntOrNull() ?: 0
        val elecBefore = etElecBefore.text.toString().toIntOrNull() ?: 0
        val unitsElec = (elecAfter - elecBefore).coerceAtLeast(0)
        tvElecUnits.text = "= $unitsElec * "
        val totalElec = unitsElec * selectedElecRate
        tvElecTotal.text = "= $totalElec บาท"
        val waterAfter = etWaterAfter.text.toString().toIntOrNull() ?: 0
        val waterBefore = etWaterBefore.text.toString().toIntOrNull() ?: 0
        val unitsWater = (waterAfter - waterBefore).coerceAtLeast(0)
        tvWaterUnits.text = "= $unitsWater * "
        val rateWater = etWaterRate.text.toString().toIntOrNull() ?: 0
        val totalWater = unitsWater * rateWater
        tvWaterTotal.text = "= $totalWater บาท"
        val additional = etAdditional.text.toString().toIntOrNull() ?: 0
        val dynamicTotal = dynamicItemPrices.values.sum()
        val totalAll = roomPrice + totalElec + totalWater + additional + dynamicTotal
        tvTotalPrice.text = String.format("%,d บาท", totalAll)
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการส่งบิล")
            .setMessage("คุณต้องการส่งบิลสำหรับ ${tvBillRoomNumber.text} ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ -> sendBillToFirestore() }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun sendBillToFirestore() {
        val totalAmount = tvTotalPrice.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val dueDate = Timestamp(calendar.time)

        val billData = hashMapOf(
            "userId" to tenantId,
            "roomNumber" to roomNumber,
            "amount" to totalAmount,
            "month" to billMonth,
            "year" to billYear,
            "status" to "unpaid",
            "dueDate" to dueDate,
            "createdAt" to FieldValue.serverTimestamp(),
            "details" to hashMapOf(
                "roomPrice" to tvRoomPrice.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                "electricPrice" to tvElecTotal.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                "electricUnit" to tvElecUnits.text.toString().replace("= ", "").replace(" * ", ""),
                "waterPrice" to tvWaterTotal.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                "waterUnit" to tvWaterUnits.text.toString().replace("= ", "").replace(" * ", ""),
                "otherPrice" to (etAdditional.text.toString().toDoubleOrNull() ?: 0.0) + dynamicItemPrices.values.sum().toDouble()
            )
        )

        db.collection("bills").add(billData).addOnSuccessListener {
            val notification = hashMapOf(
                "userId" to tenantId,
                "title" to "บิลค่าเช่าใหม่",
                "message" to "แอดมินได้ส่งบิลประจำเดือน $billMonth $billYear ให้คุณแล้ว กรุณาชำระเงินภายในวันที่ ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate.toDate())}",
                "type" to "new_bill",
                "timestamp" to FieldValue.serverTimestamp(), // แก้ไข: ใช้ serverTimestamp เพื่อให้ตรงกับ Model
                "isRead" to false
            )
            db.collection("notifications").add(notification)
            
            Toast.makeText(this, "ส่งบิลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
