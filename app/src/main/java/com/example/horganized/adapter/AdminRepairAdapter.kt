package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.RepairRequest
import java.text.SimpleDateFormat
import java.util.*

class AdminRepairAdapter(
    private val list: MutableList<RepairRequest>,
    private val onItemClick: (RepairRequest) -> Unit
) : RecyclerView.Adapter<AdminRepairAdapter.ViewHolder>() {

    fun updateList(newList: List<RepairRequest>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoom: TextView = view.findViewById(R.id.tv_repair_room)
        val tvType: TextView = view.findViewById(R.id.tv_repair_type)
        val tvTime: TextView = view.findViewById(R.id.tv_repair_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_repair_status)
        val viewUnreadDot: View = view.findViewById(R.id.view_item_unread_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_repair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvRoom.text = "ห้อง ${item.roomNumber} แจ้งซ่อม"
        holder.tvType.text = "ประเภท: ${item.repairType}"
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("th", "TH"))
        holder.tvTime.text = sdf.format(Date(item.timestamp))

        // แสดงจุดแดงถ้ายังไม่อ่าน
        holder.viewUnreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE

        // จัดการสถานะและสี
        when (item.status) {
            "pending" -> {
                holder.tvStatus.text = "รอดำเนินการ"
                holder.tvStatus.setTextColor(0xFFE53935.toInt())
                holder.tvStatus.setBackgroundColor(0xFFFFEBEB.toInt())
            }
            "in_progress" -> {
                holder.tvStatus.text = "กำลังดำเนินการ"
                holder.tvStatus.setTextColor(0xFFFB8C00.toInt())
                holder.tvStatus.setBackgroundColor(0xFFFFF3E0.toInt())
            }
            "completed" -> {
                holder.tvStatus.text = "เสร็จสิ้น"
                holder.tvStatus.setTextColor(0xFF43A047.toInt())
                holder.tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
