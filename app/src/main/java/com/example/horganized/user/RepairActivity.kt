package com.example.horganized.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R
import com.example.horganized.model.RepairRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class RepairActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedImageUri: Uri? = null
    private var selectedRepairType: String = ""
    private var isDropdownOpen = false

    private lateinit var ivPreview: ImageView
    private lateinit var tvUploadLabel: TextView
    private lateinit var tvRepairType: TextView
    private lateinit var layoutDropdown: LinearLayout
    private lateinit var ivDropdownArrow: ImageView
    private lateinit var cvCustomType: CardView
    private lateinit var etCustomType: EditText
    private lateinit var etDescription: EditText
    private lateinit var viewHistoryDot: View

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivPreview.setImageURI(selectedImageUri)
            ivPreview.layoutParams.width = resources.getDimensionPixelSize(android.R.dimen.thumbnail_width)
            ivPreview.layoutParams.height = resources.getDimensionPixelSize(android.R.dimen.thumbnail_height)
            tvUploadLabel.text = "เปลี่ยนรูปภาพ"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvRepairType = findViewById(R.id.tv_repair_type)
        ivPreview = findViewById(R.id.iv_preview)
        tvUploadLabel = findViewById(R.id.tv_upload_label)
        layoutDropdown = findViewById(R.id.layout_dropdown_list)
        ivDropdownArrow = findViewById(R.id.iv_dropdown_arrow)
        cvCustomType = findViewById(R.id.cv_custom_type)
        etCustomType = findViewById(R.id.et_custom_type)
        etDescription = findViewById(R.id.et_description)
        viewHistoryDot = findViewById(R.id.view_history_dot)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSend = findViewById<TextView>(R.id.btn_send_repair)
        val btnHistory = findViewById<ImageView>(R.id.btn_history)
        val rlRepairType = findViewById<RelativeLayout>(R.id.rl_repair_type)
        val btnSelectImage = findViewById<CardView>(R.id.btn_select_image)

        btnBack.setOnClickListener { finish() }

        btnHistory.setOnClickListener {
            // เมื่อกดเข้าดูประวัติ ให้จุดสีแดงหายไป (ถ้าต้องการ)
            viewHistoryDot.visibility = View.GONE
            val intent = Intent(this, RepairHistoryActivity::class.java)
            startActivity(intent)
        }

        rlRepairType.setOnClickListener { toggleDropdown() }

        findViewById<TextView>(R.id.option_electric).setOnClickListener { selectType("ไฟฟ้า") }
        findViewById<TextView>(R.id.option_water).setOnClickListener { selectType("ประปา") }
        findViewById<TextView>(R.id.option_internet).setOnClickListener { selectType("อินเทอร์เน็ต") }
        findViewById<TextView>(R.id.option_other).setOnClickListener { selectType("อื่น ๆ") }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnSend.setOnClickListener {
            val finalType = if (selectedRepairType == "อื่น ๆ") etCustomType.text.toString().trim() else selectedRepairType
            val description = etDescription.text.toString().trim()

            if (finalType.isEmpty()) {
                Toast.makeText(this, "กรุณาเลือกประเภทหรือระบุหัวข้อการแจ้งซ่อม", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรายละเอียด", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRepairRequest(finalType, description)
        }

        checkRepairStatusUpdates()
    }

    private fun checkRepairStatusUpdates() {
        val userId = auth.currentUser?.uid ?: return
        // ฟังการเปลี่ยนแปลงใน repair_requests ที่มี status เปลี่ยนไปจาก pending 
        // หรือดึงแจ้งเตือนล่าสุดที่ยังไม่ได้อ่าน
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val hasRepairUpdate = snapshots.documents.any { 
                        it.getString("title")?.contains("แจ้งซ่อม") == true 
                    }
                    viewHistoryDot.visibility = if (hasRepairUpdate) View.VISIBLE else View.GONE
                }
            }
    }

    private fun toggleDropdown() {
        isDropdownOpen = !isDropdownOpen
        if (isDropdownOpen) {
            layoutDropdown.visibility = View.VISIBLE
            ivDropdownArrow.setImageResource(R.drawable.ic_chevron_up_gg)
        } else {
            layoutDropdown.visibility = View.GONE
            ivDropdownArrow.setImageResource(R.drawable.ic_chevron_down_gg)
        }
    }

    private fun selectType(type: String) {
        selectedRepairType = type
        tvRepairType.text = type
        tvRepairType.setTextColor(resources.getColor(android.R.color.black, null))
        if (type == "อื่น ๆ") cvCustomType.visibility = View.VISIBLE else cvCustomType.visibility = View.GONE
        isDropdownOpen = false
        layoutDropdown.visibility = View.GONE
        ivDropdownArrow.setImageResource(R.drawable.ic_chevron_down_gg)
    }

    private fun submitRepairRequest(repairType: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val userName = userDoc.getString("name") ?: "ไม่ระบุชื่อ"
            val roomNumber = userDoc.getString("roomNumber") ?: "ไม่ระบุห้อง"
            val userPhone = userDoc.getString("phone") ?: ""
            val requestId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            val repairRequest = RepairRequest(requestId, userId, userName, roomNumber, userPhone, repairType, description, selectedImageUri?.toString() ?: "", "pending", timestamp)

            db.collection("repair_requests").document(requestId).set(repairRequest)
                .addOnSuccessListener {
                    sendNotificationToAdmin(userName, roomNumber, repairType, timestamp)
                    Toast.makeText(this, "แจ้งซ่อมเรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
    }

    private fun sendNotificationToAdmin(userName: String, roomNumber: String, repairType: String, timestamp: Long) {
        val notifId = db.collection("notifications").document().id
        val notificationData = hashMapOf("notificationId" to notifId, "userId" to "admin", "title" to "แจ้งซ่อมใหม่: ห้อง $roomNumber", "message" to "คุณ $userName แจ้งซ่อมเรื่อง $repairType", "senderName" to userName, "timestamp" to timestamp, "isRead" to false)
        db.collection("notifications").document(notifId).set(notificationData)
    }
}
