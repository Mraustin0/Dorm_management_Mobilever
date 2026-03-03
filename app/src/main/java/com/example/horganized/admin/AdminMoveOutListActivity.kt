package com.example.horganized.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.AdminMoveOutAdapter
import com.example.horganized.model.MoveOutRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminMoveOutListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvMoveOutList: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private val moveOutList = mutableListOf<MoveOutRequest>()
    private lateinit var adapter: AdminMoveOutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_move_out_list)

        rvMoveOutList = findViewById(R.id.rv_move_out_list)
        llEmpty = findViewById(R.id.ll_empty_move_out)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        rvMoveOutList.layoutManager = LinearLayoutManager(this)
        adapter = AdminMoveOutAdapter(
            moveOutList,
            onApprove = { item -> confirmApprove(item) },
            onReject  = { item -> confirmReject(item) }
        )
        rvMoveOutList.adapter = adapter

        fetchMoveOutRequests()
    }

    private fun fetchMoveOutRequests() {
        db.collection("move_out_requests")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminMoveOut", "Listen failed.", e)
                    Toast.makeText(this@AdminMoveOutListActivity, "โหลดข้อมูลไม่สำเร็จ: ${e.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    moveOutList.clear()
                    for (doc in snapshots) {
                        try {
                            val item = doc.toObject(MoveOutRequest::class.java)
                            moveOutList.add(item.copy(requestId = doc.id))
                        } catch (e: Exception) {
                            Log.e("AdminMoveOut", "Skip doc ${doc.id}: ${e.message}")
                        }
                    }
                    moveOutList.sortByDescending { it.timestamp?.seconds ?: 0L }
                    if (moveOutList.isEmpty()) {
                        rvMoveOutList.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                    } else {
                        rvMoveOutList.visibility = View.VISIBLE
                        llEmpty.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun confirmApprove(item: MoveOutRequest) {
        AlertDialog.Builder(this)
            .setTitle("อนุมัติการย้ายออก")
            .setMessage("อนุมัติห้อง ${item.roomNumber} ย้ายออกวันที่ ${item.moveOutDate}?")
            .setPositiveButton("อนุมัติ") { _, _ -> approveRequest(item) }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun approveRequest(item: MoveOutRequest) {
        val batch = db.batch()

        // 1. Update request status
        val requestRef = db.collection("move_out_requests").document(item.requestId)
        batch.update(requestRef, "status", "approved")

        // 2. Set room to vacant
        if (item.roomNumber.isNotEmpty()) {
            val roomRef = db.collection("rooms").document(item.roomNumber)
            batch.update(roomRef, mapOf("isVacant" to true, "tenantId" to ""))
        }

        // 3. Notify user
        if (item.userId.isNotEmpty()) {
            val notifRef = db.collection("notifications").document()
            batch.set(notifRef, mapOf(
                "userId"    to item.userId,
                "title"     to "อนุมัติการย้ายออกแล้ว",
                "message"   to "คำขอย้ายออกห้อง ${item.roomNumber} ได้รับการอนุมัติแล้ว ย้ายออกวันที่ ${item.moveOutDate}",
                "type"      to "move_out_approved",
                "isRead"    to false,
                "timestamp" to FieldValue.serverTimestamp()
            ))
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "อนุมัติเรียบร้อย", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "ผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmReject(item: MoveOutRequest) {
        AlertDialog.Builder(this)
            .setTitle("ปฏิเสธการย้ายออก")
            .setMessage("ปฏิเสธคำขอย้ายออกห้อง ${item.roomNumber}?")
            .setPositiveButton("ปฏิเสธ") { _, _ -> rejectRequest(item) }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun rejectRequest(item: MoveOutRequest) {
        db.collection("move_out_requests").document(item.requestId)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "ปฏิเสธเรียบร้อย", Toast.LENGTH_SHORT).show()
                if (item.userId.isNotEmpty()) {
                    db.collection("notifications").add(mapOf(
                        "userId"    to item.userId,
                        "title"     to "คำขอย้ายออกถูกปฏิเสธ",
                        "message"   to "คำขอย้ายออกห้อง ${item.roomNumber} ถูกปฏิเสธ กรุณาติดต่อแอดมิน",
                        "type"      to "move_out_rejected",
                        "isRead"    to false,
                        "timestamp" to FieldValue.serverTimestamp()
                    ))
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "ผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
