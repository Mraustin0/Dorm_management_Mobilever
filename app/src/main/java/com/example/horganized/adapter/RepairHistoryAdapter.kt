package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.RepairRequest
import java.text.SimpleDateFormat
import java.util.*

class RepairHistoryAdapter(
    private val list: List<RepairRequest>,
    private val onItemClick: (RepairRequest) -> Unit
) : RecyclerView.Adapter<RepairHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.tv_item_title)
        val status: TextView = view.findViewById(R.id.tv_item_message)
        val time: TextView = view.findViewById(R.id.tv_item_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ใช้ item_notification.xml เป็นต้นแบบความสวยงาม หรือสร้าง item_repair_history.xml
        // ในที่นี้ขอใช้ item_notification.xml เพื่อความรวดเร็วและสวยงามที่เหมือนกัน
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.type.text = "แจ้งซ่อม: ${item.repairType}"
        
        // จัดการสีตามสถานะ
        when (item.status) {
            "pending" -> {
                holder.status.text = "สถานะ: รอดำเนินการ"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            }
            "in_progress" -> {
                holder.status.text = "สถานะ: กำลังดำเนินการ"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark))
            }
            "completed" -> {
                holder.status.text = "สถานะ: ซ่อมเสร็จสิ้น"
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            }
        }

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("th", "TH"))
        holder.time.text = sdf.format(Date(item.timestamp))

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
