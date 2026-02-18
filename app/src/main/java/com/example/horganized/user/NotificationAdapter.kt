package com.example.horganized.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Notification
import java.util.*

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvItemTitle: TextView = view.findViewById(R.id.tv_item_title)
        val tvItemMessage: TextView = view.findViewById(R.id.tv_item_message)
        val tvItemTime: TextView = view.findViewById(R.id.tv_item_time)
        val viewUnreadDot: View = view.findViewById(R.id.view_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        
        // แสดงชื่อผู้ส่งเป็นหัวข้อ
        holder.tvItemTitle.text = notification.senderName
        holder.tvItemMessage.text = notification.message
        
        // แสดงเวลา (1m ago., etc.)
        holder.tvItemTime.text = if (notification.time.isNotEmpty()) notification.time else "Just now"

        // แสดงจุดแดงถ้ายังไม่อ่าน
        holder.viewUnreadDot.visibility = if (notification.isRead) View.GONE else View.VISIBLE
        
        // เปลี่ยนสีพื้นหลังถ้ายังไม่ได้อ่าน
        if (!notification.isRead) {
            holder.cardView.setCardBackgroundColor(0xFFF0F0FF.toInt())
        } else {
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF.toInt())
        }

        // ทำให้กดได้
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = notifications.size
}
