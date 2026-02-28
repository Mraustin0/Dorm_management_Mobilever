package com.example.horganized.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.example.horganized.model.MoveOutRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminMoveOutProcessActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var requestId: String = ""
    private var userId: String = ""
    private var roomNumber: String = ""

    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var etPending: EditText
    private lateinit var etDeposit: EditText
    private lateinit var etDamage: EditText
    private lateinit var tvTotalRefund: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_move_out_process)

        requestId = intent.getStringExtra("request_id") ?: ""

        initViews()
        fetchRequestDetail()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // Setup calculation logic
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateRefund()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etPending.addTextChangedListener(textWatcher)
        etDeposit.addTextChangedListener(textWatcher)
        etDamage.addTextChangedListener(textWatcher)

        findViewById<View>(R.id.btn_approve).setOnClickListener {
            showConfirmDialog("approved")
        }

        findViewById<View>(R.id.btn_reject).setOnClickListener {
            showConfirmDialog("rejected")
        }
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserPhone = findViewById(R.id.tv_user_phone)
        etPending = findViewById(R.id.et_pending_amount)
        etDeposit = findViewById(R.id.et_deposit_amount)
        etDamage = findViewById(R.id.et_damage_fee)
        tvTotalRefund = findViewById(R.id.tv_total_refund)
    }

    private fun fetchRequestDetail() {
        db.collection("move_out_requests").document(requestId).get()
            .addOnSuccessListener { doc ->
                val request = doc.toObject(MoveOutRequest::class.java)
                if (request != null) {
                    userId = request.userId
                    roomNumber = request.roomNumber
                    findViewById<TextView>(R.id.tv_title_room).text = "แจ้งย้ายออกห้อง $roomNumber"
                    tvUserName.text = request.userName
                    
                    // Fetch phone from users collection
                    db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
                        tvUserPhone.text = userDoc.getString("phone") ?: "-"
                    }
                }
            }
    }

    private fun calculateRefund() {
        val pending = etPending.text.toString().toDoubleOrNull() ?: 0.0
        val deposit = etDeposit.text.toString().toDoubleOrNull() ?: 0.0
        val damage = etDamage.text.toString().toDoubleOrNull() ?: 0.0
        
        val total = deposit - pending - damage
        tvTotalRefund.text = String.format("%,.2f", total)
    }

    private fun showConfirmDialog(status: String) {
        val title = if (status == "approved") "ยืนยันการอนุมัติ" else "ยืนยันการปฏิเสธ"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("คุณต้องการดำเนินการนี้ใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ -> updateMoveOutStatus(status) }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun updateMoveOutStatus(status: String) {
        val batch = db.batch()

        // 1. Update Request
        val requestRef = db.collection("move_out_requests").document(requestId)
        val refund = tvTotalRefund.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
        
        // แก้ไข: ระบุประเภทข้อมูล <String, Any> ให้ชัดเจนเพื่อป้องกัน Type Mismatch
        val updates = mapOf<String, Any>(
            "status" to status,
            "depositAmount" to (etDeposit.text.toString().toDoubleOrNull() ?: 0.0),
            "refundAmount" to refund,
            "damageFee" to (etDamage.text.toString().toDoubleOrNull() ?: 0.0)
        )
        batch.update(requestRef, updates)

        if (status == "approved") {
            // 2. Set Room to Vacant
            val roomRef = db.collection("rooms").document(roomNumber)
            batch.update(roomRef, "isVacant", true, "tenantId", "")
        }

        // 3. Send Notification to User
        val msg = if (status == "approved") 
            "คำขอย้ายออกห้อง $roomNumber ได้รับการอนุมัติแล้ว ยอดเงินคืน: ${tvTotalRefund.text} บาท" 
            else "คำขอย้ายออกห้อง $roomNumber ถูกปฏิเสธ กรุณาติดต่อแอดมิน"
            
        val userNotifRef = db.collection("notifications").document()
        val notificationData = mapOf<String, Any>(
            "userId" to userId,
            "title" to "สถานะการแจ้งย้ายออก",
            "message" to msg,
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )
        batch.set(userNotifRef, notificationData)

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "ดำเนินการเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
