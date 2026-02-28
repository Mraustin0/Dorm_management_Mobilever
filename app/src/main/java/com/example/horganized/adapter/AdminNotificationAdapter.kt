package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.AdminNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminNotificationAdapter(
    private val list: List<AdminNotification>,
    private val onItemClick: (AdminNotification) -> Unit
) : RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val title: TextView = view.findViewById(R.id.tv_item_title)
        val message: TextView = view.findViewById(R.id.tv_item_message)
        val tvTime: TextView = view.findViewById(R.id.tv_item_time)
        val unreadDot: View = view.findViewById(R.id.view_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // Title: ใช้ title ใน doc ถ้ามี ถ้าไม่มีสร้างจาก type
        holder.title.text = item.title.ifEmpty {
            when (item.type) {
                "repair"  -> "แจ้งซ่อม: ห้อง ${item.roomNumber}"
                "payment" -> "แจ้งชำระเงิน: ห้อง ${item.roomNumber}"
                "chat"    -> "ข้อความจากห้อง ${item.roomNumber}"
                else      -> if (item.roomNumber.isNotEmpty()) "ห้อง ${item.roomNumber}" else "แจ้งเตือน"
            }
        }
        holder.message.text = item.message

        // เวลา: แสดงแบบ relative
        val tsDate: Date? = item.timestamp?.toDate()
        if (tsDate != null) {
            val diff = System.currentTimeMillis() - tsDate.time
            holder.tvTime.text = when {
                diff < 60_000L        -> "เพิ่งส่ง"
                diff < 3_600_000L     -> "${diff / 60_000} นาทีที่แล้ว"
                diff < 86_400_000L    -> "${diff / 3_600_000} ชั่วโมงที่แล้ว"
                else -> SimpleDateFormat("d MMM", Locale("th")).format(tsDate)
            }
        } else {
            holder.tvTime.text = ""
        }

        // จุดแดงถ้ายังไม่อ่าน
        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE
        holder.cardView.setCardBackgroundColor(
            if (item.isRead) 0xFFFFFFFF.toInt() else 0xFFF0F4FF.toInt()
        )

        // Icon ตาม type
        val iconRes = when (item.type) {
            "repair"  -> R.drawable.ic_repair_gg
            "payment" -> R.drawable.ic_bill_gg
            "chat"    -> R.drawable.ic_chat_gg
            else      -> R.drawable.ic_bell_gg
        }
        holder.icon.setImageResource(iconRes)
        holder.icon.clearColorFilter()   // ล้าง tint เดิมออก

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}