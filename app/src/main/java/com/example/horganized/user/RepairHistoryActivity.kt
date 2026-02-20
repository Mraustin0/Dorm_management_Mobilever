package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.RepairHistoryAdapter
import com.example.horganized.model.RepairRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RepairHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val historyList = mutableListOf<RepairRequest>()
    private lateinit var adapter: RepairHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_history)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rv_repair_history)
        val llEmpty = findViewById<LinearLayout>(R.id.ll_empty_history)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = RepairHistoryAdapter(historyList) { item ->
            val intent = Intent(this, RepairDetailActivity::class.java)
            intent.putExtra("repair_id", item.requestId)
            startActivity(intent)
        }
        rv.adapter = adapter

        fetchHistory(rv, llEmpty)
    }

    private fun fetchHistory(rv: RecyclerView, llEmpty: LinearLayout) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("repair_requests")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null) {
                    historyList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(RepairRequest::class.java)
                        historyList.add(item)
                    }
                    
                    if (historyList.isEmpty()) {
                        rv.visibility = View.GONE
                        llEmpty.visibility = View.VISIBLE
                    } else {
                        rv.visibility = View.VISIBLE
                        llEmpty.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
