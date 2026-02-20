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
import java.util.UUID

class ReportRepairActivity : AppCompatActivity() {

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
    private lateinit var cvOtherType: CardView
    private lateinit var etOtherType: EditText
    private lateinit var etDescription: EditText

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
        setContentView(R.layout.activity_report_repair)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvRepairType = findViewById(R.id.tv_repair_type)
        ivPreview = findViewById(R.id.iv_preview)
        tvUploadLabel = findViewById(R.id.tv_upload_label)
        layoutDropdown = findViewById(R.id.layout_dropdown_list)
        ivDropdownArrow = findViewById(R.id.iv_dropdown_arrow)
        cvOtherType = findViewById(R.id.cv_other_type)
        etOtherType = findViewById(R.id.et_other_type)
        etDescription = findViewById(R.id.et_description)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSubmit = findViewById<TextView>(R.id.btn_submit)
        val btnHistory = findViewById<ImageView>(R.id.btn_history)
        val rlRepairType = findViewById<RelativeLayout>(R.id.rl_repair_type)
        val btnSelectImage = findViewById<CardView>(R.id.btn_select_image)

        btnBack.setOnClickListener { finish() }

        // เชื่อมปุ่มประวัติไปยังหน้า RepairHistoryActivity
        btnHistory.setOnClickListener {
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

        btnSubmit.setOnClickListener {
            val finalType = if (selectedRepairType == "อื่น ๆ") etOtherType.text.toString().trim() else selectedRepairType
            val description = etDescription.text.toString().trim()

            if (finalType.isEmpty()) {
                Toast.makeText(this, "กรุณาระบุเรื่องที่ต้องการแจ้งซ่อม", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรายละเอียด", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRepairRequest(finalType, description)
        }
    }

    private fun toggleDropdown() {
        isDropdownOpen = !isDropdownOpen
        layoutDropdown.visibility = if (isDropdownOpen) View.VISIBLE else View.GONE
        ivDropdownArrow.setImageResource(if (isDropdownOpen) R.drawable.ic_chevron_up_gg else R.drawable.ic_chevron_down_gg)
    }

    private fun selectType(type: String) {
        selectedRepairType = type
        tvRepairType.text = type
        tvRepairType.setTextColor(resources.getColor(android.R.color.black, null))
        
        // ถ้าเลือก "อื่น ๆ" ให้แสดงช่องกรอกข้อมูล
        cvOtherType.visibility = if (type == "อื่น ๆ") View.VISIBLE else View.GONE
        
        isDropdownOpen = false
        layoutDropdown.visibility = View.GONE
        ivDropdownArrow.setImageResource(R.drawable.ic_chevron_down_gg)
    }

    private fun submitRepairRequest(repairType: String, description: String) {
        val userId = auth.currentUser?.uid ?: return

        // ดึงข้อมูลผู้ใช้เพื่อส่งแจ้งเตือนให้แอดมิน
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val userName = userDoc.getString("name") ?: "ไม่ระบุชื่อ"
            val roomNumber = userDoc.getString("roomNumber") ?: "ไม่ระบุห้อง"

            val requestId = UUID.randomUUID().toString()
            val repairRequest = RepairRequest(
                requestId = requestId,
                userId = userId,
                repairType = repairType,
                description = description,
                imageUrl = selectedImageUri?.toString() ?: "",
                status = "pending",
                timestamp = System.currentTimeMillis()
            )

            db.collection("repair_requests").document(requestId).set(repairRequest)
                .addOnSuccessListener {
                    // ส่ง Notification ไปหา Admin
                    sendNotificationToAdmin(userName, roomNumber, repairType)
                    
                    Toast.makeText(this, "แจ้งซ่อมเรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendNotificationToAdmin(userName: String, roomNumber: String, type: String) {
        val notifId = db.collection("notifications").document().id
        val notification = hashMapOf(
            "notificationId" to notifId,
            "userId" to "admin", // ส่งให้กลุ่ม admin
            "title" to "แจ้งซ่อมใหม่: ห้อง $roomNumber",
            "message" to "คุณ $userName ได้แจ้งซ่อมเรื่อง $type",
            "senderName" to userName,
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )
        db.collection("notifications").document(notifId).set(notification)
    }
}
