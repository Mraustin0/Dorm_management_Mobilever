package com.example.horganized.admin

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.AdminRepairAdapter
import com.example.horganized.model.RepairRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminRepairListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvRepairList: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private lateinit var adapter: AdminRepairAdapter

    private val allRepairList = mutableListOf<RepairRequest>()
    private var currentFilter = "all" // all, pending, in_progress, completed

    // chip views
    private lateinit var chipAll       : TextView
    private lateinit var chipPending   : TextView
    private lateinit var chipInProgress: TextView
    private lateinit var chipCompleted : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_repair_list)

        rvRepairList   = findViewById(R.id.rv_admin_repair_list)
        llEmpty        = findViewById(R.id.ll_empty_repair)
        chipAll        = findViewById(R.id.chip_all)
        chipPending    = findViewById(R.id.chip_pending)
        chipInProgress = findViewById(R.id.chip_in_progress)
        chipCompleted  = findViewById(R.id.chip_completed)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_tech_contacts).setOnClickListener {
            startActivity(Intent(this, AdminTechnicianActivity::class.java))
        }

        rvRepairList.layoutManager = LinearLayoutManager(this)
        adapter = AdminRepairAdapter(mutableListOf()) { item ->
            if (!item.isRead) {
                db.collection("repair_requests").document(item.requestId)
                    .update("isRead", true)
                    .addOnSuccessListener {
                        Log.d("AdminRepair", "Marked as read: ${item.requestId}")
                    }
            }
            val intent = Intent(this, AdminRepairUpdateActivity::class.java)
            intent.putExtra("repair_id", item.requestId)
            startActivity(intent)
        }
        rvRepairList.adapter = adapter

        setupChips()
        fetchRepairRequests()
    }

    private fun setupChips() {
        chipAll.setOnClickListener        { selectChip("all") }
        chipPending.setOnClickListener    { selectChip("pending") }
        chipInProgress.setOnClickListener { selectChip("in_progress") }
        chipCompleted.setOnClickListener  { selectChip("completed") }
    }

    private fun selectChip(filter: String) {
        currentFilter = filter

        // reset ทุก chip
        val chips = listOf(chipAll, chipPending, chipInProgress, chipCompleted)
        chips.forEach { chip ->
            chip.background = ContextCompat.getDrawable(this, R.drawable.chip_unselected)
            chip.setTextColor(0xFF666666.toInt())
        }

        // highlight chip ที่เลือก
        val selected = when (filter) {
            "pending"     -> chipPending
            "in_progress" -> chipInProgress
            "completed"   -> chipCompleted
            else          -> chipAll
        }
        selected.background = ContextCompat.getDrawable(this, R.drawable.chip_selected)
        selected.setTextColor(0xFFFFFFFF.toInt())

        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "pending"     -> allRepairList.filter { it.status == "pending" }
            "in_progress" -> allRepairList.filter { it.status == "in_progress" }
            "completed"   -> allRepairList.filter { it.status == "completed" }
            else          -> allRepairList.toList()
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            rvRepairList.visibility = View.GONE
            llEmpty.visibility = View.VISIBLE
        } else {
            rvRepairList.visibility = View.VISIBLE
            llEmpty.visibility = View.GONE
        }
    }

    private fun fetchRepairRequests() {
        db.collection("repair_requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminRepair", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    allRepairList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(RepairRequest::class.java)
                        allRepairList.add(item)
                    }
                    applyFilter()
                }
            }
    }
}
