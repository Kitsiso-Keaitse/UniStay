package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.models.Accommodation
import com.unistay.adapters.AmenitiesAdapter
import java.text.SimpleDateFormat
import java.util.*

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var accommodationId = ""
    private var accommodation: Accommodation? = null
    private var currentUserId = ""

    // Views (using nullable for safety)
    private var ivImage: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvLocation: TextView? = null
    private var tvPrice: TextView? = null
    private var tvDeposit: TextView? = null
    private var tvAvailableFrom: TextView? = null
    private var tvStatus: TextView? = null
    private var tvDescription: TextView? = null
    private var tvProviderName: TextView? = null
    private var tvProviderRating: TextView? = null
    private var rvAmenities: RecyclerView? = null
    private var btnChat: Button? = null
    private var btnReserve: Button? = null
    private var btnBack: ImageView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        accommodationId = intent.getStringExtra("accommodation_id") ?: ""
        currentUserId = auth.currentUser?.uid ?: ""

        initViews()
        startListingListener()

        // Set click listeners
        btnBack?.setOnClickListener { finish() }
        btnChat?.setOnClickListener { openChat() }
        btnReserve?.setOnClickListener { proceedToPayment() }
    }

    private fun initViews() {
        ivImage = findViewById(R.id.ivListingImage)
        tvTitle = findViewById(R.id.tvTitle)
        tvLocation = findViewById(R.id.tvLocation)
        tvPrice = findViewById(R.id.tvPrice)
        tvDeposit = findViewById(R.id.tvDeposit)
        tvAvailableFrom = findViewById(R.id.tvAvailableFrom)
        tvStatus = findViewById(R.id.tvStatus)
        tvDescription = findViewById(R.id.tvDescription)
        tvProviderName = findViewById(R.id.tvProviderName)
        tvProviderRating = findViewById(R.id.tvProviderRating)
        rvAmenities = findViewById(R.id.rvAmenities)
        btnChat = findViewById(R.id.btnChat)
        btnReserve = findViewById(R.id.btnReserve)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        rvAmenities?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun startListingListener() {
        progressBar?.visibility = View.VISIBLE

        db.collection("accommodations").document(accommodationId)
            .addSnapshotListener { snapshot, error ->
                progressBar?.visibility = View.GONE
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                accommodation = snapshot.toObject(Accommodation::class.java)
                accommodation?.id = snapshot.id

                accommodation?.let { acc ->
                    displayListingDetails(acc)
                }
            }
    }

    private fun displayListingDetails(acc: Accommodation) {
        tvTitle?.text = acc.title
        tvLocation?.text = "📍 ${acc.location}"
        tvPrice?.text = "BWP ${acc.pricePerMonth}/month"
        tvDeposit?.text = "Deposit: BWP ${acc.depositAmount}"

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val availableDate = if (acc.availableFrom > 0) {
            dateFormat.format(Date(acc.availableFrom))
        } else {
            "Check with provider"
        }
        tvAvailableFrom?.text = "Available: $availableDate"
        tvDescription?.text = acc.description

        when (acc.status) {
            "available" -> {
                tvStatus?.text = "AVAILABLE"
                tvStatus?.setTextColor(resources.getColor(R.color.green_success, theme))
                btnReserve?.isEnabled = true
                btnReserve?.text = "Reserve Room"
            }
            "reserved" -> {
                tvStatus?.text = "RESERVED"
                tvStatus?.setTextColor(resources.getColor(R.color.red_error, theme))
                btnReserve?.isEnabled = false
                btnReserve?.text = "Already Reserved"
            }
            else -> {
                tvStatus?.text = acc.status.uppercase()
                btnReserve?.isEnabled = false
            }
        }

        // Load provider info
        if (acc.providerId.isNotEmpty()) {
            db.collection("users").document(acc.providerId).get()
                .addOnSuccessListener { providerDoc ->
                    val firstName = providerDoc.getString("firstName") ?: ""
                    val lastName = providerDoc.getString("lastName") ?: ""
                    tvProviderName?.text = if (firstName.isNotEmpty()) "$firstName $lastName" else acc.providerName
                    tvProviderRating?.text = "⭐ 4.8 Verified"
                }
        }

        // Setup amenities adapter
        if (acc.amenities.isNotEmpty()) {
            rvAmenities?.adapter = AmenitiesAdapter(acc.amenities)
        }
    }

    private fun openChat() {
        val providerId = accommodation?.providerId
        if (providerId.isNullOrEmpty()) return

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show()
            return
        }

        val threadId = if (currentUserId < providerId) "${currentUserId}_${providerId}_${accommodationId}"
                       else "${providerId}_${currentUserId}_${accommodationId}"

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("thread_id", threadId)
        intent.putExtra("accommodation_title", accommodation?.title ?: "")
        intent.putExtra("other_user_id", providerId)
        intent.putExtra("accommodation_id", accommodationId)
        startActivity(intent)
    }

    private fun proceedToPayment() {
        accommodation?.let { acc ->
            if (acc.status != "available") {
                Toast.makeText(this, "This listing is no longer available", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("accommodation_id", acc.id)
            intent.putExtra("accommodation_title", acc.title)
            intent.putExtra("deposit_amount", acc.depositAmount)
            intent.putExtra("monthly_rent", acc.pricePerMonth)
            startActivity(intent)
        }
    }
}
