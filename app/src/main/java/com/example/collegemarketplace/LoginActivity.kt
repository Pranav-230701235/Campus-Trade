package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collegemarketplace.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private var isLoginMode = true // Toggle between Login and Sign Up

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Switch between Login and Sign Up UI
        binding.tvToggleAuth.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                binding.btnLogin.text = "Login"
                binding.tvToggleAuth.text = "Don't have an account? Sign up"
            } else {
                binding.btnLogin.text = "Create Account"
                binding.tvToggleAuth.text = "Already have an account? Login"
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            // 1. Validation for REC Domain
            if (email.endsWith("@rajalakshmi.edu.in") && pass.length >= 6) {
                if (isLoginMode) {
                    performLogin(email, pass)
                } else {
                    performSignUp(email, pass)
                }
            } else {
                Toast.makeText(this, "Use REC Email (@rajalakshmi.edu.in) & 6+ chars password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogin(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                // 2. CRITICAL: Reload user to check latest Verification Status
                user?.reload()?.addOnCompleteListener {
                    if (user?.isEmailVerified == true) {
                        // Success: Go to Profile check
                        checkUserStatus()
                    } else {
                        // Block: Not verified yet
                        Toast.makeText(this, "Please verify your email! Check your REC inbox.", Toast.LENGTH_LONG).show()
                        user?.sendEmailVerification() // Re-send the link just in case
                        auth.signOut() // Sign out so they don't bypass on restart
                    }
                }
            } else {
                Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSignUp(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 3. Send the Verification Link immediately
                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                    Toast.makeText(this, "Account Created! Verify your email at $email then Login.", Toast.LENGTH_LONG).show()

                    // Switch UI back to Login mode
                    isLoginMode = true
                    binding.btnLogin.text = "Login"
                    binding.tvToggleAuth.text = "Don't have an account? Sign up"
                }
            } else {
                Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserStatus() {
        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // 4. Check if student has completed their "Seller Identity" (Profile)
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                // Profile exists -> Marketplace
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // New User -> Setup Profile
                startActivity(Intent(this, ProfileSetupActivity::class.java))
            }
            finish()
        }
    }
}