package com.unistay.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.unistay.R
import com.unistay.models.ChatThread

class ChatThreadAdapter(
    private val threads: List<ChatThread>,
    private val onItemClick: (ChatThread) -> Unit
) : RecyclerView.Adapter<ChatThreadAdapter.ThreadViewHolder>() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder {
        auth = FirebaseAuth.getInstance()
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_thread, parent, false)
        return ThreadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        val thread = threads[position]
        holder.bind(thread)
        holder.itemView.setOnClickListener { onItemClick(thread) }
    }

    override fun getItemCount() = threads.size

    inner class ThreadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvUnreadBadge: TextView = itemView.findViewById(R.id.tvUnreadBadge)

        fun bind(thread: ChatThread) {
            val currentUserId = auth.currentUser?.uid ?: return
            val otherParticipantName = thread.getOtherParticipantName(currentUserId)

            tvName.text = otherParticipantName
            tvLastMessage.text = thread.lastMessage
            tvTime.text = thread.getFormattedTime()  // ← This method now exists

            val unreadCount = thread.getUnreadCount(currentUserId)
            if (unreadCount > 0) {
                tvUnreadBadge.visibility = View.VISIBLE
                tvUnreadBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                tvUnreadBadge.visibility = View.GONE
            }
        }
    }
}