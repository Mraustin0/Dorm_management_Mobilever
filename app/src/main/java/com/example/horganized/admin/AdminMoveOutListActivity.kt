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
import com.example.horganized.adapter.AdminMoveOutAdapter
import com.example.horganized.model.MoveOutRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
        adapter = AdminMoveOutAdapter(moveOutList) { item ->
            val intent = Intent(this, AdminMoveOutProcessActivity::class.java)
            intent.putExtra("request_id", item.requestId)
            startActivity(intent)
        }
        rvMoveOutList.adapter = adapter

        fetchMoveOutRequests()
    }

    private fun fetchMoveOutRequests() {
        db.collection("move_out_requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AdminMoveOut", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    moveOutList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(MoveOutRequest::class.java)
                        // Ensure requestId is captured from document ID if not in model
                        val requestWithId = item.copy(requestId = doc.id)
                        moveOutList.add(requestWithId)
                    }
                    
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
}
