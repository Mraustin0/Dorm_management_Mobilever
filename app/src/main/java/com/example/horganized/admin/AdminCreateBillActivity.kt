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
        
        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        if (roomName.isNotEmpty()) {
            tvBillRoomNumber.text = "บิลค่าเช่า $roomName"
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
        val roomPrice = tvRoomPrice.text.toString().replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
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
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        val currentMonth = months[calendar.get(Calendar.MONTH)]
        val currentYear = calendar.get(Calendar.YEAR).toString()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val dueDate = Timestamp(calendar.time)

        // ตรวจสอบว่ามีบิลเดือน/ปีเดียวกันอยู่แล้วหรือไม่
        db.collection("bills")
            .whereEqualTo("userId", tenantId)
            .whereEqualTo("month", currentMonth)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { existingBills ->
                if (!existingBills.isEmpty) {
                    // มีบิลซ้ำ → แจ้งเตือน ไม่ให้สร้าง
                    AlertDialog.Builder(this)
                        .setTitle("ไม่สามารถสร้างบิลได้")
                        .setMessage("ห้อง $roomNumber มีบิลประจำเดือน $currentMonth $currentYear อยู่แล้ว\nไม่สามารถสร้างบิลซ้ำได้")
                        .setPositiveButton("ตกลง", null)
                        .show()
                    return@addOnSuccessListener
                }

                // ไม่ซ้ำ → สร้างบิลได้
                val billData = hashMapOf(
                    "userId" to tenantId,
                    "roomNumber" to roomNumber,
                    "amount" to totalAmount,
                    "month" to currentMonth,
                    "year" to currentYear,
                    "status" to "unpaid",
                    "dueDate" to dueDate,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "details" to hashMapOf(
                        "roomPrice" to tvRoomPrice.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                        "electricPrice" to tvElecTotal.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                        "electricUnit" to tvElecUnits.text.toString().replace("= ", "").replace(" * ", ""),
                        "waterPrice" to tvWaterTotal.text.toString().replace(Regex("[^0-9]"), "").toDoubleOrNull(),
                        "otherPrice" to (etAdditional.text.toString().toDoubleOrNull() ?: 0.0) + dynamicItemPrices.values.sum().toDouble()
                    )
                )

                db.collection("bills").add(billData).addOnSuccessListener {
                    // สร้างการแจ้งเตือนส่งให้ User
                    val notifId = db.collection("notifications").document().id
                    val notification = hashMapOf(
                        "notificationId" to notifId,
                        "userId" to tenantId,
                        "title" to "บิลค่าเช่าใหม่",
                        "message" to "แอดมินได้ส่งบิลประจำเดือน $currentMonth $currentYear ให้คุณแล้ว กรุณาชำระเงินภายในวันที่ ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate.toDate())}",
                        "senderName" to "ADMIN1",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )
                    db.collection("notifications").document(notifId).set(notification)

                    Toast.makeText(this, "ส่งบิลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminCreateBill", "Error checking duplicate bill", e)
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}