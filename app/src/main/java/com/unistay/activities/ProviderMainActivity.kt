package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.fragments.ProviderDashboardFragment
import com.unistay.fragments.ProviderListingsFragment
import com.unistay.fragments.ChatListFragment
import com.unistay.fragments.ProviderReservationsFragment
import com.unistay.fragments.ProfileFragment

class ProviderMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        bottomNav = findViewById(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProviderDashboardFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(ProviderDashboardFragment())
                    true
                }
                R.id.nav_listings -> {
                    loadFragment(ProviderListingsFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatListFragment())
                    true
                }
                R.id.nav_reservations -> {
                    loadFragment(ProviderReservationsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Check if provider is verified
        checkProviderVerification()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun checkProviderVerification() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val isVerified = document.getBoolean("isVerified") ?: false
                if (!isVerified) {
                    Toast.makeText(
                        this,
                        "Your provider account is pending verification. You'll be notified when approved.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to activity
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is ProviderDashboardFragment) {
                (fragment as ProviderDashboardFragment).refreshData()
            }
        }
    }
}