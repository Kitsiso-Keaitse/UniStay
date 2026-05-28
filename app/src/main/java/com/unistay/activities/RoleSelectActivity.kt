package com.unistay.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.unistay.R

class RoleSelectActivity : AppCompatActivity() {

    private var selectedRole = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_select)

        val cardStudent = findViewById<LinearLayout>(R.id.cardStudent)
        val cardProvider = findViewById<LinearLayout>(R.id.cardProvider)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        cardStudent.setOnClickListener {
            selectedRole = "student"
            cardStudent.setBackgroundResource(R.drawable.bg_role_selected_student)
            cardProvider.setBackgroundResource(R.drawable.bg_role_card)
            btnContinue.text = "Continue as Student →"
        }

        cardProvider.setOnClickListener {
            selectedRole = "provider"
            cardProvider.setBackgroundResource(R.drawable.bg_role_selected_provider)
            cardStudent.setBackgroundResource(R.drawable.bg_role_card)
            btnContinue.text = "Continue as Provider →"
        }

        btnContinue.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("selected_role", selectedRole)
            startActivity(intent)
        }
    }
}