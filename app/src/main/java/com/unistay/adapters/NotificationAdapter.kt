package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unistay.R
import com.unistay.models.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        holder.itemView.setOnClickListener { onItemClick(notification) }
    }

    override fun getItemCount() = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(notification: NotificationItem) {
            // Set icon based on type
            tvIcon.text = when (notification.type) {
                "match" -> "🔔"
                "reservation" -> "✅"
                "payment" -> "💰"
                "message" -> "💬"
                else -> "📌"
            }

            tvTitle.text = notification.title
            tvMessage.text = notification.message

            // Format time
            val diff = System.currentTimeMillis() - notification.timestamp
            val timeString = when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> {
                    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    dateFormat.format(Date(notification.timestamp))
                }
            }
            tvTime.text = timeString

            // Show unread dot
            viewUnreadDot.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Set background based on read status
            itemView.setBackgroundResource(
                if (notification.isRead) R.drawable.bg_card_listing
                else R.drawable.bg_notification_unread
            )
        }
    }
}