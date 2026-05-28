package com.unistay.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.unistay.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnReset: Button
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etEmail)
        btnReset = findViewById(R.id.btnReset)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        tvMessage = findViewById(R.id.tvMessage)

        btnReset.setOnClickListener { resetPassword() }
        btnBack.setOnClickListener { finish() }
    }

    private fun resetPassword() {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        btnReset.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressBar.visibility = android.view.View.GONE
                tvMessage.visibility = android.view.View.VISIBLE
                tvMessage.text = "Password reset link sent to $email.\nCheck your inbox."
                etEmail.setText("")
                Toast.makeText(this, "Reset email sent", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = android.view.View.GONE
                btnReset.isEnabled = true
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}