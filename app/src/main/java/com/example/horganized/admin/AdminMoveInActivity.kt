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
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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

    companion object {
        private const val FIREBASE_API_KEY = "AIzaSyD5thTDTjkwhhb5tyfhrzQoXqeoOFg8Wcs"
    }

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

    /**
     * สร้าง Firebase Auth account ผ่าน REST API
     * ไม่กระทบ admin session เลย เพราะเป็นแค่ HTTP request
     */
    private fun createAuthAccountViaRest(
        email: String,
        password: String,
        onSuccess: (uid: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        Thread {
            try {
                val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$FIREBASE_API_KEY")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("returnSecureToken", false)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(body.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    val uid = json.getString("localId")
                    runOnUiThread { onSuccess(uid) }
                } else {
                    val errorResponse = conn.errorStream.bufferedReader().readText()
                    val errorJson = JSONObject(errorResponse)
                    val errorMessage = errorJson
                        .getJSONObject("error")
                        .getString("message")
                    runOnUiThread { onFailure(errorMessage) }
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("MoveIn", "REST API error", e)
                runOnUiThread { onFailure(e.message ?: "Unknown error") }
            }
        }.start()
    }

    private fun saveTenantToFirestore() {
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val contractUrl = etContractLink.text.toString().trim()

        val defaultPassword = phone

        // 1) สร้าง Auth account ผ่าน REST API (ไม่กระทบ admin session)
        createAuthAccountViaRest(
            email = email,
            password = defaultPassword,
            onSuccess = { newUserId ->
                // 2) บันทึกข้อมูลผู้เช่าใน Firestore (admin ยังอยู่ครบ)
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

                // ใช้ batch write เพื่อให้ทั้ง user data + room update สำเร็จพร้อมกัน
                val batch = db.batch()

                val userRef = db.collection("users").document(newUserId)
                batch.set(userRef, userData)

                val roomRef = db.collection("rooms").document(roomNumber)
                batch.update(roomRef, mapOf(
                    "isVacant" to false,
                    "tenantId" to newUserId
                ))

                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "บันทึกข้อมูลย้ายเข้าเรียบร้อย\nรหัสผ่านเริ่มต้น: $defaultPassword",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("MoveIn", "Error saving to Firestore", e)
                        Toast.makeText(
                            this,
                            "เกิดข้อผิดพลาดบันทึก Firestore: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            onFailure = { errorMessage ->
                Log.e("MoveIn", "Auth error: $errorMessage")
                val msg = when {
                    errorMessage.contains("EMAIL_EXISTS") ->
                        "อีเมลนี้ถูกใช้งานแล้ว"
                    errorMessage.contains("INVALID_EMAIL") ->
                        "รูปแบบอีเมลไม่ถูกต้อง"
                    errorMessage.contains("WEAK_PASSWORD") ->
                        "เบอร์โทร (รหัสผ่านเริ่มต้น) ต้องมีอย่างน้อย 6 ตัว"
                    else -> "สร้างบัญชีไม่สำเร็จ: $errorMessage"
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        )
    }
}