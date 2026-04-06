package com.example.collegemarketplace

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.collegemarketplace.databinding.LayoutProductSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PurchasedDetailSheet(private val product: Product) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutProductSheetBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutProductSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup UI with Product Data
        binding.tvSheetTitle.text = product.title
        binding.tvSheetPrice.text = "₹${product.price}"
        binding.tvSheetSeller.text = "Seller: ${product.sellerName}"

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_image)
            .into(binding.ivSheetImage)

        // 2. Logic based on Delivery Status
        if (product.isDelivered) {
            showAlreadyDeliveredUI()
        } else {
            binding.tvSheetDescription.text = "IMPORTANT: Only mark as delivered after you have physically received the item from ${product.sellerName}.\n\n(Long press the button below to confirm delivery)"

            binding.btnBuyNow.text = "Chat to Arrange Meeting"
            binding.btnBuyNow.setOnClickListener {
                val url = "https://api.whatsapp.com/send?phone=91${product.sellerPhone}"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
            }

            // 3. The BUYER holds this button to complete the deal
            binding.btnBuyNow.setOnLongClickListener {
                confirmDeliveryDialog()
                true
            }
        }
    }

    private fun confirmDeliveryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Receipt")
            .setMessage("By clicking 'Received', you confirm that you have the item and the transaction is complete.")
            .setPositiveButton("I Received It") { _, _ ->
                updateStatusInFirestore()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatusInFirestore() {
        val uid = auth.uid ?: return

        // STEP 1: Update the 'purchases' collection (for Buyer's History)
        db.collection("purchases")
            .whereEqualTo("productImageUrl", product.imageUrl)
            .whereEqualTo("buyerId", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    Toast.makeText(context, "Order record not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (doc in snapshots) {
                    doc.reference.update("isDelivered", true)
                }

                // STEP 2: Update the 'products' collection (for Seller's Listings)
                updateProductInGlobalMarket()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProductInGlobalMarket() {
        // Find the product in the main listings using the image URL as a unique key
        db.collection("products")
            .whereEqualTo("imageUrl", product.imageUrl)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    for (doc in snapshots) {
                        doc.reference.update("isDelivered", true)
                    }
                }

                // Show Success on UI after both are updated
                showAlreadyDeliveredUI()
                Toast.makeText(context, "Deal Closed! Seller's listing updated. ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Still show success to buyer even if the global market update lags
                showAlreadyDeliveredUI()
            }
    }

    private fun showAlreadyDeliveredUI() {
        binding.tvSheetDescription.text = "Status: DELIVERED ✅\n\nThis item was successfully handed over. Thank you for using CampusTrade REC!"
        binding.btnBuyNow.text = "Transaction Complete"
        binding.btnBuyNow.isEnabled = false
        binding.btnBuyNow.setBackgroundColor(Color.parseColor("#BDC3C7")) // Grey out the button
    }
}