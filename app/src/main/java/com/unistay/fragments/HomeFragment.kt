package com.unistay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.activities.ChatActivity
import com.unistay.activities.ListingDetailActivity
import com.unistay.adapters.AccommodationAdapter
import com.unistay.models.Accommodation

class HomeFragment : Fragment() {

    private lateinit var recyclerViewFeatured: RecyclerView
    private lateinit var recyclerViewNearby: RecyclerView
    private lateinit var adapterFeatured: AccommodationAdapter
    private lateinit var adapterNearby: AccommodationAdapter
    
    private lateinit var tvUserName: TextView
    private lateinit var tvAvailableCount: TextView
    private lateinit var tvReservedCount: TextView
    
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val featuredListings = mutableListOf<Accommodation>()
    private val nearbyListings = mutableListOf<Accommodation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews(view)
        setupRecyclerViews()
        
        loadUserInfo()
        listenToStats()
        listenToListings()
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvAvailableCount = view.findViewById(R.id.tvAvailableCount)
        tvReservedCount = view.findViewById(R.id.tvReservedCount)
        recyclerViewFeatured = view.findViewById(R.id.recyclerViewFeatured)
        recyclerViewNearby = view.findViewById(R.id.recyclerViewNearby)
    }

    private fun setupRecyclerViews() {
        // Featured Horizontal
        adapterFeatured = AccommodationAdapter(featuredListings, 
            onItemClick = { acc: Accommodation -> openDetail(acc.id) },
            onChatClick = { acc: Accommodation -> openChat(acc) }
        )
        recyclerViewFeatured.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewFeatured.adapter = adapterFeatured

        // Nearby Vertical
        adapterNearby = AccommodationAdapter(nearbyListings, 
            onItemClick = { acc: Accommodation -> openDetail(acc.id) },
            onChatClick = { acc: Accommodation -> openChat(acc) }
        )
        recyclerViewNearby.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewNearby.adapter = adapterNearby
    }

    private fun loadUserInfo() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val firstName = snapshot.getString("firstName") ?: ""
                val lastName = snapshot.getString("lastName") ?: ""
                tvUserName.text = "$firstName $lastName"
            }
        }
    }

    private fun listenToStats() {
        // Real-time count of available properties
        db.collection("accommodations")
            .whereEqualTo("status", "available")
            .addSnapshotListener { snapshots, _ ->
                tvAvailableCount.text = (snapshots?.size() ?: 0).toString()
            }

        // Real-time count of reserved properties
        db.collection("accommodations")
            .whereEqualTo("status", "reserved")
            .addSnapshotListener { snapshots, _ ->
                tvReservedCount.text = (snapshots?.size() ?: 0).toString()
            }
    }

    private fun listenToListings() {
        // Featured Listings
        db.collection("accommodations")
            .whereEqualTo("status", "available")
            .limit(10)
            .addSnapshotListener { snapshots, _ ->
                featuredListings.clear()
                snapshots?.forEach { doc ->
                    val acc = doc.toObject(Accommodation::class.java)
                    acc.id = doc.id
                    featuredListings.add(acc)
                }
                adapterFeatured.notifyDataSetChanged()
            }

        // Available Listings (Vertical)
        db.collection("accommodations")
            .whereEqualTo("status", "available")
            .limit(10)
            .addSnapshotListener { snapshots, _ ->
                nearbyListings.clear()
                snapshots?.forEach { doc ->
                    val acc = doc.toObject(Accommodation::class.java)
                    acc.id = doc.id
                    nearbyListings.add(acc)
                }
                adapterNearby.notifyDataSetChanged()
            }
    }

    private fun openDetail(id: String) {
        val intent = Intent(requireContext(), ListingDetailActivity::class.java)
        intent.putExtra("accommodation_id", id)
        startActivity(intent)
    }

    private fun openChat(acc: Accommodation) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (acc.providerId == currentUserId) {
            Toast.makeText(requireContext(), "You cannot chat with yourself", Toast.LENGTH_SHORT).show()
            return
        }

        val threadId = if (currentUserId < acc.providerId) "${currentUserId}_${acc.providerId}_${acc.id}" 
                       else "${acc.providerId}_${currentUserId}_${acc.id}"
        
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("thread_id", threadId)
        intent.putExtra("other_user_id", acc.providerId)
        intent.putExtra("accommodation_title", acc.title)
        intent.putExtra("accommodation_id", acc.id)
        startActivity(intent)
    }
}
