package com.unistay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.activities.ChatActivity
import com.unistay.adapters.ChatThreadAdapter
import com.unistay.models.ChatThread

class ChatListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatThreadAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val threads = mutableListOf<ChatThread>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewChats)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatThreadAdapter(threads) { thread ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("thread_id", thread.threadId)
            intent.putExtra("accommodation_title", thread.accommodationTitle)
            intent.putExtra("other_user_id", thread.participants.first { it != auth.currentUser?.uid })
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadChatThreads()
    }

    private fun loadChatThreads() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("chatThreads")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                threads.clear()
                snapshots?.forEach { doc ->
                    val thread = doc.toObject(ChatThread::class.java)
                    thread.threadId = doc.id
                    threads.add(thread)
                }
                adapter.notifyDataSetChanged()
            }
    }
}