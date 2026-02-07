package com.example.horganized.admin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R

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
    private lateinit var llDynamicItemsContainer: LinearLayout
    private lateinit var btnAddItem: LinearLayout

    private var selectedElecRate: Int = 8
    private val dynamicItemPrices = mutableMapOf<View, Int>()

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
        setupListeners()
        setupElecRateSpinner()
        calculateTotal()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_send_bill).setOnClickListener {
            showConfirmDialog()
        }

        btnAddItem.setOnClickListener {
            addNewItemRow()
        }
    }

    private fun initViews() {
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

    private fun setupElecRateSpinner() {
        val rates = arrayOf("4", "5", "6", "7", "8", "อื่นๆ")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerElecRate.adapter = adapter
        spinnerElecRate.setSelection(4)

        spinnerElecRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = rates[position]
                if (selected == "อื่นๆ") {
                    showCustomRateDialog()
                } else {
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
        AlertDialog.Builder(this)
            .setTitle("กรอกค่าไฟเอง")
            .setView(input)
            .setPositiveButton("ตกลง") { _, _ ->
                selectedElecRate = input.text.toString().toIntOrNull() ?: 8
                calculateTotal()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotal()
            }
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
        val roomPriceStr = tvRoomPrice.text.toString().replace(Regex("[^0-9]"), "")
        val roomPrice = roomPriceStr.toIntOrNull() ?: 0

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
        
        // รวมรายการที่เพิ่มขึ้นมาใหม่
        val dynamicTotal = dynamicItemPrices.values.sum()

        val totalAll = roomPrice + totalElec + totalWater + additional + dynamicTotal
        tvTotalPrice.text = String.format("%,d บาท", totalAll)
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการส่งบิล")
            .setMessage("คุณต้องการส่งบิลสำหรับ ${findViewById<TextView>(R.id.tv_bill_room_number).text} ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                AlertDialog.Builder(this)
                    .setMessage("ส่งบิลเรียบร้อยแล้ว")
                    .setPositiveButton("ตกลง") { _, _ -> finish() }
                    .show()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}