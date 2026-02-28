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
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: MutableList<Notification>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView    = view as CardView
        val flIconBg: FrameLayout = view.findViewById(R.id.fl_icon_bg)
        val ivIcon: ImageView     = view.findViewById(R.id.iv_icon)
        val tvItemTitle: TextView = view.findViewById(R.id.tv_item_title)
        val tvItemMessage: TextView = view.findViewById(R.id.tv_item_message)
        val tvItemTime: TextView  = view.findViewById(R.id.tv_item_time)
        val viewUnreadDot: View   = view.findViewById(R.id.view_unread_dot)
        val viewUnreadBar: View   = view.findViewById(R.id.view_unread_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]

        // --- Title dynamic ---
        holder.tvItemTitle.text = notification.title.ifEmpty {
            when (notification.type) {
                "new_bill"          -> "บิลใหม่จากแอดมิน"
                "payment_approved"  -> "ชำระเงินสำเร็จ"
                "payment_rejected"  -> "สลิปถูกปฏิเสธ"
                "repair_update"     -> "อัปเดตสถานะแจ้งซ่อม"
                "chat"              -> "ข้อความจากแอดมิน"
                else                -> notification.senderName.ifEmpty { "การแจ้งเตือน" }
            }
        }

        // --- Message dynamic ---
        holder.tvItemMessage.text = notification.message.ifEmpty {
            when (notification.type) {
                "new_bill"          -> "มีบิลใหม่รอการชำระเงิน"
                "payment_approved"  -> "การชำระเงินของคุณได้รับการอนุมัติแล้ว"
                "payment_rejected"  -> "กรุณาตรวจสอบและส่งสลิปใหม่อีกครั้ง"
                "repair_update"     -> "แอดมินได้อัปเดตสถานะคำขอแจ้งซ่อมของคุณ"
                "chat"              -> "มีข้อความใหม่จากแอดมิน"
                else                -> ""
            }
        }

        // --- Icon + tint + bg circle ตาม type ---
        val (iconRes, iconTint, bgColor) = when (notification.type) {
            "payment_approved"  -> Triple(R.drawable.ic_bill_gg,   0xFF1B9E44.toInt(), 0xFFE8F5E9.toInt()) // green
            "payment_rejected"  -> Triple(R.drawable.ic_bill_gg,   0xFFE53935.toInt(), 0xFFFFEBEE.toInt()) // red
            "new_bill"          -> Triple(R.drawable.ic_bill_gg,   0xFF3D6FE8.toInt(), 0xFFEEF2FF.toInt()) // blue
            "repair_update"     -> Triple(R.drawable.ic_repair_gg, 0xFFF9A825.toInt(), 0xFFFFF8E1.toInt()) // amber
            "chat"              -> Triple(R.drawable.ic_chat_gg,   0xFF6C63FF.toInt(), 0xFFEDE7FF.toInt()) // purple
            else                -> Triple(R.drawable.ic_bell_gg,   0xFF3D6FE8.toInt(), 0xFFEEF2FF.toInt()) // blue
        }
        holder.ivIcon.setImageResource(iconRes)
        holder.ivIcon.setColorFilter(iconTint)
        holder.flIconBg.background = holder.flIconBg.context.getDrawable(R.drawable.bg_notif_icon_circle)
        holder.flIconBg.background?.setTint(bgColor)

        // --- เวลา ---
        holder.tvItemTime.text = formatTime(notification)

        // --- Unread indicators ---
        val isUnread = !notification.isRead
        holder.viewUnreadDot.visibility = if (isUnread) View.VISIBLE else View.GONE
        holder.viewUnreadBar.visibility = if (isUnread) View.VISIBLE else View.GONE

        // --- Card background ---
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
