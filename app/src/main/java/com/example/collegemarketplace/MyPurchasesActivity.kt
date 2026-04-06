package com.example.collegemarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegemarketplace.databinding.ActivityMyListingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPurchasesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListingsBinding
    private lateinit var adapter: ProductAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Purchase History"

        // 2. Initialize Adapter
        // canDelete = false (Buyers shouldn't delete their history records)
        // isHistory = true (Enables the PurchasedDetailSheet when clicking an item)
        adapter = ProductAdapter(mutableListOf(), canDelete = false, isHistory = true)

        binding.rvMyProducts.layoutManager = LinearLayoutManager(this)
        binding.rvMyProducts.adapter = adapter

        fetchPurchaseHistory()
    }

    private fun fetchPurchaseHistory() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // We listen to the "purchases" collection where the current user is the buyer
        db.collection("purchases")
            .whereEqualTo("buyerId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Log error if needed: e.message
                    return@addSnapshotListener
                }

                // Convert Firestore documents into Order objects
                val orders = snapshot?.toObjects(Order::class.java) ?: return@addSnapshotListener

                // MAP Order -> Product
                // This is where we bridge the database fields to the UI Adapter
                val productList = orders.map { order ->
                    Product(
                        title = order.productTitle,
                        price = order.productPrice,
                        imageUrl = order.productImageUrl,
                        sellerName = order.sellerName,
                        sellerPhone = order.sellerPhone,
                        isSold = false, // We keep this false so the red badge doesn't clutter history
                        isDelivered = order.isDelivered // CRITICAL: This pulls the saved status
                    )
                }

                // Sort by timestamp (most recent at the top)
                val sortedList = productList.sortedByDescending { it.title } // Or use order.timestamp if you added it to Product

                // Update the RecyclerView
                adapter.updateData(productList.reversed())
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Standard back navigation for the toolbar arrow
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}