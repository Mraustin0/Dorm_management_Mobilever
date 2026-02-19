package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.AdminNotificationModel
import java.text.SimpleDateFormat
import java.util.*

class AdminNotificationAdapter(
    private val list: List<AdminNotificationModel>,
    private val onItemClick: (AdminNotificationModel) -> Unit
) : RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val title: TextView = view.findViewById(R.id.tv_item_title)
        val message: TextView = view.findViewById(R.id.tv_item_message)
        val time: TextView = view.findViewById(R.id.tv_item_time)
        val unreadDot: View = view.findViewById(R.id.view_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.title.text = item.title
        holder.message.text = item.message
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("th", "TH"))
        holder.time.text = sdf.format(Date(item.timestamp))

        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE
        
        if (!item.isRead) {
            holder.cardView.setCardBackgroundColor(0xFFF0F0FF.toInt())
        } else {
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF.toInt())
        }

        // เปลี่ยนสีไอคอนตามประเภท
        when (item.type) {
            "repair" -> holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark))
            "payment" -> holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            else -> holder.icon.setColorFilter(0xFF2D5496.toInt())
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}