package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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
    private val repairList = mutableListOf<RepairRequest>()
    private lateinit var adapter: AdminRepairAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_repair_list)

        rvRepairList = findViewById(R.id.rv_admin_repair_list)
        llEmpty = findViewById(R.id.ll_empty_repair)
        
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_tech_contacts).setOnClickListener {
            // ไปหน้าเบอร์โทรช่างเดิม
            startActivity(Intent(this, AdminTechnicianActivity::class.java))
        }

        rvRepairList.layoutManager = LinearLayoutManager(this)
        adapter = AdminRepairAdapter(repairList) { item ->
            val intent = Intent(this, AdminRepairUpdateActivity::class.java)
            intent.putExtra("repair_id", item.requestId)
            startActivity(intent)
        }
        rvRepairList.adapter = adapter

        fetchRepairRequests()
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
                    repairList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(RepairRequest::class.java)
                        repairList.add(item)
                    }
                    
                    if (repairList.isEmpty()) {
                        rvRepairList.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                    } else {
                        rvRepairList.visibility = View.VISIBLE
                        llEmpty.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
