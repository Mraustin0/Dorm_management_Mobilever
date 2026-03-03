package com.example.horganized.admin

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.horganized.R
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AdminAddAnnouncementActivity : AppCompatActivity() {

    private val IMGBB_API_KEY = "106d8c7ed1b4b416772d2a91f3327a5b"
    private val db = FirebaseFirestore.getInstance()

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var frameImagePreview: FrameLayout
    private lateinit var ivPreview: ImageView
    private lateinit var layoutPickHint: LinearLayout
    private lateinit var progressImageUpload: ProgressBar
    private lateinit var btnSend: AppCompatButton

    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivPreview.visibility = View.VISIBLE
            layoutPickHint.visibility = View.GONE
            Glide.with(this).load(uri).centerCrop().into(ivPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_add_announcement)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etTitle = findViewById(R.id.et_announcement_title)
        etContent = findViewById(R.id.et_announcement_content)
        frameImagePreview = findViewById(R.id.frame_image_preview)
        ivPreview = findViewById(R.id.iv_announcement_preview)
        layoutPickHint = findViewById(R.id.layout_pick_hint)
        progressImageUpload = findViewById(R.id.progress_image_upload)
        btnSend = findViewById(R.id.btn_send_announcement)

        findViewById<ImageView>(R.id.btn_back_add_announce).setOnClickListener { finish() }

        frameImagePreview.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnSend.isEnabled = false
            if (selectedImageUri != null) {
                uploadImageAndSend(title, content)
            } else {
                sendAnnouncement(title, content, "")
            }
        }
    }

    private fun uploadImageAndSend(title: String, content: String) {
        progressImageUpload.visibility = View.VISIBLE
        layoutPickHint.visibility = View.GONE
        btnSend.text = "กำลังอัปโหลดรูป..."

        val bytes = contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        if (bytes == null) {
            progressImageUpload.visibility = View.GONE
            Toast.makeText(this, "อ่านรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            btnSend.isEnabled = true
            btnSend.text = "ส่งประกาศ"
            return
        }
        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressImageUpload.visibility = View.GONE
                    Toast.makeText(this@AdminAddAnnouncementActivity,
                        "อัปโหลดรูปไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSend.isEnabled = true
                    btnSend.text = "ส่งประกาศ"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                runOnUiThread {
                    progressImageUpload.visibility = View.GONE
                    try {
                        val json = JSONObject(body)
                        if (json.optBoolean("success", false)) {
                            val url = json.getJSONObject("data").getString("url")
                            sendAnnouncement(title, content, url)
                        } else {
                            val errMsg = json.optJSONObject("error")?.optString("message")
                                ?: "อัปโหลดรูปไม่สำเร็จ (${response.code})"
                            Toast.makeText(this@AdminAddAnnouncementActivity, errMsg, Toast.LENGTH_LONG).show()
                            btnSend.isEnabled = true
                            btnSend.text = "ส่งประกาศ"
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminAddAnnouncementActivity,
                            "Parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                        btnSend.isEnabled = true
                        btnSend.text = "ส่งประกาศ"
                    }
                }
            }
        })
    }

    private fun sendAnnouncement(title: String, content: String, imageUrl: String) {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("th", "TH"))
        val currentDate = sdf.format(Date())
        val timestamp = System.currentTimeMillis()

        val announcementData = hashMapOf(
            "title" to title,
            "detail" to content,
            "date" to currentDate,
            "timestamp" to timestamp,
            "imageUrl" to imageUrl
        )

        db.collection("announcements")
            .add(announcementData)
            .addOnSuccessListener {
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
                btnSend.isEnabled = true
                btnSend.text = "ส่งประกาศ"
            }
    }
}
