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
import com.unistay.adapters.ReservationAdapter
import com.unistay.models.Reservation

class ProviderReservationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val reservations = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_provider_reservations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewReservations)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReservationAdapter(reservations)
        recyclerView.adapter = adapter

        loadReservations()
    }

    private fun loadReservations() {
        val providerId = auth.currentUser?.uid ?: return

        db.collection("reservations")
            .whereEqualTo("providerId", providerId)
            .orderBy("reservedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                reservations.clear()
                snapshots?.forEach { doc ->
                    val reservation = doc.toObject(Reservation::class.java)
                    reservation.reservationId = doc.id
                    reservations.add(reservation)
                }
                adapter.notifyDataSetChanged()
            }
    }
}