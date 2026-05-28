package com.unistay.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.fragments.HomeFragment
import com.unistay.fragments.ListingsFragment
import com.unistay.fragments.ChatListFragment
import com.unistay.fragments.NotificationsFragment
import com.unistay.fragments.ProfileFragment
import com.google.firebase.firestore.DocumentChange
import com.unistay.utils.NotificationHelper

class StudentMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        notificationHelper = NotificationHelper(this)

        bottomNav = findViewById(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_listings -> loadFragment(ListingsFragment())
                R.id.nav_chat -> loadFragment(ChatListFragment())
                R.id.nav_notifications -> loadFragment(NotificationsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                else -> false
            }
        }

        listenForMatchingListings()
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun listenForMatchingListings() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Listen for user's saved filters
        db.collection("users").document(userId).addSnapshotListener { userSnapshot, _ ->
            val savedFilters = userSnapshot?.get("savedFilters") as? Map<*, *> ?: return@addSnapshotListener
            
            val minPrice = (savedFilters["minPrice"] as? Long)?.toInt() ?: 0
            val maxPrice = (savedFilters["maxPrice"] as? Long)?.toInt() ?: 100000
            val preferredLocations = savedFilters["locations"] as? List<String> ?: emptyList()

            // 2. Listen for NEW available accommodations that match
            db.collection("accommodations")
                .whereEqualTo("status", "available")
                .whereGreaterThanOrEqualTo("pricePerMonth", minPrice)
                .addSnapshotListener { snapshots, _ ->
                    snapshots?.documentChanges?.forEach { dc ->
                        if (docChangeMatches(dc, maxPrice, preferredLocations)) {
                            val title = dc.document.getString("title") ?: "Listing"
                            val price = dc.document.getLong("pricePerMonth")
                            notificationHelper.showNotification(
                                "New Match Found!",
                                "$title is now available for BWP $price/month"
                            )
                        }
                    }
                }
        }
    }

    private fun docChangeMatches(dc: DocumentChange, maxPrice: Int, preferredLocations: List<String>): Boolean {
        if (dc.type != DocumentChange.Type.ADDED) return false
        
        val doc = dc.document
        val price = doc.getLong("pricePerMonth")?.toInt() ?: 0
        val location = doc.getString("location") ?: ""
        
        return price <= maxPrice && (preferredLocations.isEmpty() || preferredLocations.contains(location))
    }
}
