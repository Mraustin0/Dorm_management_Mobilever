package com.example.horganized.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.horganized.R
import com.example.horganized.model.Bill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PayBillActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private lateinit var tvUploadLabel: TextView
    private var billId: String = ""
    private var billAmount: Double = 0.0
    private var billMonth: String = ""

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).into(ivPreview)
            val params = ivPreview.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            ivPreview.layoutParams = params
            ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            tvUploadLabel.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill)

        ivPreview = findViewById(R.id.iv_preview)
        tvUploadLabel = findViewById(R.id.tv_upload_label)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnCopy = findViewById<ImageView>(R.id.btn_copy_acc)
        val btnSelectImage = findViewById<androidx.cardview.widget.CardView>(R.id.btn_select_image)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val tvAmount = findViewById<TextView>(R.id.tv_amount)

        btnBack.setOnClickListener { finish() }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Account Number", "001-443-5771")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "คัดลอกเลขบัญชีแล้ว", Toast.LENGTH_SHORT).show()
        }

        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnNext.setOnClickListener {
            if (billId.isEmpty()) {
                Toast.makeText(this, "ไม่พบข้อมูลบิล กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageUri != null) {
                val intent = Intent(this, PayBillConfirmActivity::class.java)
                intent.putExtra("BILL_ID", billId)
                intent.putExtra("IMAGE_URI", selectedImageUri.toString())
                intent.putExtra("BILL_AMOUNT", billAmount)
                intent.putExtra("BILL_MONTH", billMonth)
                startActivity(intent)
            } else {
                Toast.makeText(this, "กรุณาอัพโหลดหลักฐานการชำระเงิน", Toast.LENGTH_SHORT).show()
            }
        }

        // ดึงข้อมูลบิลล่าสุดมาแสดงโดยอัตโนมัติ
        fetchLatestBill(tvAmount)
    }

    private fun fetchLatestBill(tvAmount: TextView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("bills")
            .whereEqualTo("userId", uid)
            .orderBy("dueDate", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val doc = snapshots.documents[0]
                    val bill = doc.toObject(Bill::class.java)
                    if (bill != null) {
                        billId = doc.id
                        billAmount = bill.amount
                        billMonth = bill.month
                        tvAmount.text = "${String.format("%,.2f", billAmount)} บาท"
                    }
                } else {
                    tvAmount.text = "0.00 บาท"
                    Toast.makeText(this, "ไม่พบรายการบิลค้างชำระ", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "โหลดข้อมูลบิลล้มเหลว: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
