package com.example.collegemarketplace

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegemarketplace.databinding.ActivityMyListingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListingsBinding
    private lateinit var adapter: ProductAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Wishlist"

        // canDelete = false so Pranav can't delete Sanjay's post
        // isHistory = false so the heart icon stays visible for "Remove from Wishlist"
        adapter = ProductAdapter(mutableListOf(), canDelete = false, isHistory = false)

        binding.rvMyProducts.layoutManager = LinearLayoutManager(this)
        binding.rvMyProducts.adapter = adapter

        fetchLiveWishlist()
    }

    private fun fetchLiveWishlist() {
        val uid = auth.uid ?: return

        // 1. Listen to Pranav's Wishlist collection
        db.collection("wishlists").document(uid).collection("my_items")
            .addSnapshotListener { wishlistSnapshot, _ ->
                val savedItems = wishlistSnapshot?.toObjects(Product::class.java) ?: mutableListOf()

                if (savedItems.isEmpty()) {
                    adapter.updateData(emptyList())
                    return@addSnapshotListener
                }

                // 2. Listen to the main Products collection to get LIVE status (Sold/Available)
                db.collection("products").addSnapshotListener { productsSnapshot, _ ->
                    val allMarketProducts = productsSnapshot?.toObjects(Product::class.java) ?: mutableListOf()

                    val updatedList = mutableListOf<Product>()

                    for (savedProduct in savedItems) {
                        // Find the real-time version of this product in the market
                        val liveProduct = allMarketProducts.find { it.imageUrl == savedProduct.imageUrl }

                        if (liveProduct != null) {
                            // Add the LIVE version so it shows the "SOLD" badge if Sanjay bought it
                            updatedList.add(liveProduct)
                        } else {
                            // If the seller deleted the post entirely, we can still show the saved version
                            updatedList.add(savedProduct)
                        }
                    }

                    adapter.updateData(updatedList.reversed())
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}