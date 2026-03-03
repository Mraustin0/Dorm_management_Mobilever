package com.example.horganized.user

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class UserProfileEditActivity : AppCompatActivity() {

    private val IMGBB_API_KEY = "106d8c7ed1b4b416772d2a91f3327a5b"

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivProfile  : ImageView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName : EditText
    private lateinit var etPhone    : EditText
    private lateinit var etEmail    : EditText
    private lateinit var etRoomNumber: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSubmit  : Button

    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this).load(uri).transform(CircleCrop()).into(ivProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_edit)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        ivProfile    = findViewById(R.id.profile_image_edit)
        etFirstName  = findViewById(R.id.et_first_name)
        etLastName   = findViewById(R.id.et_last_name)
        etPhone      = findViewById(R.id.et_phone)
        etEmail      = findViewById(R.id.et_email)
        etRoomNumber = findViewById(R.id.et_room_number)
        progressBar  = findViewById(R.id.progress_upload)
        btnSubmit    = findViewById(R.id.btn_submit)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btn_edit_picture).setOnClickListener { openGallery() }
        ivProfile.setOnClickListener { openGallery() }

        loadUserData()

        btnSubmit.setOnClickListener { saveUserData() }
    }

    private fun openGallery() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etFirstName.setText(doc.getString("name") ?: "")
                    etLastName.setText(doc.getString("surname") ?: "")
                    etPhone.setText(doc.getString("phone") ?: "")
                    etEmail.setText(doc.getString("email") ?: "")
                    etRoomNumber.setText(doc.getString("roomNumber") ?: "")

                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.u1)
                            .into(ivProfile)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดข้อมูลไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show()
            return
        }
        val firstName = etFirstName.text.toString().trim()
        if (firstName.isEmpty()) {
            etFirstName.error = "กรุณากรอกชื่อ"
            etFirstName.requestFocus()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "กำลังบันทึก..."

        if (selectedImageUri != null) {
            uploadToImgbb(uid)
        } else {
            updateFirestore(uid, null)
        }
    }

    private fun uploadToImgbb(uid: String) {
        progressBar.visibility = View.VISIBLE
        btnSubmit.text = "กำลังอัปโหลดรูป..."

        val bytes = contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        if (bytes == null) {
            progressBar.visibility = View.GONE
            resetButton()
            Toast.makeText(this, "อ่านรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            return
        }
        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

        // ใช้ MultipartBody เหมือน admin version
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", base64Image)
            .addFormDataPart("name", "user_$uid")
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    resetButton()
                    Toast.makeText(this@UserProfileEditActivity,
                        "อัพโหลดรูปไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        val json = JSONObject(body)
                        val success = json.optBoolean("success", false)
                        if (success) {
                            val url = json.getJSONObject("data").getString("url")
                            updateFirestore(uid, url)
                        } else {
                            // แสดง error จาก imgbb จริงๆ
                            val errMsg = json.optJSONObject("error")?.optString("message")
                                ?: json.optString("error")
                                ?: "อัพโหลดรูปไม่สำเร็จ (${response.code})"
                            resetButton()
                            Toast.makeText(this@UserProfileEditActivity, errMsg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        resetButton()
                        Toast.makeText(this@UserProfileEditActivity,
                            "parse error: $body", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun updateFirestore(uid: String, photoUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "name"    to etFirstName.text.toString().trim(),
            "surname" to etLastName.text.toString().trim(),
            "phone"   to etPhone.text.toString().trim(),
            "email"   to etEmail.text.toString().trim()
        )
        if (photoUrl != null) updates["photoUrl"] = photoUrl

        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว ✓", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                resetButton()
                Toast.makeText(this, "บันทึกไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetButton() {
        btnSubmit.isEnabled = true
        btnSubmit.text = "Submit"
    }
}
