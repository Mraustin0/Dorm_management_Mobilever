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
import java.util.Date
import java.util.Locale

class NotificationAdapter(private val list: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val title: TextView = view.findViewById(R.id.tv_item_title)
        val message: TextView = view.findViewById(R.id.tv_item_message)
        val tvTime: TextView = view.findViewById(R.id.tv_item_time)   // เปลี่ยนจาก time → tvTime
        val unreadDot: View = view.findViewById(R.id.view_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.title.ifEmpty { item.senderName }
        holder.message.text = item.message

        // Icon ตาม type
        val iconRes = when (item.type) {
            "payment_approved", "payment_rejected", "new_bill" -> R.drawable.ic_bill_gg
            "repair_update" -> R.drawable.ic_repair_gg
            "chat"          -> R.drawable.ic_chat_gg
            else            -> R.drawable.ic_bell_gg
        }
        holder.icon.setImageResource(iconRes)

        // เวลา: ใช้ firestoreTimestamp ก่อน ถ้าไม่มีใช้ timestamp (Long)
        val timeMs: Long? = when {
            item.firestoreTimestamp != null -> item.firestoreTimestamp.toDate().time
            item.timestamp > 0              -> item.timestamp
            else                            -> null
        }
        holder.tvTime.text = if (timeMs != null) formatTime(timeMs) else ""

        // จุดแดงถ้ายังไม่อ่าน
        holder.unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = list.size

    private fun formatTime(timeMs: Long): String {
        val diff = System.currentTimeMillis() - timeMs
        return when {
            diff < 60_000L         -> "เพิ่งส่ง"
            diff < 3_600_000L      -> "${diff / 60_000} นาทีที่แล้ว"
            diff < 86_400_000L     -> "${diff / 3_600_000} ชั่วโมงที่แล้ว"
            diff < 7 * 86_400_000L -> "${diff / 86_400_000} วันที่แล้ว"
            else -> SimpleDateFormat("d MMM yy", Locale("th")).format(Date(timeMs))
        }
    }
}