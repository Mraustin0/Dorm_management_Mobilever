package com.example.horganized.user

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.*

class UserMoveOutActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvFullName: TextView
    private lateinit var tvCitizenId: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMoveInDate: TextView
    private lateinit var tvContractFile: TextView
    private lateinit var etNotifyDate: EditText
    private lateinit var etMoveOutDate: EditText
    private lateinit var btnHistory: ImageView

    private var contractUrl: String = ""
    private var roomNumber: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_move_out)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        loadUserData()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        etNotifyDate.setOnClickListener { showDatePicker(etNotifyDate) }
        etMoveOutDate.setOnClickListener { showDatePicker(etMoveOutDate) }

        btnHistory.setOnClickListener {
            val intent = Intent(this, MoveOutHistoryActivity::class.java)
            startActivity(intent)
        }

        findViewById<AppCompatButton>(R.id.btn_submit_move_out).setOnClickListener {
            // แก้ไข: เช็คบิลค้างชำระก่อนส่งเรื่อง
            checkBillsBeforeSubmit()
        }
    }

    private fun initViews() {
        tvFullName = findViewById(R.id.tv_user_fullname)
        tvCitizenId = findViewById(R.id.tv_user_citizen_id)
        tvPhone = findViewById(R.id.tv_user_phone)
        tvEmail = findViewById(R.id.tv_user_email)
        tvMoveInDate = findViewById(R.id.tv_move_in_date)
        tvContractFile = findViewById(R.id.tv_contract_filename)
        etNotifyDate = findViewById(R.id.et_notify_date)
        etMoveOutDate = findViewById(R.id.et_move_out_date)
        btnHistory = findViewById(R.id.btn_history)

        findViewById<android.widget.RelativeLayout>(R.id.layout_view_contract).setOnClickListener {
            if (contractUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contractUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this, "ไม่มีไฟล์สัญญาในระบบ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val surname = doc.getString("surname") ?: ""
                    tvFullName.text = "$name $surname"
                    tvCitizenId.text = doc.getString("citizenId") ?: "-"
                    tvPhone.text = doc.getString("phone") ?: "-"
                    tvEmail.text = doc.getString("email") ?: "-"
                    
                    contractUrl = doc.getString("contractUrl") ?: ""
                    roomNumber = doc.getString("roomNumber") ?: ""
                    
                    val moveInTimestamp = doc.getTimestamp("moveInDate")
                    if (moveInTimestamp != null) {
                        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("th", "TH"))
                        tvMoveInDate.text = sdf.format(moveInTimestamp.toDate())
                    }
                }
            }
    }

    private fun showDatePicker(editText: EditText) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(sdf.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun checkBillsBeforeSubmit() {
        val userId = auth.currentUser?.uid ?: return
        
        // ตรวจสอบว่ามียอดค้างชำระ (status == pending) หรือไม่
        db.collection("bills")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // มีบิลค้างชำระ แสดง Alert
                    AlertDialog.Builder(this)
                        .setTitle("ค้างชำระค่าบริการ")
                        .setMessage("คุณมียอดค้างชำระ กรุณาชำระเงินให้เรียบร้อยก่อนแจ้งย้ายออก")
                        .setPositiveButton("ไปที่หน้าชำระเงิน") { _, _ ->
                            startActivity(Intent(this, DetailBillActivity::class.java))
                        }
                        .setNegativeButton("ปิด", null)
                        .show()
                } else {
                    // ไม่มีบิลค้างชำระ ดำเนินการส่งเรื่องต่อ
                    submitMoveOutRequest()
                }
            }
            .addOnFailureListener { e ->
                // หากเกิดข้อผิดพลาดในการเช็คบิล ให้ลองส่งเรื่องตามปกติ
                submitMoveOutRequest()
            }
    }

    private fun submitMoveOutRequest() {
        val userId = auth.currentUser?.uid ?: return
        val notifyDate = etNotifyDate.text.toString().trim()
        val moveOutDate = etMoveOutDate.text.toString().trim()

        if (notifyDate.isEmpty() || moveOutDate.isEmpty()) {
            Toast.makeText(this, "กรุณาเลือกวันที่ให้ครบถ้วน", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. ข้อมูลสำหรับเก็บประวัติ
        val requestData = hashMapOf(
            "userId" to userId,
            "userName" to tvFullName.text.toString(),
            "roomNumber" to roomNumber,
            "notifyDate" to notifyDate,
            "moveOutDate" to moveOutDate,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        // 2. ข้อมูลสำหรับการแจ้งเตือนแอดมิน (แสดงจุดแดง)
        val notificationData = hashMapOf(
            "userId" to "admin",
            "roomNumber" to roomNumber,
            "title" to "แจ้งย้ายออกใหม่",
            "message" to "ห้อง $roomNumber แจ้งย้ายออกวันที่ $moveOutDate",
            "notifyDate" to notifyDate,
            "moveOutDate" to moveOutDate,
            "status" to "pending",
            "type" to "move_out",
            "isRead" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("move_out_requests").add(requestData)
        
        db.collection("Admin_Notifications").add(notificationData)
            .addOnSuccessListener {
                Toast.makeText(this, "ส่งข้อมูลแจ้งย้ายออกสำเร็จ", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
