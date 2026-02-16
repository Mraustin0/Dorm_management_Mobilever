package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R

data class ChatMessage(val roomName: String, val lastMessage: String, val time: String)

class ChatListActivity : AppCompatActivity() {

    private lateinit var rvChatList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_top_bar_chat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvChatList = findViewById(R.id.rv_chat_list)
        setupRecyclerView()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navHome = findViewById<ImageView>(R.id.iv_nav_home)
        val navApartment = findViewById<ImageView>(R.id.iv_nav_apartment)

        navHome.setOnClickListener {
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        navApartment.setOnClickListener {
            val intent = Intent(this, AdminSelectRoomActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val chatList = listOf(
            ChatMessage("ห้อง 101", "สอบถามเรื่องค่าน้ำครับ", "10:30"),
            ChatMessage("ห้อง 204", "แจ้งซ่อมไฟทางเดินครับ", "09:15"),
            ChatMessage("ห้อง 302", "จ่ายค่าเช่าแล้วครับ ส่งสลิปในแอป", "Yesterday"),
            ChatMessage("ห้อง 105", "ขอบคุณมากค่ะ", "Yesterday"),
            ChatMessage("ห้อง 410", "มีพัสดุมาส่งไหมครับ?", "Monday")
        )

        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = ChatAdapter(chatList) { chat ->
            // เมื่อกดที่รายการแชท ให้เปิดหน้า Chat Detail
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("ROOM_NAME", chat.roomName)
            startActivity(intent)
        }
    }
}

class ChatAdapter(private val chats: List<ChatMessage>, private val onItemClick: (ChatMessage) -> Unit) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoomName: TextView = view.findViewById(R.id.tv_chat_room_name)
        val tvLastMessage: TextView = view.findViewById(R.id.tv_last_message)
        val tvTime: TextView = view.findViewById(R.id.tv_chat_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.tvRoomName.text = chat.roomName
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text = chat.time
        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount() = chats.size
}