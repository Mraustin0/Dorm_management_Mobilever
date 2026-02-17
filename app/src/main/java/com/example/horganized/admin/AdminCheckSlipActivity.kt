package com.example.horganized.admin

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.horganized.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminCheckSlipActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvSlips: RecyclerView
    private lateinit var tvPendingCount: TextView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var spinnerFloor: Spinner
    private lateinit var spinnerMonth: Spinner

    private var allSlips = listOf<PendingSlip>()
    private var selectedFloor = "ทุกชั้น"
    private var selectedMonth = "ทุกเดือน"

    private val floorOptions = arrayOf("ทุกชั้น", "ชั้น 1", "ชั้น 2", "ชั้น 3", "ชั้น 4")
    private val monthOptions = arrayOf(
        "ทุกเดือน",
        "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
        "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม",
        "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_check_slip)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvSlips = findViewById(R.id.rv_pending_slips)
        tvPendingCount = findViewById(R.id.tv_pending_count)
        layoutEmpty = findViewById(R.id.layout_empty)
        spinnerFloor = findViewById(R.id.spinner_filter_floor)
        spinnerMonth = findViewById(R.id.spinner_filter_month)

        rvSlips.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btn_back_check_slip).setOnClickListener {
            finish()
        }

        setupFilters()
        createMockBillIfNeeded()
        loadPendingSlips()
    }

    private fun setupFilters() {
        val floorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, floorOptions)
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFloor.adapter = floorAdapter

        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthOptions)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedFloor = spinnerFloor.selectedItem.toString()
                selectedMonth = spinnerMonth.selectedItem.toString()
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFloor.onItemSelectedListener = filterListener
        spinnerMonth.onItemSelectedListener = filterListener
    }

    private fun applyFilters() {
        var filtered = allSlips

        if (selectedFloor != "ทุกชั้น") {
            val floorNum = selectedFloor.replace("ชั้น ", "")
            filtered = filtered.filter { it.roomNumber.startsWith(floorNum) }
        }

        if (selectedMonth != "ทุกเดือน") {
            filtered = filtered.filter { it.month == selectedMonth }
        }

        tvPendingCount.text = "รายการรอตรวจ: ${filtered.size}"

        if (filtered.isEmpty()) {
            rvSlips.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvSlips.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }

        rvSlips.adapter = SlipAdapter(
            slips = filtered,
            onViewSlip = { slip -> showSlipDialog(slip) },
            onApprove = { slip -> confirmApprove(slip) },
            onReject = { slip -> confirmReject(slip) }
        )
    }

    private fun loadPendingSlips() {
        db.collection("bills")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("CheckSlip", "Error loading slips", error)
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener

                allSlips = snapshots.documents.mapNotNull { doc ->
                    val roomNumber = doc.getString("roomNumber") ?: ""
                    val amount = doc.getDouble("amount") ?: 0.0
                    val month = doc.getString("month") ?: ""
                    val slipUrl = doc.getString("slipUrl") ?: ""
                    val paymentDate = doc.getTimestamp("paymentDate")

                    PendingSlip(
                        billId = doc.id,
                        roomNumber = roomNumber,
                        amount = amount,
                        month = month,
                        slipUrl = slipUrl,
                        paymentDate = paymentDate
                    )
                }

                applyFilters()
            }
    }

    /**
     * สร้าง mock bill สำหรับทดสอบ (จะสร้างแค่ครั้งแรกถ้ายังไม่มี)
     */
    private fun createMockBillIfNeeded() {
        val mockId = "mock_bill_101_feb"
        db.collection("bills").document(mockId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val mockBill = hashMapOf(
                        "userId" to "mock_user_001",
                        "amount" to 6064.0,
                        "month" to "กุมภาพันธ์",
                        "status" to "pending",
                        "roomNumber" to "101",
                        "slipUrl" to "https://via.placeholder.com/400x600.png?text=Mock+Slip+101",
                        "paymentDate" to Timestamp.now(),
                        "dueDate" to Timestamp.now(),
                        "details" to hashMapOf(
                            "roomPrice" to 4800.0,
                            "electricPrice" to 452.0,
                            "electricUnit" to "79 หน่วย",
                            "waterPrice" to 125.0,
                            "otherPrice" to 687.0
                        )
                    )
                    db.collection("bills").document(mockId).set(mockBill)
                        .addOnSuccessListener {
                            Log.d("CheckSlip", "Mock bill created")
                        }

                    // สร้างอีก 1 บิล ชั้น 2
                    val mockId2 = "mock_bill_203_feb"
                    val mockBill2 = hashMapOf(
                        "userId" to "mock_user_002",
                        "amount" to 5230.0,
                        "month" to "กุมภาพันธ์",
                        "status" to "pending",
                        "roomNumber" to "203",
                        "slipUrl" to "https://via.placeholder.com/400x600.png?text=Mock+Slip+203",
                        "paymentDate" to Timestamp.now(),
                        "dueDate" to Timestamp.now(),
                        "details" to hashMapOf(
                            "roomPrice" to 4800.0,
                            "electricPrice" to 280.0,
                            "electricUnit" to "40 หน่วย",
                            "waterPrice" to 100.0,
                            "otherPrice" to 50.0
                        )
                    )
                    db.collection("bills").document(mockId2).set(mockBill2)
                }
            }
    }

    private fun showSlipDialog(slip: PendingSlip) {
        val dialog = Dialog(this)
        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        Glide.with(this)
            .load(slip.slipUrl)
            .into(imageView)

        dialog.setContentView(imageView)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun confirmApprove(slip: PendingSlip) {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการชำระเงิน")
            .setMessage("ยืนยันว่าห้อง ${slip.roomNumber} ชำระ ${String.format("%,.0f", slip.amount)} บาท เรียบร้อยแล้ว?")
            .setPositiveButton("ยืนยัน") { _, _ ->
                db.collection("bills").document(slip.billId)
                    .update("status", "paid")
                    .addOnSuccessListener {
                        Toast.makeText(this, "ยืนยันการชำระเงินเรียบร้อย", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun confirmReject(slip: PendingSlip) {
        AlertDialog.Builder(this)
            .setTitle("ปฏิเสธสลิป")
            .setMessage("ปฏิเสธสลิปของห้อง ${slip.roomNumber}?")
            .setPositiveButton("ปฏิเสธ") { _, _ ->
                db.collection("bills").document(slip.billId)
                    .update("status", "rejected")
                    .addOnSuccessListener {
                        Toast.makeText(this, "ปฏิเสธสลิปเรียบร้อย", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}
