package com.example.horganized.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.Notification

class NotificationAdapter(
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val tvNotifTitle: TextView = view.findViewById(R.id.tv_notif_title)
        val tvNotifBody: TextView = view.findViewById(R.id.tv_notif_body)
        val tvNotifTime: TextView = view.findViewById(R.id.tv_notif_time)
        val viewUnreadDot: View = view.findViewById(R.id.view_unread_dot)
        val flIcon: FrameLayout = view.findViewById(R.id.fl_icon)
        val ivNotifIcon: ImageView = view.findViewById(R.id.iv_notif_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.tvNotifTitle.text = if (notification.title.isNotEmpty()) notification.title else notification.senderName
        holder.tvNotifBody.text = notification.message

        holder.tvNotifTime.text = if (notification.timestamp > 0) {
            getTimeAgo(notification.timestamp)
        } else {
            notification.time
        }

        // Handle read/unread state
        if (!notification.isRead) {
            holder.viewUnreadDot.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(0xFFF0F0FF.toInt())
        } else {
            holder.viewUnreadDot.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun getItemCount() = notifications.size

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            weeks > 0 -> "${weeks}w ago"
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            else -> "Just now"
        }
    }
}
