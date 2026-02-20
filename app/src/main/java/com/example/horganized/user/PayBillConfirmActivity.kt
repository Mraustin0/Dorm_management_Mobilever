package com.example.horganized.user

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.horganized.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayBillConfirmActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var billId = ""
    private var imageUriString: String? = null
    private var billAmount = 0.0

    companion object {
        private const val IMGBB_API_KEY = "03c7b6aeebf7b62f78427d1d28a7a7b4"  // ← ใส่ API Key จาก https://api.imgbb.com/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_bill_confirm)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val ivSlip = findViewById<ImageView>(R.id.iv_slip_preview)

        btnBack.setOnClickListener { finish() }

        billId = intent.getStringExtra("BILL_ID") ?: ""
        imageUriString = intent.getStringExtra("IMAGE_URI")
        billAmount = intent.getDoubleExtra("BILL_AMOUNT", 0.0)

        if (imageUriString != null) {
            ivSlip.setImageURI(Uri.parse(imageUriString))
        }

        updateBillInfo(billAmount)

        btnConfirm.setOnClickListener {
            btnConfirm.isEnabled = false
            btnConfirm.text = "กำลังอัพโหลด..."
            uploadSlipAndUpdateBill()
        }
    }

    private fun updateBillInfo(amount: Double) {
        findViewById<TextView>(R.id.tv_amount_value).text = "${amount.toInt()} บาท"

        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH.mm", Locale.getDefault())

        findViewById<TextView>(R.id.tv_date_value).text = dateFormat.format(currentDate)
        findViewById<TextView>(R.id.tv_time_value).text = "${timeFormat.format(currentDate)} น."
    }

    private fun uploadSlipAndUpdateBill() {
        val uri = imageUriString?.let { Uri.parse(it) }
        if (uri == null || billId.isEmpty()) {
            Toast.makeText(this, "ข้อมูลไม่ครบ กรุณาลองใหม่", Toast.LENGTH_SHORT).show()
            resetButton()
            return
        }

        // แปลงรูปเป็น Base64 + compress ก่อนอัพไป ImgBB
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Toast.makeText(this, "อ่านไฟล์รูปไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                resetButton()
                return
            }

            // ย่อขนาดรูปถ้าใหญ่เกิน 1280px
            val maxSize = 1280
            val ratio = minOf(maxSize.toFloat() / originalBitmap.width, maxSize.toFloat() / originalBitmap.height, 1f)
            val scaledBitmap = if (ratio < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * ratio).toInt(), (originalBitmap.height * ratio).toInt(), true)
            } else {
                originalBitmap
            }

            // compress เป็น JPEG 80%
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedBytes = outputStream.toByteArray()

            Log.d("PayBillConfirm", "Original: ${originalBitmap.width}x${originalBitmap.height}, Compressed: ${compressedBytes.size / 1024}KB")

            val base64Image = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            uploadToImgBB(base64Image)
        } catch (e: Exception) {
            Log.e("PayBillConfirm", "Error reading image", e)
            Toast.makeText(this, "อ่านไฟล์รูปไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
            resetButton()
        }
    }

    /**
     * อัพโหลดรูปไป ImgBB ผ่าน REST API (ฟรี ไม่ต้องใช้ Firebase Storage)
     */
    private fun uploadToImgBB(base64Image: String) {
        Thread {
            try {
                val url = URL("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true

                // ImgBB ใช้ form-urlencoded
                val postData = "image=${java.net.URLEncoder.encode(base64Image, "UTF-8")}"

                conn.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    val imageUrl = json.getJSONObject("data").getString("url")

                    runOnUiThread { updateBillWithSlipUrl(imageUrl) }
                } else {
                    val errorResponse = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e("PayBillConfirm", "ImgBB error: $errorResponse")
                    runOnUiThread {
                        Toast.makeText(this, "อัพโหลดรูปไม่สำเร็จ กรุณาลองใหม่", Toast.LENGTH_LONG).show()
                        resetButton()
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("PayBillConfirm", "ImgBB upload error", e)
                runOnUiThread {
                    Toast.makeText(this, "อัพโหลดรูปไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
                    resetButton()
                }
            }
        }.start()
    }

    /**
     * ได้ URL รูปจาก ImgBB แล้ว → อัปเดต bill ใน Firestore
     */
    private fun updateBillWithSlipUrl(imageUrl: String) {
        db.collection("bills").document(billId)
            .update(
                mapOf(
                    "slipUrl" to imageUrl,
                    "paymentDate" to Timestamp.now(),
                    "status" to "pending"
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "แจ้งชำระเงินเรียบร้อยแล้ว!",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, HomeUserActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "อัปเดตบิลไม่สำเร็จ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetButton()
            }
    }

    private fun resetButton() {
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        btnConfirm.isEnabled = true
        btnConfirm.text = "ยืนยันการชำระเงิน"
    }
}
