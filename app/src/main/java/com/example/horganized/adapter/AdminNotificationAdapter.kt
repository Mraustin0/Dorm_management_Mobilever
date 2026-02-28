package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
        val cardView: CardView   = view as CardView
        val flIconBg: FrameLayout = view.findViewById(R.id.fl_icon_bg)
        val icon: ImageView      = view.findViewById(R.id.iv_icon)
        val title: TextView      = view.findViewById(R.id.tv_item_title)
        val message: TextView    = view.findViewById(R.id.tv_item_message)
        val tvTime: TextView     = view.findViewById(R.id.tv_item_time)
        val unreadDot: View      = view.findViewById(R.id.view_unread_dot)
        val unreadBar: View      = view.findViewById(R.id.view_unread_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // --- Title dynamic ---
        holder.title.text = item.title.ifEmpty {
            when (item.type) {
                "repair"  -> "แจ้งซ่อม จากห้อง ${item.roomNumber}"
                "payment" -> "แจ้งชำระเงิน จากห้อง ${item.roomNumber}"
                "chat"    -> "ข้อความ จากห้อง ${item.roomNumber}"
                else      -> if (item.roomNumber.isNotEmpty()) "ห้อง ${item.roomNumber}" else "การแจ้งเตือน"
            }
        }

        // --- Message dynamic ---
        holder.message.text = item.message.ifEmpty {
            when (item.type) {
                "repair"  -> "ลูกหอส่งคำขอแจ้งซ่อมใหม่"
                "payment" -> "ลูกหอแนบสลิปการชำระเงิน"
                "chat"    -> "มีข้อความใหม่รอการตอบกลับ"
                else      -> ""
            }
        }

        // --- Icon + tint + bg circle color ตาม type ---
        val (iconRes, iconTint, bgColor) = when (item.type) {
            "repair"  -> Triple(R.drawable.ic_repair_gg, 0xFFF9A825.toInt(), 0xFFFFF8E1.toInt()) // amber
            "payment" -> Triple(R.drawable.ic_bill_gg,   0xFF1B9E44.toInt(), 0xFFE8F5E9.toInt()) // green
            "chat"    -> Triple(R.drawable.ic_chat_gg,   0xFF6C63FF.toInt(), 0xFFEDE7FF.toInt()) // purple
            else      -> Triple(R.drawable.ic_bell_gg,   0xFF3D6FE8.toInt(), 0xFFEEF2FF.toInt()) // blue
        }
        holder.icon.setImageResource(iconRes)
        holder.icon.setColorFilter(iconTint)
        holder.flIconBg.setBackgroundColor(0)  // clear first
        holder.flIconBg.background = holder.flIconBg.context.getDrawable(R.drawable.bg_notif_icon_circle)
        holder.flIconBg.background?.setTint(bgColor)

        // --- เวลา relative ---
        val tsDate: Date? = item.timestamp?.toDate()
        holder.tvTime.text = if (tsDate != null) {
            val diff = System.currentTimeMillis() - tsDate.time
            when {
                diff < 60_000L         -> "เพิ่งส่ง"
                diff < 3_600_000L      -> "${diff / 60_000} นาทีที่แล้ว"
                diff < 86_400_000L     -> "${diff / 3_600_000} ชั่วโมงที่แล้ว"
                diff < 7 * 86_400_000L -> "${diff / 86_400_000} วันที่แล้ว"
                else -> SimpleDateFormat("d MMM", Locale("th")).format(tsDate)
            }
        } else ""

        // --- Unread indicators ---
        val isUnread = !item.isRead
        holder.unreadDot.visibility = if (isUnread) View.VISIBLE else View.GONE
        holder.unreadBar.visibility = if (isUnread) View.VISIBLE else View.GONE

        // --- Card background ---
        holder.cardView.setCardBackgroundColor(
            if (isUnread) 0xFFF0F4FF.toInt() else 0xFFFFFFFF.toInt()
        )

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
