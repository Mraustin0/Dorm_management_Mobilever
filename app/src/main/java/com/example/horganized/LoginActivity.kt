package com.example.horganized

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.user.HomeUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("th")
        db = FirebaseFirestore.getInstance()

        // เช็ค network ตอนเปิดแอป
        checkAndLogNetwork()

        // เช็คว่า login อยู่แล้วหรือยัง
        if (auth.currentUser != null) {
            Log.d(TAG, "User already logged in: ${auth.currentUser?.email}")
            startActivity(Intent(this, HomeUserActivity::class.java))
            finish()
            return
        }

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<RelativeLayout>(R.id.btn_login_submit_custom)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // บัญชี Mock สำหรับทดสอบหน้าจอ
            if (email == "tin" && password == "1234") {
                Toast.makeText(this, "Mock Login Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeUserActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // เช็ค network ก่อน login
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "ไม่มีการเชื่อมต่ออินเทอร์เน็ต กรุณาเชื่อมต่อแล้วลองใหม่", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login with: $email")
            Log.d(TAG, "Network available: ${isNetworkAvailable()}")

            // ทดสอบ ping Firebase ก่อน
            Thread {
                try {
                    val url = java.net.URL("https://www.googleapis.com")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.connect()
                    Log.d(TAG, "Google API reachable: ${conn.responseCode}")
                    conn.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot reach Google API: ${e.message}")
                }
            }.start()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    Log.d(TAG, "Login SUCCESS: ${result.user?.uid}")
                    checkUserRole(result.user?.uid)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Login FAILED: ${exception.javaClass.simpleName}: ${exception.message}", exception)

                    val errorMessage = when (exception) {
                        is FirebaseAuthException -> {
                            Log.e(TAG, "Error code: ${exception.errorCode}")
                            when (exception.errorCode) {
                                "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                                "ERROR_WRONG_PASSWORD" -> "รหัสผ่านไม่ถูกต้อง"
                                "ERROR_INVALID_EMAIL" -> "รูปแบบอีเมลไม่ถูกต้อง"
                                "ERROR_USER_DISABLED" -> "บัญชีถูกปิดใช้งาน"
                                "ERROR_INVALID_CREDENTIAL" -> "อีเมลหรือรหัสผ่านไม่ถูกต้อง"
                                else -> "เข้าสู่ระบบไม่สำเร็จ: ${exception.errorCode}"
                            }
                        }
                        else -> "เข้าสู่ระบบไม่สำเร็จ: ${exception.javaClass.simpleName}\n${exception.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkAndLogNetwork() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        Log.d(TAG, "=== NETWORK STATUS ===")
        Log.d(TAG, "Active network: $network")
        Log.d(TAG, "Has INTERNET: ${caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
        Log.d(TAG, "Has WIFI: ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
        Log.d(TAG, "Has CELLULAR: ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
        Log.d(TAG, "Has VALIDATED: ${caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")
        Log.d(TAG, "======================")
    }

    private fun checkUserRole(uid: String?) {
        if (uid == null) return
        Log.d(TAG, "Checking role for uid: $uid")

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    Log.d(TAG, "User role: $role")
                    if (role == "admin") {
                        Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, com.example.horganized.admin.AdminHomeActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeUserActivity::class.java))
                    }
                    finish()
                } else {
                    Log.w(TAG, "User document not found, going to home")
                    startActivity(Intent(this, HomeUserActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get user role: ${e.message}")
                startActivity(Intent(this, HomeUserActivity::class.java))
                finish()
            }
    }
}
