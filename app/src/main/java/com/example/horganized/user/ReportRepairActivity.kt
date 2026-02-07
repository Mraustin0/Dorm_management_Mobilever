package com.example.horganized.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private lateinit var ivPreview: ImageView
    private lateinit var tvUploadLabel: TextView
    private lateinit var tvRepairType: TextView

    private val repairTypes = arrayOf(
        "ไฟฟ้า / หลอดไฟ",
        "ประปา / ท่อน้ำ",
        "แอร์ / เครื่องปรับอากาศ",
        "ประตู / หน้าต่าง",
        "เฟอร์นิเจอร์",
        "อินเทอร์เน็ต",
        "อื่นๆ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_repair)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSubmit = findViewById<TextView>(R.id.btn_submit)
        val rlRepairType = findViewById<RelativeLayout>(R.id.rl_repair_type)
        val btnSelectImage = findViewById<CardView>(R.id.btn_select_image)
        val etDescription = findViewById<EditText>(R.id.et_description)

        tvRepairType = findViewById(R.id.tv_repair_type)
        ivPreview = findViewById(R.id.iv_preview)
        tvUploadLabel = findViewById(R.id.tv_upload_label)

        btnBack.setOnClickListener { finish() }

        // Repair Type Dropdown
        rlRepairType.setOnClickListener {
            showRepairTypeDialog()
        }

        // Image Picker
        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                ivPreview.setImageURI(selectedImageUri)
                ivPreview.layoutParams.width = resources.getDimensionPixelSize(android.R.dimen.thumbnail_width)
                ivPreview.layoutParams.height = resources.getDimensionPixelSize(android.R.dimen.thumbnail_height)
                tvUploadLabel.text = "เปลี่ยนรูปภาพ"
            }
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        // Submit
        btnSubmit.setOnClickListener {
            val description = etDescription.text.toString().trim()

            if (selectedRepairType.isEmpty()) {
                Toast.makeText(this, "กรุณาเลือกประเภทการแจ้งซ่อม", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรายละเอียด", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitRepairRequest(description)
        }
    }

    private fun showRepairTypeDialog() {
        AlertDialog.Builder(this)
            .setTitle("เลือกประเภทการแจ้งซ่อม")
            .setItems(repairTypes) { _, which ->
                selectedRepairType = repairTypes[which]
                tvRepairType.text = selectedRepairType
                tvRepairType.setTextColor(resources.getColor(android.R.color.black, null))
            }
            .show()
    }

    private fun submitRepairRequest(description: String) {
        val userId = auth.currentUser?.uid ?: return

        val requestId = UUID.randomUUID().toString()
        val repairRequest = RepairRequest(
            requestId = requestId,
            userId = userId,
            repairType = selectedRepairType,
            description = description,
            imageUrl = selectedImageUri?.toString() ?: "",
            status = "pending",
            timestamp = System.currentTimeMillis()
        )

        db.collection("repair_requests")
            .document(requestId)
            .set(repairRequest)
            .addOnSuccessListener {
                Toast.makeText(this, "แจ้งซ่อมเรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
