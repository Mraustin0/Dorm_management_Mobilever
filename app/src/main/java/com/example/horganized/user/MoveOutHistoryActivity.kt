package com.example.horganized.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.adapter.MoveOutHistoryAdapter
import com.example.horganized.model.MoveOutRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MoveOutHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val historyList = mutableListOf<MoveOutRequest>()
    private lateinit var adapter: MoveOutHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_out_history)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rv_move_out_history)
        val llEmpty = findViewById<LinearLayout>(R.id.ll_empty_state)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = MoveOutHistoryAdapter(historyList)
        rv.adapter = adapter

        fetchMoveOutHistory(rv, llEmpty)
    }

    private fun fetchMoveOutHistory(rv: RecyclerView, llEmpty: LinearLayout) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("move_out_requests")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MoveOutHistory", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    historyList.clear()
                    for (doc in snapshots) {
                        val item = doc.toObject(MoveOutRequest::class.java)
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
