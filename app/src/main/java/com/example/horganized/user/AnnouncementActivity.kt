package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Announcement
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AnnouncementActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val announcementList = mutableListOf<Announcement>()
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)

        db = FirebaseFirestore.getInstance()

        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        val rv = findViewById<RecyclerView>(R.id.rv_announcements)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementAdapter(announcementList)
        rv.adapter = adapter

        // ดึงประกาศจริงจาก Firebase แบบ Real-time
        fetchAnnouncements()
        setupBottomNavigation()
    }

    private fun fetchAnnouncements() {
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING) // เอาประกาศล่าสุดขึ้นก่อน
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("AnnouncementActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    announcementList.clear()
                    for (document in snapshots) {
                        val announcement = document.toObject(Announcement::class.java)
                        announcementList.add(announcement)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeUserActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_bill -> {
                    startActivity(Intent(this, DetailBillActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_notifications -> {
                    startActivity(Intent(this, DormInfoActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}