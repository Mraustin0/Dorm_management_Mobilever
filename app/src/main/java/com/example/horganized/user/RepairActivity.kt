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

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnSend = findViewById<TextView>(R.id.btn_send_repair)
        val rlRepairType = findViewById<RelativeLayout>(R.id.rl_repair_type)
        val btnSelectImage = findViewById<CardView>(R.id.btn_select_image)
        val etDescription = findViewById<EditText>(R.id.et_description)

        btnBack.setOnClickListener { finish() }

        // Toggle dropdown
        rlRepairType.setOnClickListener {
            toggleDropdown()
        }

        // ตัวเลือก dropdown
        findViewById<TextView>(R.id.option_electric).setOnClickListener { selectType("ไฟฟ้า") }
        findViewById<TextView>(R.id.option_water).setOnClickListener { selectType("ประปา") }
        findViewById<TextView>(R.id.option_internet).setOnClickListener { selectType("อินเทอร์เน็ต") }
        findViewById<TextView>(R.id.option_other).setOnClickListener { selectType("อื่น ๆ") }

        // Image Picker
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        // Submit
        btnSend.setOnClickListener {
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
        // ปิด dropdown
        isDropdownOpen = false
        layoutDropdown.visibility = View.GONE
        ivDropdownArrow.setImageResource(R.drawable.ic_chevron_down_gg)
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
