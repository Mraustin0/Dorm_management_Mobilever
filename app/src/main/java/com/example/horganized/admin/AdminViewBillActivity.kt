package com.example.horganized.admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.horganized.R
import com.example.horganized.model.Bill
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewBillActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_view_bill)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val billId   = intent.getStringExtra("BILL_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""

        findViewById<ImageView>(R.id.btn_back_view_bill).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_view_bill_title).text = "บิลค่าเช่า $roomName"

        if (billId.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลบิล", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadBill(billId)
    }

    private fun loadBill(billId: String) {
        db.collection("bills").document(billId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "ไม่พบข้อมูลบิล", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val bill = doc.toObject(Bill::class.java) ?: return@addOnSuccessListener

                // อัปเดต title ให้มีเดือน/ปี
                findViewById<TextView>(R.id.tv_view_bill_title).text =
                    "บิลค่าเช่า ห้อง ${bill.roomNumber} (${bill.month} ${bill.year})"

                // Status badge
                val tvStatus = findViewById<TextView>(R.id.tv_view_bill_status)
                val greenColor  = android.graphics.Color.parseColor("#1B9E44")
                val orangeColor = android.graphics.Color.parseColor("#FF9800")
                val redColor    = android.graphics.Color.parseColor("#E53935")
                val density = resources.displayMetrics.density

                fun setStatusBadge(text: String, color: Int) {
                    tvStatus.text = text
                    val bg = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        cornerRadius = 20f * density
                        setColor(color)
                    }
                    tvStatus.background = bg
                }

                when {
                    bill.isPaid    -> setStatusBadge("ชำระแล้ว", greenColor)
                    bill.isPending -> setStatusBadge("รอการยืนยัน", orangeColor)
                    else           -> setStatusBadge("ค้างชำระ", redColor)
                }

                // โหลดข้อมูลผู้เช่า
                if (bill.userId.isNotEmpty()) {
                    db.collection("users").document(bill.userId).get()
                        .addOnSuccessListener { userDoc ->
                            val name  = "${userDoc.getString("name") ?: ""} ${userDoc.getString("surname") ?: ""}".trim()
                            val phone = userDoc.getString("phone") ?: "-"
                            findViewById<TextView>(R.id.tv_vb_tenant_name).text  = name.ifEmpty { "-" }
                            findViewById<TextView>(R.id.tv_vb_tenant_phone).text = phone
                        }
                }

                // รายละเอียดบิล
                val elecUnit  = bill.details.electricUnit.trim().replace(" * ", "").replace("*", "").trim()
                val waterUnit = bill.details.waterUnit.trim().replace(" * ", "").replace("*", "").trim()

                findViewById<TextView>(R.id.tv_vb_room_price).text =
                    String.format("%,.0f บาท", bill.details.roomPrice)
                findViewById<TextView>(R.id.tv_vb_electric_price).text =
                    "$elecUnit หน่วย = ${String.format("%,.0f", bill.details.electricPrice)} บาท"
                findViewById<TextView>(R.id.tv_vb_water_price).text =
                    "$waterUnit หน่วย = ${String.format("%,.0f", bill.details.waterPrice)} บาท"
                findViewById<TextView>(R.id.tv_vb_other_price).text =
                    String.format("%,.0f บาท", bill.details.otherPrice)

                val tvTotal = findViewById<TextView>(R.id.tv_vb_total_price)
                tvTotal.text = String.format("%,.2f บาท", bill.amount)
                tvTotal.setTextColor(when {
                    bill.isPaid -> greenColor
                    else        -> redColor
                })
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
