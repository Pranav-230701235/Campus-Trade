package com.example.collegemarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegemarketplace.databinding.ActivityMyListingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyListingsActivity : AppCompatActivity() {

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
        supportActionBar?.title = "My Listings"

        // 2. Initialize Adapter
        // canDelete = true: Allows the seller to Delete or toggle SOLD status
        // isHistory = false: This is the seller's management view, not the buyer's history
        adapter = ProductAdapter(mutableListOf(), canDelete = true, isHistory = false)

        binding.rvMyProducts.layoutManager = LinearLayoutManager(this)
        binding.rvMyProducts.adapter = adapter

        fetchMyListings()
    }

    private fun fetchMyListings() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // We listen to the "products" collection where the current user is the seller
        db.collection("products")
            .whereEqualTo("sellerId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle potential errors here (e.g., logging)
                    return@addSnapshotListener
                }

                // Convert Firestore documents directly into Product objects
                // This includes isSold and the newly updated isDelivered field
                val myList = snapshot?.toObjects(Product::class.java) ?: mutableListOf()

                // Optional: Sort so that active items are at the top, or by title
                val sortedList = myList.sortedBy { it.isDelivered }

                adapter.updateData(sortedList)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle the back arrow in the toolbar
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}