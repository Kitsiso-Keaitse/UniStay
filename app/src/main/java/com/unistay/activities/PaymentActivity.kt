package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.models.Reservation
import com.unistay.utils.PaymentSimulator

class PaymentActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var accommodationId = ""
    private var accommodationTitle = ""
    private var depositAmount = 0
    private var monthlyRent = 0
    private var adminFee = 150
    private var totalAmount = 0

    private lateinit var tvAccommodationTitle: TextView
    private lateinit var tvMonthlyRent: TextView
    private lateinit var tvDeposit: TextView
    private lateinit var tvAdminFee: TextView
    private lateinit var tvTotal: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnPay: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        accommodationId = intent.getStringExtra("accommodation_id") ?: ""
        accommodationTitle = intent.getStringExtra("accommodation_title") ?: ""
        depositAmount = intent.getIntExtra("deposit_amount", 0)
        monthlyRent = intent.getIntExtra("monthly_rent", 0)
        totalAmount = depositAmount + adminFee

        initViews()
        displayPaymentSummary()
        setupListeners()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        tvAccommodationTitle = findViewById(R.id.tvAccommodationTitle)
        tvMonthlyRent = findViewById(R.id.tvMonthlyRent)
        tvDeposit = findViewById(R.id.tvDeposit)
        tvAdminFee = findViewById(R.id.tvAdminFee)
        tvTotal = findViewById(R.id.tvTotal)
        radioGroup = findViewById(R.id.radioGroupPayment)
        btnPay = findViewById(R.id.btnPay)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun displayPaymentSummary() {
        tvAccommodationTitle.text = accommodationTitle
        tvMonthlyRent.text = "BWP $monthlyRent"
        tvDeposit.text = "BWP $depositAmount"
        tvAdminFee.text = "BWP $adminFee"
        tvTotal.text = "BWP $totalAmount"
    }

    private fun setupListeners() {
        btnPay.setOnClickListener { 
            // Final check before processing payment
            checkAvailabilityAndPay()
        }
    }

    private fun checkAvailabilityAndPay() {
        progressBar.visibility = android.view.View.VISIBLE
        btnPay.isEnabled = false

        db.collection("accommodations").document(accommodationId).get()
            .addOnSuccessListener { doc ->
                val status = doc.getString("status") ?: "available"
                if (status == "available") {
                    processPayment()
                } else {
                    progressBar.visibility = android.view.View.GONE
                    btnPay.isEnabled = true
                    Toast.makeText(this, "Sorry, this room was just reserved by someone else.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = android.view.View.GONE
                btnPay.isEnabled = true
                Toast.makeText(this, "Connection error. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processPayment() {
        val selectedId = radioGroup.checkedRadioButtonId
        val paymentMethod = when (selectedId) {
            R.id.rbOrangeMoney -> "Orange Money"
            R.id.rbCard -> "Credit/Debit Card"
            R.id.rbBank -> "Bank Transfer"
            else -> {
                progressBar.visibility = android.view.View.GONE
                btnPay.isEnabled = true
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Simulate payment processing
        Handler(Looper.getMainLooper()).postDelayed({
            val result = PaymentSimulator.processPayment(totalAmount, paymentMethod)

            if (result.success) {
                createReservation(result.referenceNumber, paymentMethod)
            } else {
                progressBar.visibility = android.view.View.GONE
                btnPay.isEnabled = true
                Toast.makeText(this, "Payment failed: ${result.message}", Toast.LENGTH_SHORT).show()
            }
        }, 2000)
    }

    private fun createReservation(referenceNumber: String, paymentMethod: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""

                db.collection("accommodations").document(accommodationId).get()
                    .addOnSuccessListener { accDoc ->
                        val providerId = accDoc.getString("providerId") ?: ""

                        val reservation = Reservation(
                            accommodationId = accommodationId,
                            accommodationTitle = accommodationTitle,
                            studentId = userId,
                            studentName = "$firstName $lastName",
                            providerId = providerId,
                            amountPaid = totalAmount,
                            depositAmount = depositAmount,
                            referenceNumber = referenceNumber,
                            paymentMethod = paymentMethod,
                            status = "confirmed",
                            moveInDate = System.currentTimeMillis(),
                            reservedAt = System.currentTimeMillis()
                        )

                        // Atomic batch write for consistency
                        val batch = db.batch()
                        val resRef = db.collection("reservations").document()
                        val accRef = db.collection("accommodations").document(accommodationId)
                        
                        batch.set(resRef, reservation)
                        batch.update(accRef, mapOf(
                            "status" to "reserved",
                            "reservedBy" to userId,
                            "reservedAt" to System.currentTimeMillis()
                        ))

                        batch.commit().addOnSuccessListener {
                            progressBar.visibility = android.view.View.GONE
                            val intent = Intent(this, ReceiptActivity::class.java)
                            intent.putExtra("reservation_id", resRef.id)
                            intent.putExtra("reference_number", referenceNumber)
                            intent.putExtra("accommodation_title", accommodationTitle)
                            intent.putExtra("amount_paid", totalAmount)
                            startActivity(intent)
                            finish()
                        }
                    }
            }
    }
}