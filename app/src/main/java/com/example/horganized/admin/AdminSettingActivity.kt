package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.horganized.LoginActivity
import com.example.horganized.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminSettingActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_setting)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back_setting).setOnClickListener { finish() }

        findViewById<CardView>(R.id.cv_edit_profile).setOnClickListener {
            startActivity(Intent(this, AdminEditProfileActivity::class.java))
        }

        findViewById<CardView>(R.id.cv_change_password).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        findViewById<CardView>(R.id.cv_logout).setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        val tvName    = findViewById<TextView>(R.id.tv_setting_name)
        val ivProfile = findViewById<ImageView>(R.id.iv_setting_profile)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("username")
                    ?: doc.getString("firstName")
                    ?: doc.getString("name")
                    ?: "Admin"
                tvName?.text = name

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty() && ivProfile != null) {
                    ivProfile.setPadding(0, 0, 0, 0)
                    Glide.with(this)
                        .load(photoUrl)
                        .transform(CircleCrop())
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfile)
                }
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("ออกจากระบบ")
            .setMessage("คุณต้องการออกจากระบบใช่หรือไม่?")
            .setPositiveButton("ตกลง") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}
