package com.unistay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.adapters.NotificationAdapter
import com.unistay.models.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val notifications = mutableListOf<NotificationItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewNotifications)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(notifications) { notification ->
            // Handle notification click
            onNotificationClick(notification)
        }
        recyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        // Load notifications from Firestore
        db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { documents ->
                notifications.clear()
                for (doc in documents) {
                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    val type = doc.getString("type") ?: "info"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val isRead = doc.getBoolean("isRead") ?: false
                    val actionId = doc.getString("actionId") ?: ""
                    val actionType = doc.getString("actionType") ?: ""

                    val notification = NotificationItem(
                        notificationId = doc.id,
                        title = title,
                        message = message,
                        type = type,
                        timestamp = timestamp,
                        isRead = isRead,
                        actionId = actionId,
                        actionType = actionType
                    )
                    notifications.add(notification)

                    // Mark as read if not already
                    if (!isRead) {
                        doc.reference.update("isRead", true)
                    }
                }

                if (notifications.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load notifications"
                recyclerView.visibility = View.GONE
            }

        // Also check for saved filter matches and create notifications
        checkFilterMatches()
    }

    private fun checkFilterMatches() {
        val userId = auth.currentUser?.uid ?: return

        // Get user's saved filters
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val savedFilters = userDoc.get("savedFilters") as? Map<*, *>
                if (savedFilters != null) {
                    val minPrice = (savedFilters["minPrice"] as? Long)?.toInt() ?: 0
                    val maxPrice = (savedFilters["maxPrice"] as? Long)?.toInt() ?: 10000
                    val preferredLocations = savedFilters["locations"] as? List<String> ?: emptyList()
                    val preferredTypes = savedFilters["types"] as? List<String> ?: emptyList()

                    // Query matching new listings
                    val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)

                    db.collection("accommodations")
                        .whereGreaterThanOrEqualTo("pricePerMonth", minPrice)
                        .whereLessThanOrEqualTo("pricePerMonth", maxPrice)
                        .whereEqualTo("status", "available")
                        .whereGreaterThan("createdAt", threeDaysAgo)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (doc in documents) {
                                val listingLocation = doc.getString("location") ?: ""
                                val listingType = doc.getString("type") ?: ""

                                val locationMatch = preferredLocations.isEmpty() || preferredLocations.contains(listingLocation)
                                val typeMatch = preferredTypes.isEmpty() || preferredTypes.contains(listingType)

                                if (locationMatch && typeMatch) {
                                    // Check if notification already exists
                                    checkAndCreateNotification(
                                        userId,
                                        doc.id,
                                        doc.getString("title") ?: "New Listing",
                                        listingLocation,
                                        doc.getLong("pricePerMonth") ?: 0
                                    )
                                }
                            }
                        }
                }
            }
    }

    private fun checkAndCreateNotification(userId: String, listingId: String, title: String, location: String, price: Long) {
        db.collection("users").document(userId).collection("notifications")
            .whereEqualTo("actionId", listingId)
            .whereGreaterThan("timestamp", System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000))
            .get()
            .addOnSuccessListener { existing ->
                if (existing.isEmpty) {
                    // Create new notification
                    val notification = hashMapOf(
                        "title" to "🔔 New Match Found!",
                        "message" to "$title in $location is available for BWP $price/month - matches your saved filter.",
                        "type" to "match",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false,
                        "actionId" to listingId,
                        "actionType" to "listing"
                    )

                    db.collection("users").document(userId).collection("notifications")
                        .add(notification)
                        .addOnSuccessListener {
                            // Refresh notifications list
                            loadNotifications()
                        }
                }
            }
    }

    private fun onNotificationClick(notification: NotificationItem) {
        // Mark as read
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("notifications")
            .document(notification.notificationId)
            .update("isRead", true)

        // Handle navigation based on action type
        when (notification.actionType) {
            "listing" -> {
                // Navigate to listing detail
                val intent = android.content.Intent(requireContext(), com.unistay.activities.ListingDetailActivity::class.java)
                intent.putExtra("accommodation_id", notification.actionId)
                startActivity(intent)
            }
            "reservation" -> {
                // Navigate to receipt
                val intent = android.content.Intent(requireContext(), com.unistay.activities.ReceiptActivity::class.java)
                intent.putExtra("reservation_id", notification.actionId)
                startActivity(intent)
            }
            "chat" -> {
                // Navigate to chat
                val intent = android.content.Intent(requireContext(), com.unistay.activities.ChatActivity::class.java)
                intent.putExtra("thread_id", notification.actionId)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }
}