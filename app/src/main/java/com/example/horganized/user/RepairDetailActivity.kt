package com.example.horganized.user

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.horganized.R
import com.example.horganized.model.RepairRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RepairDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_detail)

        db = FirebaseFirestore.getInstance()

        val repairId = intent.getStringExtra("repair_id") ?: ""
        
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        if (repairId.isNotEmpty()) {
            fetchRepairDetail(repairId)
        }
    }

    private fun fetchRepairDetail(repairId: String) {
        db.collection("repair_requests").document(repairId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val repair = document.toObject(RepairRequest::class.java)
                    if (repair != null) {
                        displayDetail(repair)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "ไม่สามารถโหลดข้อมูลได้", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayDetail(repair: RepairRequest) {
        val tvStatus = findViewById<TextView>(R.id.tv_detail_status)
        val tvType = findViewById<TextView>(R.id.tv_detail_type)
        val tvDesc = findViewById<TextView>(R.id.tv_detail_description)
        val tvDate = findViewById<TextView>(R.id.tv_detail_date)
        val ivImage = findViewById<ImageView>(R.id.iv_detail_image)
        val cvImage = findViewById<CardView>(R.id.cv_detail_image)
        val tvImageLabel = findViewById<TextView>(R.id.tv_image_label)

        tvType.text = repair.repairType
        tvDesc.text = repair.description
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm น.", Locale("th", "TH"))
        tvDate.text = sdf.format(Date(repair.timestamp))

        // จัดการสถานะ
        when (repair.status) {
            "pending" -> {
                tvStatus.text = "รอดำเนินการ"
                tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
            }
            "in_progress" -> {
                tvStatus.text = "กำลังดำเนินการ"
                tvStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
            "completed" -> {
                tvStatus.text = "ซ่อมเสร็จสิ้น"
                tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
        }

        // จัดการรูปภาพ
        if (repair.imageUrl.isNotEmpty()) {
            cvImage.visibility = View.VISIBLE
            tvImageLabel.visibility = View.VISIBLE
            Glide.with(this).load(repair.imageUrl).into(ivImage)
        } else {
            cvImage.visibility = View.GONE
            tvImageLabel.visibility = View.GONE
        }
    }
}
