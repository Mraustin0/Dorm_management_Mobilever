package com.example.horganized.user

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.ChatMessage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var tvBanner: TextView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    // chatRoomId = uid ของ user เอง
    private val chatRoomId get() = auth.currentUser?.uid ?: ""

    // ข้อมูล user สำหรับส่ง noti ไปหา admin
    private var userRoomNumber = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rv_chat)
        etMessage = findViewById(R.id.et_chat_message)
        btnSend = findViewById(R.id.btn_send_chat)
        tvBanner = findViewById(R.id.tv_banner)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        loadUserInfo()
        listenMessages()

        btnSend.setOnClickListener {
            sendMessage()
        }

        setupBottomNavigation()
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: doc.getString("firstName") ?: ""
                userRoomNumber = doc.getString("roomNumber") ?: ""
                tvBanner.text = "ห้อง $userRoomNumber · $userName"

                // สร้าง/อัปเดต chat room document ไว้ให้ admin เห็นใน list
                // + ล้าง hasUnreadForUser เพราะ user กำลังเปิดดูแชทอยู่
                db.collection("chats").document(chatRoomId)
                    .set(mapOf(
                        "userId"           to uid,
                        "userName"         to userName,
                        "roomNumber"       to userRoomNumber,
                        "lastMessage"      to "",
                        "lastTimestamp"    to Timestamp.now(),
                        "hasUnreadForUser" to false
                    ), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    private fun listenMessages() {
        if (chatRoomId.isEmpty()) return

        db.collection("chats").document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                messages.addAll(snapshots.documents.mapNotNull { it.toObject(ChatMessage::class.java) })
                chatAdapter.notifyDataSetChanged()

                // scroll ไปล่างสุดเสมอ
                if (messages.isNotEmpty()) {
                    rvChat.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return
        if (chatRoomId.isEmpty()) {
            Toast.makeText(this, "กรุณา login ก่อน", Toast.LENGTH_SHORT).show()
            return
        }

        val message = hashMapOf(
            "senderId"  to chatRoomId,
            "message"   to text,
            "timestamp" to Timestamp.now()
        )

        // เพิ่ม message ใน subcollection
        db.collection("chats").document(chatRoomId)
            .collection("messages").add(message)
            .addOnSuccessListener {
                etMessage.setText("")
                val now = Timestamp.now()
                // อัปเดต lastMessage บน chat room doc เพื่อให้ admin list แสดงผล
                db.collection("chats").document(chatRoomId)
                    .update(mapOf(
                        "lastMessage"   to text,
                        "lastTimestamp" to now
                    ))
                // ส่ง notification ไปหา admin (เก็บลง Admin_Notifications)
                sendChatNotificationToAdmin(text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "ส่งข้อความไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }

    /** ส่ง notification ไปหา admin เมื่อ user ส่งข้อความ (เก็บใน Admin_Notifications) */
    private fun sendChatNotificationToAdmin(text: String) {
        val uid = auth.currentUser?.uid ?: return
        val notification = hashMapOf(
            "userId"     to uid,
            "title"      to "ข้อความจากห้อง $userRoomNumber",
            "message"    to text,
            "type"       to "chat",
            "roomNumber" to userRoomNumber,
            "senderName" to (userName.ifEmpty { "ผู้เช่า" }),
            "timestamp"  to Timestamp.now(),
            "isRead"     to false
        )
        db.collection("Admin_Notifications").add(notification)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_chat

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
                R.id.navigation_chat -> true
                else -> false
            }
        }
    }
}
