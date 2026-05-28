package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unistay.R
import com.unistay.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 1) {
            R.layout.item_chat_sent
        } else {
            R.layout.item_chat_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.message

            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = dateFormat.format(Date(message.timestamp))
        }
    }
}