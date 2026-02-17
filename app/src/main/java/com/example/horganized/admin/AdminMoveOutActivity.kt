package com.example.horganized.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminMoveOutActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var roomNumber = ""
    private var tenantId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_move_out)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val roomName = intent.getStringExtra("ROOM_NAME") ?: "ห้อง xxx"
        roomNumber = roomName.replace("ห้อง ", "").trim()

        findViewById<TextView>(R.id.tv_room_title_move_out).text = roomName

        findViewById<ImageView>(R.id.btn_back_move_out).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btn_confirm_move_out).setOnClickListener {
            showMoveOutConfirmationDialog()
        }

        // โหลดข้อมูลผู้เช่าจาก Firestore
        loadTenantData()
    }

    private fun loadTenantData() {
        // ดึง tenantId จาก room document
        db.collection("rooms").document(roomNumber)
            .get()
            .addOnSuccessListener { roomDoc ->
                tenantId = roomDoc.getString("tenantId") ?: ""
                if (tenantId.isEmpty()) {
                    Toast.makeText(this, "ไม่พบข้อมูลผู้เช่า", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // ดึงข้อมูลผู้เช่า
                db.collection("users").document(tenantId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "-"
                        val surname = userDoc.getString("surname") ?: "-"
                        val phone = userDoc.getString("phone") ?: "-"
                        val email = userDoc.getString("email") ?: "-"
                        val contractTerm = userDoc.getString("contractTerm") ?: "-"
                        val moveInDate = userDoc.getTimestamp("moveInDate")
                        val contractUrl = userDoc.getString("contractUrl") ?: ""

                        // กดดูเอกสารสัญญา
                        findViewById<android.widget.RelativeLayout>(R.id.layout_contract).setOnClickListener {
                            if (contractUrl.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contractUrl))
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "ยังไม่มีเอกสารสัญญา", Toast.LENGTH_SHORT).show()
                            }
                        }

                        findViewById<TextView>(R.id.tv_move_out_name).text = name
                        findViewById<TextView>(R.id.tv_move_out_surname).text = surname
                        findViewById<TextView>(R.id.tv_move_out_phone).text = phone
                        findViewById<TextView>(R.id.tv_move_out_email).text = email

                        // คำนวณวันเริ่มต้น-สิ้นสุดสัญญา
                        if (moveInDate != null) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("th"))
                            val startDate = moveInDate.toDate()
                            findViewById<TextView>(R.id.tv_move_out_start).text = dateFormat.format(startDate)

                            val months = when (contractTerm) {
                                "3 เดือน" -> 3
                                "6 เดือน" -> 6
                                "12 เดือน" -> 12
                                else -> 6
                            }
                            val cal = Calendar.getInstance()
                            cal.time = startDate
                            cal.add(Calendar.MONTH, months)
                            findViewById<TextView>(R.id.tv_move_out_end).text = dateFormat.format(cal.time)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MoveOut", "Error loading tenant", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MoveOut", "Error loading room", e)
            }
    }

    private fun showMoveOutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("ยืนยันการย้ายออก")
            .setMessage("คุณต้องการย้ายผู้เช่าออกจากห้องนี้ใช่หรือไม่?")
            .setPositiveButton("ยืนยัน") { _, _ ->
                performMoveOut()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun performMoveOut() {
        // 1) อัปเดตห้องเป็นว่าง
        db.collection("rooms").document(roomNumber)
            .update(
                "isVacant", true,
                "tenantId", ""
            )
            .addOnSuccessListener {
                // 2) ลบข้อมูลผู้เช่า (หรือจะเก็บไว้เป็น history ก็ได้)
                if (tenantId.isNotEmpty()) {
                    db.collection("users").document(tenantId).delete()
                }
                Toast.makeText(this, "ดำเนินการย้ายออกเรียบร้อย", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("MoveOut", "Error moving out", e)
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
