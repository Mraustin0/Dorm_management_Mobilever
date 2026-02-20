package com.example.horganized.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileEditActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivProfile: ImageView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etRoomNumber: EditText

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            ivProfile.setImageURI(uri)
            // บันทึก URI ลง Firestore เป็น string
            val uid = auth.currentUser?.uid ?: return@registerForActivityResult
            db.collection("users").document(uid).update("profileImage", uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_edit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ivProfile = findViewById(R.id.profile_image_edit)
        etFirstName = findViewById(R.id.et_first_name)
        etLastName = findViewById(R.id.et_last_name)
        etPhone = findViewById(R.id.et_phone)
        etEmail = findViewById(R.id.et_email)
        etRoomNumber = findViewById(R.id.et_room_number)

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // เลือกรูปโปรไฟล์
        findViewById<TextView>(R.id.btn_edit_picture).setOnClickListener {
            openImagePicker()
        }
        ivProfile.setOnClickListener {
            openImagePicker()
        }

        // โหลดข้อมูลจาก Firestore
        loadUserData()

        // ปุ่ม Submit
        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            saveUserData()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImage.launch(intent)
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etFirstName.setText(document.getString("name") ?: "")
                    etLastName.setText(document.getString("surname") ?: "")
                    etPhone.setText(document.getString("phone") ?: "")
                    etEmail.setText(document.getString("email") ?: "")
                    etRoomNumber.setText(document.getString("roomNumber") ?: "")

                    val imageUri = document.getString("profileImage")
                    if (!imageUri.isNullOrEmpty()) {
                        try {
                            ivProfile.setImageURI(Uri.parse(imageUri))
                        } catch (_: Exception) { }
                    }
                }
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (firstName.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกชื่อ", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf<String, Any>(
            "name" to firstName,
            "surname" to lastName,
            "phone" to phone,
            "email" to email
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "บันทึกข้อมูลเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "บันทึกไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
