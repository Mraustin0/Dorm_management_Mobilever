package com.example.horganized.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.ChatMessage
import com.example.horganized.user.ChatAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private var chatRoomId = ""  // = userId ของ user ที่ admin กำลังคุยด้วย
    private val adminId get() = auth.currentUser?.uid ?: "admin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_top_bar_detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        val userName = intent.getStringExtra("USER_NAME") ?: ""

        // แสดง header
        findViewById<TextView>(R.id.tv_chat_detail_title).text = roomName
        findViewById<TextView>(R.id.tv_banner_text).text = "$roomName  คุณ $userName"

        // หา input bar — layout_input มี EditText และ ImageView ส่ง
        rvChat    = findViewById(R.id.rv_chat_detail)
        etMessage = findViewById(R.id.et_chat_detail_message)
        btnSend   = findViewById(R.id.btn_send_chat_detail)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        findViewById<ImageView>(R.id.btn_back_chat_detail).setOnClickListener { finish() }

        listenMessages()

        btnSend.setOnClickListener { sendMessage() }
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

                if (messages.isNotEmpty()) rvChat.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return
        if (chatRoomId.isEmpty()) return

        val message = hashMapOf(
            "senderId"  to adminId,
            "message"   to text,
            "timestamp" to Timestamp.now()
        )

        db.collection("chats").document(chatRoomId)
            .collection("messages").add(message)
            .addOnSuccessListener {
                etMessage.setText("")
                // อัปเดต lastMessage บน chat room doc
                db.collection("chats").document(chatRoomId)
                    .update(mapOf(
                        "lastMessage"   to text,
                        "lastTimestamp" to Timestamp.now()
                    ))
            }
            .addOnFailureListener {
                Toast.makeText(this, "ส่งข้อความไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }
}
