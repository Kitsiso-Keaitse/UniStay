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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgot: TextView
    private lateinit var progressBar: ProgressBar
    private var selectedRole = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        selectedRole = intent.getStringExtra("selected_role") ?: "student"

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgot = findViewById(R.id.tvForgot)
        progressBar = findViewById(R.id.progressBar)

        // Use consistent hint for both roles
        etEmail.hint = "Email Address"

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("selected_role", selectedRole)
            startActivity(intent)
        }

        tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false
        
        Log.d("LoginActivity", "Attempting login for $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                Log.d("LoginActivity", "Auth success, fetching user profile for $userId")

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        
                        if (!document.exists()) {
                            Log.e("LoginActivity", "User document does not exist in Firestore")
                            Toast.makeText(this, "Account profile not found. Please register.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            return@addOnSuccessListener
                        }

                        val role = document.getString("role") ?: ""
                        Log.d("LoginActivity", "User role: $role, Selected role: $selectedRole")

                        if (role != selectedRole) {
                            Toast.makeText(this, "This account is registered as a $role. Please go back and select the correct role.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            return@addOnSuccessListener
                        }

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        if (role == "provider") {
                            startActivity(Intent(this, ProviderMainActivity::class.java))
                        } else {
                            startActivity(Intent(this, StudentMainActivity::class.java))
                        }
                        finishAffinity()
                    }
                    .addOnFailureListener { e ->
                        Log.e("LoginActivity", "Firestore fetch error", e)
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Error fetching profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Auth error", e)
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Toast.makeText(this, "Login failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}