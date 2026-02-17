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
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminMoveInActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContractLink: EditText
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
        etContractLink = findViewById(R.id.et_contract_link)
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
        if (etName.text.toString().trim().isEmpty()) { etName.error = "กรุณากรอกชื่อ"; return false }
        if (etSurname.text.toString().trim().isEmpty()) { etSurname.error = "กรุณากรอกนามสกุล"; return false }
        if (etPhone.text.toString().trim().isEmpty()) { etPhone.error = "กรุณากรอกเบอร์โทร"; return false }
        if (etEmail.text.toString().trim().isEmpty()) { etEmail.error = "กรุณากรอกอีเมล"; return false }
        if (etContractLink.text.toString().trim().isEmpty()) { etContractLink.error = "กรุณาใส่ลิงก์สัญญา"; return false }
        if (etWater.text.toString().trim().isEmpty()) { etWater.error = "กรุณากรอกเลขมิเตอร์น้ำ"; return false }
        if (etElectric.text.toString().trim().isEmpty()) { etElectric.error = "กรุณากรอกเลขมิเตอร์ไฟ"; return false }
        return true
    }

    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการบันทึก")
            .setMessage("คุณต้องการบันทึกข้อมูลการย้ายเข้าใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ -> saveTenantToFirestore() }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun saveTenantToFirestore() {
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val contractUrl = etContractLink.text.toString().trim()

        val secondaryApp = try {
            FirebaseApp.initializeApp(this, FirebaseApp.getInstance().options, "secondaryAuth")
        } catch (e: IllegalStateException) {
            FirebaseApp.getInstance("secondaryAuth")
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        secondaryAuth.createUserWithEmailAndPassword(email, phone)
            .addOnSuccessListener { result ->
                val newUserId = result.user?.uid ?: return@addOnSuccessListener
                secondaryAuth.signOut()

                val userData = hashMapOf(
                    "name" to etName.text.toString().trim(),
                    "surname" to etSurname.text.toString().trim(),
                    "phone" to phone,
                    "email" to email,
                    "contractUrl" to contractUrl, // บันทึกลิงก์สัญญา
                    "role" to "user",
                    "roomNumber" to roomNumber,
                    "contractTerm" to spinnerContract.selectedItem.toString(),
                    "waterMeter" to etWater.text.toString().trim().toIntOrNull(),
                    "electricMeter" to etElectric.text.toString().trim().toIntOrNull(),
                    "moveInDate" to Timestamp.now()
                )

                db.collection("users").document(newUserId).set(userData)
                    .addOnSuccessListener {
                        db.collection("rooms").document(roomNumber)
                            .update("isVacant", false, "tenantId", newUserId)
                            .addOnSuccessListener {
                                Toast.makeText(this, "บันทึกเรียบร้อย", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
            }
            .addOnFailureListener { Toast.makeText(this, "ผิดพลาด: ${it.message}", Toast.LENGTH_SHORT).show() }
    }
}