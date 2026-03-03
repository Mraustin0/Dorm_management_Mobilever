package com.example.horganized.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.horganized.R
import com.example.horganized.model.Announcement

class AnnouncementAdapter(
    private val announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.iv_announcement_image)
        val tvTitle: TextView = view.findViewById(R.id.tv_announcement_title)
        val tvDetail: TextView = view.findViewById(R.id.tv_announcement_detail)
        val tvDate: TextView = view.findViewById(R.id.tv_announcement_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = announcements[position]

        if (item.imageUrl.isNotEmpty()) {
            holder.ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.ivImage.setBackgroundResource(R.drawable.bg_announcement_placeholder)
            Glide.with(holder.ivImage.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_announcement_placeholder)
                .error(R.drawable.ic_announcement_gg)
                .centerCrop()
                .into(holder.ivImage)
            holder.ivImage.visibility = View.VISIBLE
        } else {
            holder.ivImage.setBackgroundResource(R.drawable.bg_announcement_placeholder)
            holder.ivImage.setImageResource(R.drawable.ic_announcement_gg)
            holder.ivImage.scaleType = ImageView.ScaleType.CENTER
            holder.ivImage.visibility = View.VISIBLE
        }

        holder.tvTitle.text = item.title

        if (item.detail.isNotEmpty()) {
            holder.tvDetail.text = item.detail
            holder.tvDetail.visibility = View.VISIBLE
        } else {
            holder.tvDetail.visibility = View.GONE
        }

        if (item.date.isNotEmpty()) {
            holder.tvDate.text = item.date
            holder.tvDate.visibility = View.VISIBLE
        } else {
            holder.tvDate.visibility = View.GONE
        }
    }

    override fun getItemCount() = announcements.size
}
