package com.example.horganized.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.horganized.R
import java.text.SimpleDateFormat
import java.util.Locale

data class PendingSlip(
    val billId: String,
    val roomNumber: String,
    val amount: Double,
    val month: String,
    val slipUrl: String,
    val paymentDate: com.google.firebase.Timestamp?
)

class SlipAdapter(
    private val slips: List<PendingSlip>,
    private val onViewSlip: (PendingSlip) -> Unit,
    private val onApprove: (PendingSlip) -> Unit,
    private val onReject: (PendingSlip) -> Unit
) : RecyclerView.Adapter<SlipAdapter.SlipViewHolder>() {

    class SlipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoom: TextView = view.findViewById(R.id.tv_slip_room)
        val tvAmount: TextView = view.findViewById(R.id.tv_slip_amount)
        val tvMonth: TextView = view.findViewById(R.id.tv_slip_month)
        val tvDate: TextView = view.findViewById(R.id.tv_slip_date)
        val ivThumbnail: ImageView = view.findViewById(R.id.iv_slip_thumbnail)
        val btnViewSlip: AppCompatButton = view.findViewById(R.id.btn_view_slip)
        val btnApprove: AppCompatButton = view.findViewById(R.id.btn_approve)
        val btnReject: AppCompatButton = view.findViewById(R.id.btn_reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_slip, parent, false)
        return SlipViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlipViewHolder, position: Int) {
        val slip = slips[position]

        holder.tvRoom.text = "ห้อง ${slip.roomNumber}"
        holder.tvAmount.text = String.format("%,.0f บาท", slip.amount)
        holder.tvMonth.text = slip.month

        if (slip.paymentDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("th"))
            holder.tvDate.text = dateFormat.format(slip.paymentDate.toDate())
        } else {
            holder.tvDate.text = "-"
        }

        holder.btnViewSlip.setOnClickListener { onViewSlip(slip) }
        holder.btnApprove.setOnClickListener { onApprove(slip) }
        holder.btnReject.setOnClickListener { onReject(slip) }
    }

    override fun getItemCount() = slips.size
}
