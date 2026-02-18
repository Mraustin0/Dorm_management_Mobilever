package com.example.horganized.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminAddAnnouncementActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_add_announcement)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etTitle = findViewById<EditText>(R.id.et_announcement_title)
        val etContent = findViewById<EditText>(R.id.et_announcement_content)

        findViewById<ImageView>(R.id.btn_back_add_announce).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_send_announcement).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendAnnouncement(title, content)
        }
    }

    private fun sendAnnouncement(title: String, content: String) {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("th", "TH"))
        val currentDate = sdf.format(Date())
        val timestamp = System.currentTimeMillis()

        val announcementData = hashMapOf(
            "title" to title,
            "detail" to content,
            "date" to currentDate,
            "timestamp" to timestamp,
            "imageUrl" to "" // เผื่อใส่รูปในอนาคต
        )

        db.collection("announcements")
            .add(announcementData)
            .addOnSuccessListener {
                // สร้างการแจ้งเตือนส่งให้ User ทุกคน (userId = "all")
                val notifId = db.collection("notifications").document().id
                val notification = hashMapOf(
                    "notificationId" to notifId,
                    "userId" to "all",
                    "title" to "ประกาศใหม่: $title",
                    "message" to content,
                    "senderName" to "ADMIN1",
                    "timestamp" to timestamp,
                    "isRead" to false
                )
                db.collection("notifications").document(notifId).set(notification)

                Toast.makeText(this, "ส่งประกาศเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}