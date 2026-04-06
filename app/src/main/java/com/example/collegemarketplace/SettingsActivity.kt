package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collegemarketplace.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar with Back Arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // 2. Load User Info for the Header
        loadUserInfo()

        // 3. Navigation Listeners

        // My Listings
        binding.btnMyAds.setOnClickListener {
            startActivity(Intent(this, MyListingsActivity::class.java))
        }

        // --- MY WISHLIST ---
        binding.btnWishlist.setOnClickListener {
            if (auth.uid != null) {
                startActivity(Intent(this, WishlistActivity::class.java))
            } else {
                Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            }
        }

        // My Orders / Purchases
        binding.btnMyOrders.setOnClickListener {
            startActivity(Intent(this, MyPurchasesActivity::class.java))
        }

        // 4. Edit Profile
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            intent.putExtra("isEditing", true)
            startActivity(intent)
        }

        // 5. Contact Support
        binding.btnSupport.setOnClickListener {
            Toast.makeText(this, "Support: student-support@rajalakshmi.edu.in", Toast.LENGTH_LONG).show()
        }

        // 6. Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserInfo() {
        val uid = auth.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                binding.tvUserHeader.text = doc.getString("name") ?: "Student"
                val dept = doc.getString("dept") ?: ""
                val year = doc.getString("year") ?: ""
                binding.tvUserDetails.text = "$dept - $year"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}