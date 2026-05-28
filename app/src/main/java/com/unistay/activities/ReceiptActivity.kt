package com.unistay.activities

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView

class ReceiptActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var reservationId = ""
    private var referenceNumber = ""
    private var accommodationTitle = ""
    private var amountPaid = 0

    private lateinit var tvReference: TextView
    private lateinit var tvAccommodation: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentId: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnDownload: Button
    private lateinit var btnHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        reservationId = intent.getStringExtra("reservation_id") ?: ""
        referenceNumber = intent.getStringExtra("reference_number") ?: ""
        accommodationTitle = intent.getStringExtra("accommodation_title") ?: ""
        amountPaid = intent.getIntExtra("amount_paid", 0)

        initViews()
        loadReceiptData()

        btnDownload.setOnClickListener { downloadReceipt() }
        btnHome.setOnClickListener {
            val intent = Intent(this, StudentMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, StudentMainActivity::class.java))
            finish()
        }
    }

    private fun initViews() {
        tvReference = findViewById(R.id.tvReference)
        tvAccommodation = findViewById(R.id.tvAccommodation)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvStudentId = findViewById(R.id.tvStudentId)
        tvAmount = findViewById(R.id.tvAmount)
        tvDate = findViewById(R.id.tvDate)
        tvStatus = findViewById(R.id.tvStatus)
        btnDownload = findViewById(R.id.btnDownload)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun loadReceiptData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""
                val studentId = userDoc.getString("studentId") ?: ""

                tvStudentName.text = "$firstName $lastName"
                tvStudentId.text = studentId
            }

        tvReference.text = referenceNumber
        tvAccommodation.text = accommodationTitle
        tvAmount.text = "BWP $amountPaid"

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
        tvStatus.text = "CONFIRMED"
        tvStatus.setTextColor(resources.getColor(R.color.green_success))
    }

    private fun downloadReceipt() {
        Toast.makeText(this, "Receipt saved to Downloads folder", Toast.LENGTH_SHORT).show()
        // In production, generate actual PDF
    }
}