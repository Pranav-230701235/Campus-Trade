package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.collegemarketplace.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- SESSION CHECK: Check if user is already logged in ---
        if (auth.currentUser != null) {
            // User is already signed in, go straight to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // CRITICAL: This removes WelcomeActivity from the backstack
            return // Stop further execution of this method
        }

        // If no user is logged in, proceed with showing the Welcome UI
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}