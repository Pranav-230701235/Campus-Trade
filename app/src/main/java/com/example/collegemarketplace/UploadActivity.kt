package com.example.collegemarketplace

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.collegemarketplace.databinding.ActivityUploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar with the Back Arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 2. Setup Category Dropdown
        val categories = arrayOf("Books", "Lab Kits", "Electronics", "Stationery", "Cycles", "Others")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        binding.autoCompleteCategory.setAdapter(categoryAdapter)

        // 3. Live Image Preview
        binding.etImageUrl.addTextChangedListener {
            val url = it.toString().trim()
            if (url.isNotEmpty()) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_image)
                    .error(android.R.drawable.stat_notify_error)
                    .into(binding.ivPreview)
            }
        }

        binding.btnPublish.setOnClickListener {
            validateAndUpload()
        }
    }

    private fun validateAndUpload() {
        val title = binding.etTitle.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // NEW: Capture Category
        val category = binding.autoCompleteCategory.text.toString().trim()

        val uid = auth.uid

        // Added category to the validation check
        if (title.isEmpty() || price.isEmpty() || imageUrl.isEmpty() || category.isEmpty() || uid == null) {
            Toast.makeText(this, "Please fill all required details including Category", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Fetch Seller Info from Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "REC Student"
                    val dept = document.getString("dept") ?: ""
                    val year = document.getString("year") ?: ""
                    val phone = document.getString("whatsapp") ?: ""

                    saveProductToFirestore(title, price, description, imageUrl, category, name, dept, year, phone)
                } else {
                    Toast.makeText(this, "Please finish your profile first!", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveProductToFirestore(
        t: String, p: String, desc: String, img: String, cat: String,
        name: String, dept: String, year: String, phone: String
    ) {
        val product = Product(
            title = t,
            price = p,
            description = desc,
            imageUrl = img,
            category = cat, // SAVING THE CATEGORY
            sellerId = auth.uid ?: "",
            sellerName = name,
            sellerDept = dept,
            sellerYear = year,
            sellerPhone = phone
        )

        db.collection("products").add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Item posted to REC Marketplace!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}