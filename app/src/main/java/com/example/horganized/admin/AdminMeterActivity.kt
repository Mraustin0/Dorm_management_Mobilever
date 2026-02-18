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

data class MeterItem(val room: String, val status: Boolean, val before: Int, var latest: Int)

class AdminMeterActivity : AppCompatActivity() {

    private lateinit var rvMeter: RecyclerView
    private lateinit var spinnerFloor: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private var isWaterTab = false

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
        
        // แก้ไขปุ่มบันทึกให้แสดง Alert ยืนยันก่อนกลับหน้า Home
        findViewById<TextView>(R.id.btn_save_meter).setOnClickListener {
            showSaveConfirmationDialog()
        }
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลมิเตอร์ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                // เมื่อยืนยัน ให้แสดงข้อความสำเร็จและกลับหน้า Home
                Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun setupSpinners() {
        // Floor Spinner
        val floors = arrayOf("ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
        val floorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floors)
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = floorAdapter

        spinnerFloor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateList(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Month Spinner
        val months = arrayOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", 
                            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        // Year Spinner
        val years = arrayOf("2024", "2025", "2026")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(1) // Default 2025
    }

    private fun setupTabs() {
        val tvWater = findViewById<TextView>(R.id.tv_tab_water)
        val tvElectric = findViewById<TextView>(R.id.tv_tab_electric)

        tvWater.setOnClickListener {
            isWaterTab = true
            tvWater.setTextColor(resources.getColor(R.color.black, null))
            tvWater.setTypeface(null, android.graphics.Typeface.BOLD)
            tvElectric.setTextColor(resources.getColor(R.color.gray_text, null))
            tvElectric.setTypeface(null, android.graphics.Typeface.NORMAL)
            updateList(spinnerFloor.selectedItemPosition + 1)
        }

        tvElectric.setOnClickListener {
            isWaterTab = false
            tvElectric.setTextColor(resources.getColor(R.color.black, null))
            tvElectric.setTypeface(null, android.graphics.Typeface.BOLD)
            tvWater.setTextColor(resources.getColor(R.color.gray_text, null))
            tvWater.setTypeface(null, android.graphics.Typeface.NORMAL)
            updateList(spinnerFloor.selectedItemPosition + 1)
        }
    }

    private fun updateList(floor: Int) {
        val list = List(10) { i ->
            val roomNumber = floor * 100 + (i + 1)
            // กำหนดค่า default เป็น 0 ตามคำขอ
            MeterItem("$roomNumber", (i + floor) % 2 == 0, 0, 0)
        }
        rvMeter.layoutManager = LinearLayoutManager(this)
        rvMeter.adapter = MeterAdapter(list)
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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meter, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvRoom.text = item.room
        holder.ivStatus.setColorFilter(if (item.status) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
        holder.tvBefore.text = "${item.before}"
        holder.etLatest.setText("${item.latest}")
        holder.tvUnits.text = "0"

        holder.etLatest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val latest = s.toString().toIntOrNull() ?: item.before
                val units = (latest - item.before).coerceAtLeast(0)
                holder.tvUnits.text = "$units"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = items.size
}