package com.example.horganized.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContractListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_list)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        // เมื่อคลิกที่ Card สัญญา
        findViewById<CardView>(R.id.card_contract_item).setOnClickListener {
            loadAndOpenContract()
        }
    }

    private fun loadAndOpenContract() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "กรุณาเข้าสู่ระบบ", Toast.LENGTH_SHORT).show()
            return
        }

        // ดึงลิงก์สัญญาจากข้อมูลของผู้ใช้ใน Firestore
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val url = document.getString("contractUrl")
                    if (!url.isNullOrEmpty()) {
                        openContractLink(url)
                    } else {
                        Toast.makeText(this, "ยังไม่มีไฟล์สัญญาแนบไว้ในระบบ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการโหลดข้อมูล", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openContractLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "ไม่สามารถเปิดไฟล์ได้ กรุณาตรวจสอบลิงก์", Toast.LENGTH_SHORT).show()
        }
    }
}