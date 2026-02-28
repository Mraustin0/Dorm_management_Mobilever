package com.example.horganized.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AdminEditProfileActivity : AppCompatActivity() {

    // ใส่ imgbb API key ของตัวเอง (สมัครฟรีที่ https://api.imgbb.com/)
    private val IMGBB_API_KEY = "5d5ae17ff9575ef5b3f0b8e82f45fd64"

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private lateinit var ivProfilePic  : ImageView
    private lateinit var btnPickImage  : FrameLayout
    private lateinit var progressUpload: ProgressBar
    private lateinit var etUsername    : EditText
    private lateinit var etFirstName   : EditText
    private lateinit var etLastName    : EditText
    private lateinit var etEmail       : EditText
    private lateinit var etTel         : EditText
    private lateinit var btnSubmit     : AppCompatButton

    private var selectedImageUri: Uri? = null

    // Launcher เลือกรูปจาก Gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .transform(CircleCrop())
                .into(ivProfilePic)
            ivProfilePic.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivProfilePic   = findViewById(R.id.iv_edit_profile_pic)
        btnPickImage   = findViewById(R.id.btn_pick_image)
        progressUpload = findViewById(R.id.progress_upload)
        etUsername     = findViewById(R.id.et_edit_username)
        etFirstName    = findViewById(R.id.et_edit_first_name)
        etLastName     = findViewById(R.id.et_edit_last_name)
        etEmail        = findViewById(R.id.et_edit_email)
        etTel          = findViewById(R.id.et_edit_tel)
        btnSubmit      = findViewById(R.id.btn_submit_profile)

        findViewById<ImageView>(R.id.btn_back_edit_profile).setOnClickListener { finish() }

        ivProfilePic.setOnClickListener { openGallery() }
        btnPickImage.setOnClickListener { openGallery() }
        btnSubmit.setOnClickListener { saveProfile() }

        loadProfile()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickImageLauncher.launch(intent)
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        btnSubmit.isEnabled = false

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etUsername.setText(doc.getString("username") ?: "")
                    etFirstName.setText(doc.getString("firstName") ?: doc.getString("name") ?: "")
                    etLastName.setText(doc.getString("lastName") ?: "")
                    etEmail.setText(doc.getString("email") ?: auth.currentUser?.email ?: "")
                    etTel.setText(doc.getString("tel") ?: doc.getString("phone") ?: "")

                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        ivProfilePic.setPadding(0, 0, 0, 0)
                        Glide.with(this)
                            .load(photoUrl)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivProfilePic)
                    }
                } else {
                    etEmail.setText(auth.currentUser?.email ?: "")
                }
                btnSubmit.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดข้อมูลไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "กรุณา login ก่อน", Toast.LENGTH_SHORT).show()
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
            uploadToImgbb(uid, firstName)
        } else {
            saveToFirestore(uid, firstName, null)
        }
    }

    // ── อัปโหลดรูปไปยัง imgbb ──────────────────────────────────────────────
    private fun uploadToImgbb(uid: String, firstName: String) {
        progressUpload.visibility = View.VISIBLE
        btnSubmit.text = "กำลังอัปโหลดรูป..."

        // อ่านไฟล์รูปแล้วแปลงเป็น Base64
        val bytes = contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
        if (bytes == null) {
            progressUpload.visibility = View.GONE
            Toast.makeText(this, "อ่านรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            btnSubmit.isEnabled = true
            btnSubmit.text = "บันทึก"
            return
        }
        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

        // สร้าง multipart request ส่งไป imgbb
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", base64Image)
            .addFormDataPart("name", "profile_$uid")
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressUpload.visibility = View.GONE
                    Toast.makeText(this@AdminEditProfileActivity,
                        "อัปโหลดรูปไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "บันทึก"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                runOnUiThread {
                    progressUpload.visibility = View.GONE
                    try {
                        val json = JSONObject(body)
                        if (json.getBoolean("success")) {
                            val url = json.getJSONObject("data").getString("url")
                            saveToFirestore(uid, firstName, url)
                        } else {
                            Toast.makeText(this@AdminEditProfileActivity,
                                "อัปโหลดรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                            btnSubmit.isEnabled = true
                            btnSubmit.text = "บันทึก"
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminEditProfileActivity,
                            "parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "บันทึก"
                    }
                }
            }
        })
    }

    // ── บันทึกลง Firestore ─────────────────────────────────────────────────
    private fun saveToFirestore(uid: String, firstName: String, photoUrl: String?) {
        val data = mutableMapOf<String, Any>(
            "username"  to etUsername.text.toString().trim(),
            "firstName" to firstName,
            "lastName"  to etLastName.text.toString().trim(),
            "name"      to "$firstName ${etLastName.text.toString().trim()}".trim(),
            "email"     to etEmail.text.toString().trim(),
            "tel"       to etTel.text.toString().trim(),
            "phone"     to etTel.text.toString().trim()
        )
        if (photoUrl != null) data["photoUrl"] = photoUrl

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว ✓", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "บันทึกไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "บันทึก"
            }
    }
}
