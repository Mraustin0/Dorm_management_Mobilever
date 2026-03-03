package com.example.horganized.user

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.horganized.LoginActivity
import com.example.horganized.R
import com.example.horganized.admin.ChangePasswordActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var contractUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ปุ่มย้อนกลับ
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }
        btnBack.applyHoverAnimation()

        // เมนูแก้ไขข้อมูลส่วนตัว
        findViewById<CardView>(R.id.menu_edit_profile).apply {
            setOnClickListener { startActivity(Intent(this@UserProfileActivity, UserProfileEditActivity::class.java)) }
            applyHoverAnimation()
        }

        // เมนูเปลี่ยนรหัสผ่าน
        findViewById<CardView>(R.id.menu_change_password).apply {
            setOnClickListener { 
                startActivity(Intent(this@UserProfileActivity, ChangePasswordActivity::class.java))
            }
            applyHoverAnimation()
        }

        // เมนูเอกสารและสัญญาเช่า
        findViewById<CardView>(R.id.menu_documents).apply {
            setOnClickListener {
                if (!contractUrl.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contractUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@UserProfileActivity, "ยังไม่มีข้อมูลสัญญาเช่าในระบบ", Toast.LENGTH_SHORT).show()
                }
            }
            applyHoverAnimation()
        }

        // เมนูแจ้งย้ายออก
        findViewById<CardView>(R.id.menu_move_out).apply {
            setOnClickListener { startActivity(Intent(this@UserProfileActivity, UserMoveOutActivity::class.java)) }
            applyHoverAnimation()
        }

        // ปุ่มออกจากระบบ
        findViewById<CardView>(R.id.btn_logout).apply {
            setOnClickListener {
                auth.signOut()
                val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            applyHoverAnimation()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        
        // ใช้ SnapshotListener เพื่อให้อัปเดตข้อมูลทันทีเมื่อมีการแก้ไข
        db.collection("users").document(uid).addSnapshotListener { doc, error ->
            if (error != null) return@addSnapshotListener
            
            if (doc != null && doc.exists()) {
                val name = doc.getString("name") ?: ""
                val room = doc.getString("roomNumber") ?: ""
                findViewById<TextView>(R.id.tv_profile_name).text = "คุณ $name ห้อง $room"
                
                contractUrl = doc.getString("contractUrl")

                val photoUrl = doc.getString("photoUrl")
                val ivProfile = findViewById<ImageView>(R.id.profile_image)
                if (!photoUrl.isNullOrEmpty() && ivProfile != null) {
                    Glide.with(this)
                        .load(photoUrl)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.u1)
                        .into(ivProfile)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.applyHoverAnimation() {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
    }
}
