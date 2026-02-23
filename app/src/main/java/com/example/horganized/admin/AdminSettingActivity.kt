package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.LoginActivity
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth

class AdminSettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_setting).setOnClickListener {
            finish()
        }

        // เชื่อมไปยังหน้าแก้ไขข้อมูลส่วนตัว
        findViewById<CardView>(R.id.cv_edit_profile).setOnClickListener {
            val intent = Intent(this, AdminEditProfileActivity::class.java)
            startActivity(intent)
        }

        // เชื่อมไปยังหน้าเปลี่ยนรหัสผ่าน
        findViewById<CardView>(R.id.cv_change_password).setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cv_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("ออกจากระบบ")
            .setMessage("คุณต้องการออกจากระบบใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}