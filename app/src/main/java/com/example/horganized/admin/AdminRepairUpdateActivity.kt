package com.example.horganized.admin

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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AdminRepairUpdateActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var requestId: String = ""
    private var userId: String = ""
    private var roomNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_repair_update)

        requestId = intent.getStringExtra("repair_id") ?: ""

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        if (requestId.isNotEmpty()) {
            fetchRepairDetail()
        }

        findViewById<View>(R.id.btn_status_in_progress).setOnClickListener {
            updateStatus("in_progress", "แอดมินรับเรื่องแล้ว กำลังดำเนินการซ่อม")
        }

        findViewById<View>(R.id.btn_status_completed).setOnClickListener {
            updateStatus("completed", "ดำเนินการซ่อมเสร็จสิ้นเรียบร้อยแล้ว")
        }
    }

    private fun fetchRepairDetail() {
        db.collection("repair_requests").document(requestId).get()
            .addOnSuccessListener { document ->
                val repair = document.toObject(RepairRequest::class.java)
                if (repair != null) {
                    userId = repair.userId
                    roomNumber = repair.roomNumber
                    
                    findViewById<TextView>(R.id.tv_update_room).text = "ห้อง ${repair.roomNumber}"
                    findViewById<TextView>(R.id.tv_update_user).text = "ผู้แจ้ง: ${repair.userName}"
                    findViewById<TextView>(R.id.tv_update_type).text = repair.repairType
                    findViewById<TextView>(R.id.tv_update_description).text = repair.description

                    val ivImage = findViewById<ImageView>(R.id.iv_update_image)
                    val cvImage = findViewById<CardView>(R.id.cv_update_image)
                    val tvLabel = findViewById<TextView>(R.id.tv_image_label)

                    if (repair.imageUrl.isNotEmpty()) {
                        cvImage.visibility = View.VISIBLE
                        tvLabel.visibility = View.VISIBLE
                        Glide.with(this).load(repair.imageUrl).into(ivImage)
                    } else {
                        cvImage.visibility = View.GONE
                        tvLabel.visibility = View.GONE
                    }
                }
            }
    }

    private fun updateStatus(newStatus: String, message: String) {
        db.collection("repair_requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener {
                sendNotificationToUser(newStatus, message)   // ส่ง noti พร้อม type
                Toast.makeText(this, "อัปเดตสถานะเรียบร้อย", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "ผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotificationToUser(statusKey: String, message: String) {
        if (userId.isEmpty()) return

        val title = when (statusKey) {
            "in_progress" -> "รับเรื่องแจ้งซ่อมแล้ว"
            "completed"   -> "ซ่อมเสร็จเรียบร้อยแล้ว"
            else          -> "อัปเดตการแจ้งซ่อม"
        }

        val notification = hashMapOf(
            "userId"             to userId,
            "title"              to "$title: ห้อง $roomNumber",
            "message"            to message,
            "type"               to "repair_update",
            "senderName"         to "แอดมิน",
            "timestamp"          to System.currentTimeMillis(),
            "firestoreTimestamp" to Timestamp.now(),
            "isRead"             to false
        )
        db.collection("notifications").add(notification)
    }
}
