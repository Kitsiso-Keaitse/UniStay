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

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var threadId = ""
    private var otherUserId = ""
    private var accommodationTitle = ""
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        threadId = intent.getStringExtra("thread_id") ?: ""
        otherUserId = intent.getStringExtra("other_user_id") ?: ""
        accommodationTitle = intent.getStringExtra("accommodation_title") ?: ""

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        loadMessages()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        recyclerView = findViewById(R.id.recyclerViewChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        tvTitle.text = accommodationTitle
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
        val currentUserName = currentUser.email?.split("@")?.first() ?: "User"

        val chatMessage = ChatMessage(
            threadId = threadId,
            senderId = currentUser.uid,
            senderName = currentUserName,
            receiverId = otherUserId,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        // 1. Save the message
        db.collection("chatMessages").add(chatMessage)
            .addOnSuccessListener {
                etMessage.text.clear()
                
                // 2. Update the ChatThread for the list view
                updateChatThread(message, currentUser.uid)
            }
    }

    private fun updateChatThread(lastMsg: String, senderId: String) {
        val threadRef = db.collection("chatThreads").document(threadId)
        
        val threadUpdate = mapOf(
            "lastMessage" to lastMsg,
            "lastMessageTime" to System.currentTimeMillis(),
            "lastMessageSenderId" to senderId,
            "participants" to listOf(senderId, otherUserId),
            "accommodationTitle" to accommodationTitle
        )

        threadRef.set(threadUpdate, com.google.firebase.firestore.SetOptions.merge())
    }

    private fun loadMessages() {
        db.collection("chatMessages")
            .whereEqualTo("threadId", threadId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                messages.clear()
                snapshots?.forEach { doc ->
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
