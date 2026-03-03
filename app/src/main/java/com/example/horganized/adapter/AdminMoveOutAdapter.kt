package com.example.horganized.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R
import com.example.horganized.model.MoveOutRequest

class AdminMoveOutAdapter(
    private val list: List<MoveOutRequest>,
    private val onApprove: (MoveOutRequest) -> Unit,
    private val onReject: (MoveOutRequest) -> Unit
) : RecyclerView.Adapter<AdminMoveOutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoomUser: TextView         = view.findViewById(R.id.tv_move_room_user)
        val tvNotifyDate: TextView       = view.findViewById(R.id.tv_move_notify_date)
        val tvMoveOutDate: TextView      = view.findViewById(R.id.tv_move_out_date)
        val tvStatus: TextView           = view.findViewById(R.id.tv_move_status)
        val llActions: LinearLayout      = view.findViewById(R.id.ll_move_actions)
        val btnApprove: AppCompatButton  = view.findViewById(R.id.btn_approve_move)
        val btnReject: AppCompatButton   = view.findViewById(R.id.btn_reject_move)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_move_out_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvRoomUser.text    = "ห้อง ${item.roomNumber}  ·  ${item.userName}"
        holder.tvNotifyDate.text  = "วันที่แจ้ง: ${item.notifyDate}"
        holder.tvMoveOutDate.text = "วันที่ย้ายออก: ${item.moveOutDate}"

        when (item.status) {
            "pending" -> {
                holder.tvStatus.text = "รอดำเนินการ"
                holder.tvStatus.setTextColor(0xFFFB8C00.toInt())
                holder.tvStatus.setBackgroundColor(0xFFFFF3E0.toInt())
                holder.llActions.visibility = View.VISIBLE
            }
            "approved" -> {
                holder.tvStatus.text = "อนุมัติแล้ว"
                holder.tvStatus.setTextColor(0xFF43A047.toInt())
                holder.tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                holder.llActions.visibility = View.GONE
            }
            "rejected" -> {
                holder.tvStatus.text = "ปฏิเสธ"
                holder.tvStatus.setTextColor(0xFFE53935.toInt())
                holder.tvStatus.setBackgroundColor(0xFFFFEBEB.toInt())
                holder.llActions.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.text = item.status
                holder.llActions.visibility = View.GONE
            }
        }

        holder.btnApprove.setOnClickListener { onApprove(item) }
        holder.btnReject.setOnClickListener  { onReject(item) }
    }

    override fun getItemCount() = list.size
}
