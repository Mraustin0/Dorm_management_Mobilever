package com.example.horganized.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private lateinit var layoutEmpty: LinearLayout
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_top_bar_chat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val dp20 = (20 * resources.displayMetrics.density).toInt()
            val dp14 = (14 * resources.displayMetrics.density).toInt()
            v.setPadding(dp20, systemBars.top + dp14, dp20, dp14)
            insets
        }

        rvChatList  = findViewById(R.id.rv_chat_list)
        layoutEmpty = findViewById(R.id.layout_empty_chat)

        adapter = ChatRoomAdapter(chatRooms) { room ->
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("CHAT_ROOM_ID", room.chatRoomId)
            intent.putExtra("ROOM_NAME", "ห้อง ${room.roomNumber}")
            intent.putExtra("USER_NAME", room.userName)
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = adapter
        rvChatList.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)
        )

        listenChatRooms()
        setupBottomNavigation()

        // ปุ่ม + เริ่มแชทห้องใหม่ → เลือกห้องแบบ Dialog
        findViewById<android.view.View>(R.id.iv_new_chat).setOnClickListener {
            openNewChatDialog()
        }
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

                // แสดง empty state ถ้าไม่มีแชท
                if (chatRooms.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvChatList.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvChatList.visibility = View.VISIBLE
                }
            }
    }

    // เลือกห้องที่มี user อาศัยอยู่ เพื่อเริ่มแชทใหม่
    private fun openNewChatDialog() {
        db.collection("users")
            .whereEqualTo("role", "user")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                if (users.isEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle("ไม่พบผู้เช่า")
                        .setMessage("ยังไม่มีผู้เช่าในระบบ")
                        .setPositiveButton("ตกลง", null)
                        .show()
                    return@addOnSuccessListener
                }

                val bottomSheet = BottomSheetDialog(this)
                val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_select_chat_user, null)
                bottomSheet.setContentView(sheetView)

                val rv = sheetView.findViewById<RecyclerView>(R.id.rv_select_chat_user)
                rv.layoutManager = LinearLayoutManager(this)
                rv.adapter = SelectChatUserAdapter(users) { userDoc ->
                    bottomSheet.dismiss()
                    val roomNum  = userDoc.getString("roomNumber") ?: ""
                    val userName = userDoc.getString("name") ?: ""
                    val intent = Intent(this, ChatDetailActivity::class.java).apply {
                        putExtra("CHAT_ROOM_ID", userDoc.id)
                        putExtra("ROOM_NAME", "ห้อง $roomNum")
                        putExtra("USER_NAME", userName)
                    }
                    startActivity(intent)
                }

                bottomSheet.show()
            }
    }

    private fun setupBottomNavigation() {
        // highlight icon ที่ active (chat)
        findViewById<ImageView>(R.id.iv_nav_chat)
            .setColorFilter(getColor(R.color.blue_primary))
        findViewById<ImageView>(R.id.iv_nav_home)
            .setColorFilter(getColor(R.color.gray_text))
        findViewById<ImageView>(R.id.iv_nav_apartment)
            .setColorFilter(getColor(R.color.gray_text))

        findViewById<ImageView>(R.id.iv_nav_home).setOnClickListener {
            startActivity(Intent(this, AdminHomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
        findViewById<ImageView>(R.id.iv_nav_apartment).setOnClickListener {
            startActivity(Intent(this, AdminSelectRoomActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
        // iv_nav_chat อยู่หน้านี้แล้ว ไม่ต้องทำอะไร
    }
}

class ChatRoomAdapter(
    private val rooms: List<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar:   TextView = view.findViewById(R.id.tv_chat_avatar)
        val tvRoomName: TextView = view.findViewById(R.id.tv_chat_room_name)
        val tvLastMsg:  TextView = view.findViewById(R.id.tv_last_message)
        val tvTime:     TextView = view.findViewById(R.id.tv_chat_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val room = rooms[position]
        holder.tvAvatar.text   = room.roomNumber.firstOrNull()?.toString() ?: "?"
        holder.tvRoomName.text = "ห้อง ${room.roomNumber}  ${room.userName}"
        holder.tvLastMsg.text  = room.lastMessage.ifEmpty { "ยังไม่มีข้อความ" }
        holder.tvTime.text     = room.lastTimestamp?.toDate()?.let { formatTime(it) } ?: ""
        holder.itemView.setOnClickListener { onClick(room) }
    }

    override fun getItemCount() = rooms.size

    private fun formatTime(date: Date): String {
        val now  = Date()
        val diff = now.time - date.time
        return when {
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("d MMM", Locale("th")).format(date)
        }
    }
}

class SelectChatUserAdapter(
    private val users: List<com.google.firebase.firestore.DocumentSnapshot>,
    private val onClick: (com.google.firebase.firestore.DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<SelectChatUserAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar:   TextView  = view.findViewById(R.id.tv_avatar_initial)
        val tvRoom:     TextView  = view.findViewById(R.id.tv_room_number)
        val tvUserName: TextView  = view.findViewById(R.id.tv_user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_select_chat_user, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doc      = users[position]
        val roomNum  = doc.getString("roomNumber") ?: "-"
        val name     = doc.getString("name") ?: "-"
        val surname  = doc.getString("surname") ?: ""
        val fullName = "$name $surname".trim()

        holder.tvRoom.text     = "ห้อง $roomNum"
        holder.tvUserName.text = fullName
        holder.tvAvatar.text   = name.firstOrNull()?.uppercase() ?: "?"
        holder.itemView.setOnClickListener { onClick(doc) }
    }

    override fun getItemCount() = users.size
}
