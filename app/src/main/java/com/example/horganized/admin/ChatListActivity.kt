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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatRoom(
    val chatRoomId: String = "",
    val userName: String = "",
    val roomNumber: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Timestamp? = null
)

class ChatListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvChatList: RecyclerView
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var adapter: ChatRoomAdapter

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
        adapter = ChatRoomAdapter(chatRooms) { room ->
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("CHAT_ROOM_ID", room.chatRoomId)
            intent.putExtra("ROOM_NAME", "ห้อง ${room.roomNumber}")
            intent.putExtra("USER_NAME", room.userName)
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = adapter

        listenChatRooms()
        setupBottomNavigation()
    }

    private fun listenChatRooms() {
        db.collection("chats")
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                chatRooms.clear()
                chatRooms.addAll(snapshots.documents.mapNotNull { doc ->
                    ChatRoom(
                        chatRoomId    = doc.id,
                        userName      = doc.getString("userName") ?: "",
                        roomNumber    = doc.getString("roomNumber") ?: "",
                        lastMessage   = doc.getString("lastMessage") ?: "",
                        lastTimestamp = doc.getTimestamp("lastTimestamp")
                    )
                })
                adapter.notifyDataSetChanged()
            }
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
            startActivity(Intent(this, AdminSelectRoomActivity::class.java))
        }
    }
}

class ChatRoomAdapter(
    private val rooms: List<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoomName: TextView = view.findViewById(R.id.tv_chat_room_name)
        val tvLastMsg: TextView  = view.findViewById(R.id.tv_last_message)
        val tvTime: TextView     = view.findViewById(R.id.tv_chat_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val room = rooms[position]
        holder.tvRoomName.text = "ห้อง ${room.roomNumber}  ${room.userName}"
        holder.tvLastMsg.text  = room.lastMessage.ifEmpty { "ยังไม่มีข้อความ" }
        holder.tvTime.text     = room.lastTimestamp?.toDate()?.let { formatTime(it) } ?: ""
        holder.itemView.setOnClickListener { onClick(room) }
    }

    override fun getItemCount() = rooms.size

    private fun formatTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        return when {
            diff < 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("d MMM", Locale("th")).format(date)
        }
    }
}
