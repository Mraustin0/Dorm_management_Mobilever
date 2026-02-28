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
import java.text.SimpleDateFormat
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
        val viewUnreadBar: View = view.findViewById(R.id.view_unread_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]

        // Title: ใช้ title ถ้ามี ถ้าไม่มีใช้ senderName
        holder.tvItemTitle.text = notification.title.ifEmpty { notification.senderName }
        holder.tvItemMessage.text = notification.message

        // Icon + tint ตาม type
        val (iconRes, iconTint) = when (notification.type) {
            "payment_approved"  -> Pair(R.drawable.ic_bill_gg, 0xFF1B9E44.toInt())   // green
            "payment_rejected"  -> Pair(R.drawable.ic_bill_gg, 0xFFE53935.toInt())   // red
            "new_bill"          -> Pair(R.drawable.ic_bill_gg, 0xFF3D6FE8.toInt())   // blue
            "repair_update"     -> Pair(R.drawable.ic_repair_gg, 0xFFF9A825.toInt()) // amber
            "chat"              -> Pair(R.drawable.ic_chat_gg, 0xFF6C63FF.toInt())   // purple
            else                -> Pair(R.drawable.ic_bell_gg, 0xFF3D6FE8.toInt())   // blue
        }
        holder.ivIcon.setImageResource(iconRes)
        holder.ivIcon.setColorFilter(iconTint)

        // เวลา
        holder.tvItemTime.text = formatTime(notification)

        // Unread indicators
        val isUnread = !notification.isRead
        holder.viewUnreadDot.visibility = if (isUnread) View.VISIBLE else View.GONE
        holder.viewUnreadBar.visibility = if (isUnread) View.VISIBLE else View.GONE

        // สีพื้นหลัง card
        holder.cardView.setCardBackgroundColor(
            if (isUnread) 0xFFF0F4FF.toInt() else 0xFFFFFFFF.toInt()
        )

        holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = notifications.size

    private fun formatTime(notification: Notification): String {
        val timeMs: Long = when {
            notification.firestoreTimestamp != null ->
                notification.firestoreTimestamp.toDate().time
            notification.timestamp > 0 ->
                notification.timestamp
            else -> return "เพิ่งส่ง"
        }

        val diff = System.currentTimeMillis() - timeMs
        return when {
            diff < 60_000            -> "เพิ่งส่ง"
            diff < 3_600_000         -> "${diff / 60_000} นาทีที่แล้ว"
            diff < 86_400_000        -> "${diff / 3_600_000} ชั่วโมงที่แล้ว"
            diff < 7 * 86_400_000    -> "${diff / 86_400_000} วันที่แล้ว"
            else -> SimpleDateFormat("d MMM yy", Locale("th")).format(Date(timeMs))
        }
    }
}
