package com.example.collegemarketplace

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collegemarketplace.databinding.ActivityPostRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostRequestBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbarRequest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnPostRequest.setOnClickListener {
            validateAndPost()
        }
    }

    private fun validateAndPost() {
        val title = binding.etRequestTitle.text.toString().trim()
        val desc = binding.etRequestDesc.text.toString().trim()
        val uid = auth.uid

        if (title.isEmpty() || uid == null) {
            Toast.makeText(this, "Please enter what you are looking for", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch User Info to attach to the request
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val name = doc.getString("name") ?: "REC Student"
                val dept = doc.getString("dept") ?: ""
                val year = doc.getString("year") ?: ""
                val whatsapp = doc.getString("whatsapp") ?: ""

                val newRequest = Request(
                    itemName = title,
                    description = desc,
                    requesterName = name,
                    requesterDept = dept,
                    requesterYear = year,
                    requesterId = uid,
                    requesterPhone = whatsapp,
                    timestamp = System.currentTimeMillis()
                )

                saveToFirestore(newRequest)
            } else {
                Toast.makeText(this, "Please complete your profile first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToFirestore(request: Request) {
        db.collection("requests").add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Request posted to REC Wall!", Toast.LENGTH_SHORT).show()
                finish() // Go back to the wall
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to post request", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}