package com.unistay.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.models.Accommodation
import java.util.*

class AddListingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDeposit: EditText
    private lateinit var spLocation: Spinner
    private lateinit var spType: Spinner
    private lateinit var etAvailableFrom: EditText
    private lateinit var btnSelectImage: Button
    private lateinit var ivPreview: ImageView
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var chipGroupAmenities: LinearLayout

    private val selectedAmenities = mutableListOf<String>()

    // List of placeholder images for property listings
    private val placeholderImages = listOf(
        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupSpinners()
        setupAmenityChips()
        setupListeners()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        etDeposit = findViewById(R.id.etDeposit)
        spLocation = findViewById(R.id.spLocation)
        spType = findViewById(R.id.spType)
        etAvailableFrom = findViewById(R.id.etAvailableFrom)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        ivPreview = findViewById(R.id.ivPreview)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        chipGroupAmenities = findViewById(R.id.chipGroupAmenities)
        
        btnSelectImage.text = "Random Placeholder Image"
    }

    private fun setupSpinners() {
        val locations = arrayOf("Select Location", "Gaborone West", "Broadhurst", "Tlokweng", "Mogoditshane", "Old Naledi", "Phakalane", "Block 6", "Block 8", "Phase 2")
        val types = arrayOf("Select Type", "Ensuite", "Self-Contained", "Single Room", "Shared Room", "Studio", "Flat", "Bachelor Flat")

        spLocation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
    }

    private fun setupAmenityChips() {
        val amenities = listOf("WiFi", "Water Included", "Electricity", "Parking", "Security", "Furnished", "Kitchen", "Laundry")

        for (amenity in amenities) {
            val chip = TextView(this).apply {
                text = amenity
                setPadding(24, 12, 24, 12)
                setBackgroundResource(R.drawable.bg_role_card)
                setTextColor(resources.getColor(R.color.white_40))
                textSize = 11f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
                setOnClickListener {
                    if (selectedAmenities.contains(amenity)) {
                        selectedAmenities.remove(amenity)
                        setBackgroundResource(R.drawable.bg_role_card)
                        setTextColor(resources.getColor(R.color.white_40))
                    } else {
                        selectedAmenities.add(amenity)
                        setBackgroundResource(R.drawable.bg_role_selected_student)
                        setTextColor(resources.getColor(R.color.blue_light))
                    }
                }
            }
            chipGroupAmenities.addView(chip)
        }
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener { 
            Toast.makeText(this, "Placeholders will be used automatically", Toast.LENGTH_SHORT).show()
        }

        btnSubmit.setOnClickListener { submitListing() }
    }

    private fun submitListing() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val price = etPrice.text.toString().toIntOrNull()
        val deposit = etDeposit.text.toString().toIntOrNull()
        val location = spLocation.selectedItem.toString()
        val type = spType.selectedItem.toString()
        val availableFrom = System.currentTimeMillis()
        val providerId = auth.currentUser?.uid ?: return

        if (title.isEmpty() || description.isEmpty() || price == null || deposit == null || location == "Select Location" || type == "Select Type") {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        btnSubmit.isEnabled = false

        // Pick a random placeholder instead of uploading
        val randomPlaceholder = placeholderImages[Random().nextInt(placeholderImages.size)]
        saveListingToFirestore(title, description, price, deposit, location, type, availableFrom, providerId, selectedAmenities, randomPlaceholder)
    }

    private fun saveListingToFirestore(
        title: String, description: String, price: Int, deposit: Int,
        location: String, type: String, availableFrom: Long, providerId: String,
        amenities: List<String>, imageUrl: String
    ) {
        val providerName = auth.currentUser?.email?.split("@")?.first() ?: "Provider"

        val accommodation = Accommodation(
            title = title,
            description = description,
            pricePerMonth = price,
            depositAmount = deposit,
            location = location,
            type = type,
            amenities = amenities,
            images = listOf(imageUrl),
            availableFrom = availableFrom,
            providerId = providerId,
            providerName = providerName,
            status = "available",
            createdAt = System.currentTimeMillis()
        )

        db.collection("accommodations").add(accommodation)
            .addOnSuccessListener {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Listing published successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = android.view.View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Failed to publish listing", Toast.LENGTH_SHORT).show()
            }
    }
}