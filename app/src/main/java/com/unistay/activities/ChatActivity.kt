package com.unistay.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unistay.R
import com.unistay.adapters.ChatAdapter
import com.unistay.models.ChatMessage
import com.unistay.models.ChatThread

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnBack: ImageView
    private var tvChatTitle: TextView? = null
    private var tvAccommodationInfo: TextView? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var threadId = ""
    private var otherUserId = ""
    private var accommodationTitle = ""
    private var accommodationId = ""
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        threadId = intent.getStringExtra("thread_id") ?: ""
        otherUserId = intent.getStringExtra("other_user_id") ?: ""
        accommodationTitle = intent.getStringExtra("accommodation_title") ?: ""
        accommodationId = intent.getStringExtra("accommodation_id") ?: ""

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        loadMessages()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvChatTitle = findViewById(R.id.tvChatTitle)
        tvAccommodationInfo = findViewById(R.id.tvAccommodationInfo)
        recyclerView = findViewById(R.id.recyclerViewChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        tvChatTitle?.text = if (accommodationTitle.isNotEmpty()) accommodationTitle else "Chat"
        tvAccommodationInfo?.text = accommodationTitle

        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(messages, auth.currentUser?.uid ?: "")
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(message: String) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        val timestamp = System.currentTimeMillis()

        val chatMessage = ChatMessage(
            threadId = threadId,
            senderId = currentUserId,
            senderName = "User", 
            receiverId = otherUserId,
            message = message,
            timestamp = timestamp,
            isRead = false
        )

        db.collection("chatMessages").add(chatMessage)
            .addOnSuccessListener {
                etMessage.text.clear()
                
                val threadUpdate = hashMapOf(
                    "threadId" to threadId,
                    "lastMessage" to message,
                    "lastMessageTime" to timestamp,
                    "lastMessageSenderId" to currentUserId,
                    "participants" to listOf(currentUserId, otherUserId),
                    "accommodationTitle" to accommodationTitle,
                    "accommodationId" to accommodationId
                )
                
                db.collection("chatThreads").document(threadId).set(threadUpdate, com.google.firebase.firestore.SetOptions.merge())
            }
    }

    private fun loadMessages() {
        if (threadId.isEmpty()) return

        db.collection("chatMessages")
            .whereEqualTo("threadId", threadId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshots != null) {
                    messages.clear()
                    snapshots.forEach { doc ->
                        val msg = doc.toObject(ChatMessage::class.java)
                        messages.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        recyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }
}
