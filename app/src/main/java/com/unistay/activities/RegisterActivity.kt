package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.models.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedRole = "student"

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var btnStudentRole: LinearLayout
    private lateinit var btnProviderRole: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        selectedRole = intent.getStringExtra("selected_role") ?: "student"

        initViews()
        setupRoleButtons()
        setupListeners()
    }

    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        btnStudentRole = findViewById(R.id.btnStudentRole)
        btnProviderRole = findViewById(R.id.btnProviderRole)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRoleButtons() {
        if (selectedRole == "student") {
            btnStudentRole.setBackgroundResource(R.drawable.bg_role_selected_student)
            btnProviderRole.setBackgroundResource(R.drawable.bg_role_card)
        } else {
            btnProviderRole.setBackgroundResource(R.drawable.bg_role_selected_provider)
            btnStudentRole.setBackgroundResource(R.drawable.bg_role_card)
        }
    }

    private fun setupListeners() {
        btnStudentRole.setOnClickListener {
            selectedRole = "student"
            btnStudentRole.setBackgroundResource(R.drawable.bg_role_selected_student)
            btnProviderRole.setBackgroundResource(R.drawable.bg_role_card)
        }

        btnProviderRole.setOnClickListener {
            selectedRole = "provider"
            btnProviderRole.setBackgroundResource(R.drawable.bg_role_selected_provider)
            btnStudentRole.setBackgroundResource(R.drawable.bg_role_card)
        }

        btnRegister.setOnClickListener { registerUser() }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false
        Toast.makeText(this, "Starting registration...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                
                // AUTH SUCCESS - Now saving profile
                Toast.makeText(this, "Account created! Saving profile...", Toast.LENGTH_SHORT).show()
                
                val user = User(
                    userId = userId,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    role = selectedRole,
                    phoneNumber = phone,
                    createdAt = System.currentTimeMillis(),
                    isVerified = false
                )

                db.collection("users").document(userId).set(user)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Success! Redirecting...", Toast.LENGTH_SHORT).show()

                        val nextActivity = if (selectedRole == "provider") {
                            ProviderMainActivity::class.java
                        } else {
                            StudentMainActivity::class.java
                        }
                        startActivity(Intent(this, nextActivity))
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        Log.e("RegisterActivity", "Firestore error", e)
                        handleFailure("Account created, but profile save failed: ${e.localizedMessage}. Check if Firestore is enabled in Console.")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "Auth error", e)
                handleFailure("Registration failed: ${e.localizedMessage}")
            }
    }

    private fun handleFailure(message: String) {
        progressBar.visibility = View.GONE
        btnRegister.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}