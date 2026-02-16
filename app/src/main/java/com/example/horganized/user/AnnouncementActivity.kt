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

        // ปุ่มย้อนกลับ
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        }

        val rv = findViewById<RecyclerView>(R.id.rv_announcements)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AnnouncementAdapter(announcementList)
        rv.adapter = adapter

        fetchAnnouncements()
        setupBottomNavigation()
    }

    private fun fetchAnnouncements() {
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                announcementList.clear()
                for (document in documents) {
                    val announcement = document.toObject(Announcement::class.java)
                    announcementList.add(announcement)
                }

                // ถ้าไม่มีข้อมูลใน Firestore ใช้ข้อมูลจำลอง
                if (announcementList.isEmpty()) {
                    addSampleAnnouncements()
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("AnnouncementActivity", "Error fetching announcements: ${e.message}")
                addSampleAnnouncements()
                adapter.notifyDataSetChanged()
            }
    }

    private fun addSampleAnnouncements() {
        announcementList.add(
            Announcement(
                title = "เรื่องวันปิดทำการออฟฟิศ",
                detail = "ออฟฟิศปิดทำการ\n5 ธันวาคม 2568"
            )
        )
        announcementList.add(
            Announcement(
                title = "เรื่องวันปิดทำการออฟฟิศ",
                detail = ""
            )
        )
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
