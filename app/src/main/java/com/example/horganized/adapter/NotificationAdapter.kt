package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val list: List<Notification>) : 
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        
        // กำหนดข้อมูล
        holder.title.text = item.title.ifEmpty { item.senderName }
        holder.message.text = item.message
        
        // การจัดการเวลา
        if (item.time.isNotEmpty()) {
            holder.time.text = item.time
        } else {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("th", "TH"))
            holder.time.text = sdf.format(Date(item.timestamp))
        }

        // แสดงจุดแดงถ้ายังไม่อ่าน
        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE
        
        // สามารถเปลี่ยน Icon ตามประเภทได้ที่นี่ (ตัวอย่าง)
        // holder.icon.setImageResource(R.drawable.ic_list)
    }

    override fun getItemCount() = list.size
}