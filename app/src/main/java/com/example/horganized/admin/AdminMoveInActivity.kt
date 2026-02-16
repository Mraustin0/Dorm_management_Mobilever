package com.example.horganized.admin

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminMoveInActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etWater: EditText
    private lateinit var etElectric: EditText
    private lateinit var spinnerContract: Spinner

    private var roomName = ""
    private var roomNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_move_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        roomName = intent.getStringExtra("ROOM_NAME") ?: "ห้อง xxx"
        roomNumber = roomName.replace("ห้อง ", "").trim()

        findViewById<TextView>(R.id.tv_room_title_move_in).text = roomName

        etName = findViewById(R.id.et_move_in_name)
        etSurname = findViewById(R.id.et_move_in_surname)
        etPhone = findViewById(R.id.et_move_in_phone)
        etEmail = findViewById(R.id.et_move_in_email)
        etWater = findViewById(R.id.et_move_in_water)
        etElectric = findViewById(R.id.et_move_in_electric)
        spinnerContract = findViewById(R.id.spinner_contract_term)

        setupContractSpinner()

        findViewById<ImageView>(R.id.btn_back_move_in).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_save_move_in).setOnClickListener {
            if (validateInputs()) {
                showSaveConfirmationDialog()
            }
        }
    }

    private fun setupContractSpinner() {
        val terms = arrayOf("3 เดือน", "6 เดือน", "12 เดือน")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerContract.adapter = adapter
    }

    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val water = etWater.text.toString().trim()
        val electric = etElectric.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "กรุณากรอกชื่อ"
            etName.requestFocus()
            return false
        }
        if (surname.isEmpty()) {
            etSurname.error = "กรุณากรอกนามสกุล"
            etSurname.requestFocus()
            return false
        }
        if (phone.isEmpty()) {
            etPhone.error = "กรุณากรอกเบอร์โทร"
            etPhone.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            etEmail.error = "กรุณากรอกอีเมล"
            etEmail.requestFocus()
            return false
        }
        if (water.isEmpty()) {
            etWater.error = "กรุณากรอกเลขมิเตอร์น้ำ"
            etWater.requestFocus()
            return false
        }
        if (electric.isEmpty()) {
            etElectric.error = "กรุณากรอกเลขมิเตอร์ไฟ"
            etElectric.requestFocus()
            return false
        }
        return true
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลการย้ายเข้าใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                saveTenantToFirestore()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun saveTenantToFirestore() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val waterMeter = etWater.text.toString().trim().toIntOrNull() ?: 0
        val electricMeter = etElectric.text.toString().trim().toIntOrNull() ?: 0
        val contractTerm = spinnerContract.selectedItem.toString()

        // รหัสผ่านเริ่มต้น = เบอร์โทร (ผู้เช่าเปลี่ยนทีหลังได้)
        val defaultPassword = phone

        // จำ admin ที่กำลัง login อยู่ เพราะ createUser จะเปลี่ยน currentUser
        val adminUser = auth.currentUser

        // 1) สร้าง Firebase Auth account ให้ผู้เช่า
        auth.createUserWithEmailAndPassword(email, defaultPassword)
            .addOnSuccessListener { result ->
                val newUserId = result.user?.uid ?: return@addOnSuccessListener

                // 2) บันทึกข้อมูลผู้เช่าใน Firestore โดยใช้ UID เป็น document ID
                val userData = hashMapOf(
                    "name" to name,
                    "surname" to surname,
                    "phone" to phone,
                    "email" to email,
                    "role" to "user",
                    "roomNumber" to roomNumber,
                    "contractTerm" to contractTerm,
                    "waterMeter" to waterMeter,
                    "electricMeter" to electricMeter,
                    "moveInDate" to Timestamp.now(),
                    "profileImage" to ""
                )

                db.collection("users").document(newUserId)
                    .set(userData)
                    .addOnSuccessListener {
                        // 3) อัปเดตสถานะห้อง
                        db.collection("rooms").document(roomNumber)
                            .update(
                                "isVacant", false,
                                "tenantId", newUserId
                            )
                            .addOnSuccessListener {
                                // 4) กลับไป login เป็น admin เหมือนเดิม
                                if (adminUser != null) {
                                    auth.updateCurrentUser(adminUser)
                                }
                                Toast.makeText(this, "บันทึกข้อมูลย้ายเข้าเรียบร้อย\nรหัสผ่านเริ่มต้น: $defaultPassword", Toast.LENGTH_LONG).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("MoveIn", "Error updating room", e)
                                Toast.makeText(this, "บันทึกผู้เช่าแล้ว แต่อัปเดตห้องไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MoveIn", "Error saving user data", e)
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MoveIn", "Error creating auth account", e)
                val msg = when {
                    e.message?.contains("email address is already in use") == true ->
                        "อีเมลนี้ถูกใช้งานแล้ว"
                    e.message?.contains("badly formatted") == true ->
                        "รูปแบบอีเมลไม่ถูกต้อง"
                    e.message?.contains("at least 6 characters") == true ->
                        "เบอร์โทร (รหัสผ่านเริ่มต้น) ต้องมีอย่างน้อย 6 ตัว"
                    else -> "สร้างบัญชีไม่สำเร็จ: ${e.message}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
    }
}
