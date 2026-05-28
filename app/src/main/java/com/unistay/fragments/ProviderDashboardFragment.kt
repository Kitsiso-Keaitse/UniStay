package com.unistay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R

class ProviderDashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tvTotalListings: TextView
    private lateinit var tvAvailableListings: TextView
    private lateinit var tvReservedListings: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_provider_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvTotalListings = view.findViewById(R.id.tvTotalListings)
        tvAvailableListings = view.findViewById(R.id.tvAvailableListings)
        tvReservedListings = view.findViewById(R.id.tvReservedListings)

        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val providerId = auth.currentUser?.uid ?: return

        db.collection("accommodations")
            .whereEqualTo("providerId", providerId)
            .get()
            .addOnSuccessListener { documents ->
                val total = documents.size()
                val available = documents.documents.count { it.getString("status") == "available" }
                val reserved = documents.documents.count { it.getString("status") == "reserved" }

                tvTotalListings.text = total.toString()
                tvAvailableListings.text = available.toString()
                tvReservedListings.text = reserved.toString()
            }
    }
}