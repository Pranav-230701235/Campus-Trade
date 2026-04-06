package com.example.collegemarketplace

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.collegemarketplace.databinding.ActivityOrderConfirmationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderConfirmationBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // To prevent multiple clicks while processing
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar for Back Navigation
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Confirm Order"

        // 2. Extract Data from Intent
        val title = intent.getStringExtra("product_title") ?: "Product"
        val price = intent.getStringExtra("product_price") ?: "0"
        val image = intent.getStringExtra("product_image") ?: ""
        val sellerName = intent.getStringExtra("seller_name") ?: "Seller"
        val sellerPhone = intent.getStringExtra("seller_phone") ?: ""

        // 3. Bind UI Elements
        binding.tvConfirmTitle.text = title
        binding.tvConfirmPrice.text = "₹$price"

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.ic_image)
            .into(binding.ivConfirmImage)

        // 4. Confirm Button Logic
        binding.btnConfirm.setOnClickListener {
            if (isProcessing) return@setOnClickListener

            val uid = auth.uid ?: return@setOnClickListener

            // Start processing state
            isProcessing = true
            binding.btnConfirm.isEnabled = false
            binding.btnConfirm.text = "Processing..."

            // Build the Order object for history
            val order = Order(
                productTitle = title,
                productPrice = price,
                productImageUrl = image,
                sellerName = sellerName,
                sellerPhone = sellerPhone,
                buyerId = uid,
                timestamp = System.currentTimeMillis()
            )

            // STEP 1: Save to Buyer's Purchases Collection
            db.collection("purchases")
                .add(order)
                .addOnSuccessListener {
                    // STEP 2: Mark the original product as SOLD and link the Buyer ID
                    updateProductAsSold(image, sellerName, sellerPhone, title, uid)
                }
                .addOnFailureListener {
                    isProcessing = false
                    binding.btnConfirm.isEnabled = true
                    binding.btnConfirm.text = "Confirm Order"
                    Toast.makeText(this, "Order failed. Please check connection.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProductAsSold(imageUrl: String, name: String, phone: String, title: String, buyerUid: String) {
        // Find the product by its unique Image URL
        db.collection("products")
            .whereEqualTo("imageUrl", imageUrl)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    for (doc in snapshots) {
                        // CRITICAL FIX: Save the buyerId so the Adapter knows who bought it
                        val updates = hashMapOf<String, Any>(
                            "isSold" to true,
                            "buyerId" to buyerUid
                        )
                        doc.reference.update(updates)
                    }
                }
                // STEP 3: Switch UI to Success/WhatsApp mode
                showSuccessState(name, phone, title)
            }
            .addOnFailureListener {
                showSuccessState(name, phone, title)
            }
    }

    private fun showSuccessState(name: String, phone: String, title: String) {
        // UI Updates for Success
        binding.tvSellerPhone.visibility = View.VISIBLE
        binding.tvSellerPhone.text = "Confirmed! Contact $name: \n+91 $phone"
        binding.tvSellerPhone.setTextColor(Color.parseColor("#4CAF50"))
        binding.tvSellerPhone.textSize = 16f

        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.text = "Chat with Seller on WhatsApp"
        binding.btnConfirm.setBackgroundColor(Color.parseColor("#25D366"))

        // Step 4: Final logic to redirect to WhatsApp
        binding.btnConfirm.setOnClickListener {
            val msg = "Hi $name, I just confirmed my interest in your $title on CampusTrade REC!"
            val url = "https://api.whatsapp.com/send?phone=91$phone&text=${Uri.encode(msg)}"
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed on this device", Toast.LENGTH_SHORT).show()
            }
        }
        Toast.makeText(this, "Purchase Confirmed & Recorded!", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}