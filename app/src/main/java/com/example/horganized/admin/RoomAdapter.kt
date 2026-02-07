package com.example.horganized.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.horganized.R

data class Room(val name: String, val isVacant: Boolean)

class RoomAdapter(private val rooms: List<Room>, private val onItemClick: (Room) -> Unit) :
    RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvRoom: CardView = view.findViewById(R.id.cv_room)
        val ivStatus: ImageView = view.findViewById(R.id.iv_room_status)
        val tvName: TextView = view.findViewById(R.id.tv_room_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        val context = holder.itemView.context
        holder.tvName.text = room.name
        
        if (room.isVacant) {
            // ห้องว่าง (สีเขียว) - ไอคอนและตัวหนังสือสีดำตามภาพ
            holder.cvRoom.setCardBackgroundColor(ContextCompat.getColor(context, R.color.room_vacant))
            holder.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.black))
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            // ห้องมีคนพัก (สีแดง) - ไอคอนและตัวหนังสือสีขาวตามภาพ
            holder.cvRoom.setCardBackgroundColor(ContextCompat.getColor(context, R.color.room_occupied))
            holder.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.white))
            holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.white))
        }

        holder.itemView.setOnClickListener { onItemClick(room) }
    }

    override fun getItemCount() = rooms.size
}