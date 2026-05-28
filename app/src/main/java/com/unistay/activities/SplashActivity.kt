package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.R
import com.unistay.utils.DataSeeder

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // Seed data for the first time (Requirement A & B: 50 students/listings)
        // You can comment this out after the first run
        DataSeeder.seedData()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                checkUserRoleAndNavigate(currentUser.uid)
            } else {
                startActivity(Intent(this, RoleSelectActivity::class.java))
                finish()
            }
        }, 2000)
    }

    private fun checkUserRoleAndNavigate(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "student"
                if (role == "provider") {
                    startActivity(Intent(this, ProviderMainActivity::class.java))
                } else {
                    startActivity(Intent(this, StudentMainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, RoleSelectActivity::class.java))
                finish()
            }
    }
}
