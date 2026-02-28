package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.MoveOutRequest

class AdminMoveOutAdapter(
    private val list: List<MoveOutRequest>,
    private val onItemClick: (MoveOutRequest) -> Unit
) : RecyclerView.Adapter<AdminMoveOutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoom: TextView = view.findViewById(R.id.tv_repair_room) // Reusing IDs for consistency if possible or creating new ones
        val tvUser: TextView = view.findViewById(R.id.tv_repair_type)
        val tvTime: TextView = view.findViewById(R.id.tv_repair_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_repair_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Using item_admin_repair.xml as a base for list items to maintain design consistency
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_repair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvRoom.text = "ห้อง ${item.roomNumber} แจ้งย้ายออก"
        holder.tvUser.text = "ผู้แจ้ง: ${item.userName}"
        holder.tvTime.text = "ย้ายออกวันที่: ${item.moveOutDate}"

        when (item.status) {
            "pending" -> {
                holder.tvStatus.text = "รอดำเนินการ"
                holder.tvStatus.setTextColor(0xFFFB8C00.toInt()) // Orange
                holder.tvStatus.setBackgroundColor(0xFFFFF3E0.toInt())
            }
            "approved" -> {
                holder.tvStatus.text = "อนุมัติแล้ว"
                holder.tvStatus.setTextColor(0xFF43A047.toInt()) // Green
                holder.tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
            }
            "rejected" -> {
                holder.tvStatus.text = "ปฏิเสธ"
                holder.tvStatus.setTextColor(0xFFE53935.toInt()) // Red
                holder.tvStatus.setBackgroundColor(0xFFFFEBEB.toInt())
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size
}
