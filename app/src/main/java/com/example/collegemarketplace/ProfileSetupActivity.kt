package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collegemarketplace.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Year Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.year_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = adapter

        binding.btnSaveProfile.setOnClickListener {
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val name = binding.etFullName.text.toString().trim()
        val dept = binding.etDept.text.toString().trim()
        val year = binding.spinnerYear.selectedItem.toString()
        val whatsapp = binding.etWhatsApp.text.toString().trim()
        val uid = auth.currentUser?.uid

        // Basic Validation
        if (name.isEmpty() || dept.isEmpty() || whatsapp.isEmpty() || year == "Select Year") {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (whatsapp.length < 10) {
            Toast.makeText(this, "Enter a valid 10-digit number", Toast.LENGTH_SHORT).show()
            return
        }

        val userMap = hashMapOf(
            "uid" to uid,
            "name" to name,
            "dept" to dept,
            "year" to year,
            "whatsapp" to whatsapp
        )

        // Save to Firestore
        if (uid != null) {
            db.collection("users").document(uid).set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Welcome to CampusTrade!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}