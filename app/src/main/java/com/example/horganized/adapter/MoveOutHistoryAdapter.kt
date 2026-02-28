package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.MoveOutRequest

class MoveOutHistoryAdapter(private val list: List<MoveOutRequest>) :
    RecyclerView.Adapter<MoveOutHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoom: TextView = view.findViewById(R.id.tv_history_room)
        val tvStatus: TextView = view.findViewById(R.id.tv_history_status)
        val tvNotifyDate: TextView = view.findViewById(R.id.tv_history_notify_date)
        val tvMoveOutDate: TextView = view.findViewById(R.id.tv_history_move_out_date)
        val layoutRefund: LinearLayout = view.findViewById(R.id.layout_refund_details)
        val tvRefundAmount: TextView = view.findViewById(R.id.tv_refund_amount)
        val tvDamageFee: TextView = view.findViewById(R.id.tv_damage_fee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_move_out_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvRoom.text = "ห้อง ${item.roomNumber}"
        holder.tvNotifyDate.text = item.notifyDate
        holder.tvMoveOutDate.text = item.moveOutDate

        when (item.status) {
            "pending" -> {
                holder.tvStatus.text = "รอดำเนินการ"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
                holder.tvStatus.setBackgroundResource(R.drawable.shape_status_pending) // Make sure this exists or use a color
                holder.layoutRefund.visibility = View.GONE
            }
            "approved" -> {
                holder.tvStatus.text = "อนุมัติแล้ว"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
                holder.layoutRefund.visibility = View.VISIBLE
                holder.tvRefundAmount.text = String.format("%,.0f บาท", item.refundAmount)
                holder.tvDamageFee.text = String.format("%,.0f บาท", item.damageFee)
            }
            "rejected" -> {
                holder.tvStatus.text = "ปฏิเสธ"
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
                holder.layoutRefund.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = list.size
}
