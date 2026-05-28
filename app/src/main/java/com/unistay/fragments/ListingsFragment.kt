package com.unistay.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.unistay.models.FilterCriteria
import java.text.SimpleDateFormat
import java.util.*

class ListingsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccommodationAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val listings = mutableListOf<Accommodation>()
    private var currentFilter = FilterCriteria()
    private lateinit var btnFilter: Button
    
    private var tempSelectedDate: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewListings)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = AccommodationAdapter(
            listings,
            onItemClick = { accommodation ->
                val intent = Intent(requireContext(), ListingDetailActivity::class.java)
                intent.putExtra("accommodation_id", accommodation.id)
                startActivity(intent)
            },
            onChatClick = { accommodation ->
                openChatWithProvider(accommodation)
            }
        )
        recyclerView.adapter = adapter

        btnFilter = view.findViewById(R.id.btnFilter)
        btnFilter.setOnClickListener { showFilterDialog() }

        loadAllListings()
    }

    private fun openChatWithProvider(acc: Accommodation) {
        val currentUserId = auth.currentUser?.uid ?: return
        val providerId = acc.providerId
        if (providerId.isEmpty()) return

        val sorted = listOf(currentUserId, providerId).sorted()
        val threadId = "${sorted[0]}_${sorted[1]}_${acc.id}"

        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("thread_id", threadId)
        intent.putExtra("accommodation_title", acc.title)
        intent.putExtra("other_user_id", providerId)
        startActivity(intent)
    }

    private fun loadAllListings() {
        var query = db.collection("accommodations")
            .whereEqualTo("status", "available")

        if (currentFilter.minPrice > 0) {
            query = query.whereGreaterThanOrEqualTo("pricePerMonth", currentFilter.minPrice)
        }
        if (currentFilter.maxPrice < 10000) {
            query = query.whereLessThanOrEqualTo("pricePerMonth", currentFilter.maxPrice)
        }

        query.get()
            .addOnSuccessListener { documents ->
                listings.clear()
                for (doc in documents) {
                    val accommodation = doc.toObject(Accommodation::class.java)
                    accommodation.id = doc.id

                    var shouldInclude = true
                    if (currentFilter.locations.isNotEmpty()) {
                        shouldInclude = currentFilter.locations.contains(accommodation.location)
                    }
                    if (currentFilter.types.isNotEmpty() && shouldInclude) {
                        shouldInclude = currentFilter.types.contains(accommodation.type)
                    }
                    if (currentFilter.availableFrom > 0 && shouldInclude) {
                        shouldInclude = accommodation.availableFrom <= currentFilter.availableFrom
                    }

                    if (shouldInclude) {
                        listings.add(accommodation)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val etMinPrice = dialogView.findViewById<EditText>(R.id.etMinPrice)
        val etMaxPrice = dialogView.findViewById<EditText>(R.id.etMaxPrice)
        val spLocation = dialogView.findViewById<Spinner>(R.id.spLocation)
        val spType = dialogView.findViewById<Spinner>(R.id.spType)
        val btnPickDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyFilters)
        val btnReset = dialogView.findViewById<Button>(R.id.btnResetFilters)

        val locations = arrayOf("Any", "Gaborone West", "Broadhurst", "Tlokweng", "Mogoditshane", "Old Naledi", "Phakalane")
        val types = arrayOf("Any", "Ensuite", "Self-Contained", "Single", "Shared", "Studio", "Flat")

        spLocation.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
        spType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)

        if (currentFilter.minPrice > 0) etMinPrice.setText(currentFilter.minPrice.toString())
        if (currentFilter.maxPrice < 10000) etMaxPrice.setText(currentFilter.maxPrice.toString())
        
        tempSelectedDate = currentFilter.availableFrom
        if (tempSelectedDate > 0) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            btnPickDate.text = sdf.format(Date(tempSelectedDate))
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                tempSelectedDate = selectedCal.timeInMillis
                btnPickDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedCal.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnReset.setOnClickListener {
            currentFilter = FilterCriteria()
            loadAllListings()
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            currentFilter = FilterCriteria(
                minPrice = etMinPrice.text.toString().toIntOrNull() ?: 0,
                maxPrice = etMaxPrice.text.toString().toIntOrNull() ?: 10000,
                locations = if (spLocation.selectedItemPosition > 0) listOf(locations[spLocation.selectedItemPosition]) else emptyList(),
                types = if (spType.selectedItemPosition > 0) listOf(types[spType.selectedItemPosition]) else emptyList(),
                availableFrom = tempSelectedDate
            )
            loadAllListings()
            dialog.dismiss()
        }
        dialog.show()
    }
}
