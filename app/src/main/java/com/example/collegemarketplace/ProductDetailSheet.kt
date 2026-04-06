package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.collegemarketplace.databinding.LayoutProductSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class ProductDetailSheet(private val product: Product) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutProductSheetBinding
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

        val currentUserId = auth.uid

        // 1. Populate UI with Product Data
        binding.tvSheetTitle.text = product.title
        binding.tvSheetPrice.text = "₹${product.price}"
        binding.tvSheetSeller.text = "Seller: ${product.sellerName}"
        binding.tvSheetDept.text = "${product.sellerDept} - ${product.sellerYear}"

        // Populate Description
        binding.tvSheetDescription.text = if (product.description.isNullOrEmpty()) {
            "No additional details provided by the seller."
        } else {
            product.description
        }

        // --- Handle UI States: Sold Out or Own Product ---
        if (product.isSold) {
            binding.btnBuyNow.visibility = View.GONE
            binding.tvSheetTitle.text = "${product.title} (SOLD)"
            binding.tvSheetPrice.alpha = 0.5f
        } else if (product.sellerId == currentUserId) {
            binding.btnBuyNow.visibility = View.GONE
            binding.tvSheetDescription.text = "${binding.tvSheetDescription.text}\n\n(This is your listing)"
        } else {
            binding.btnBuyNow.visibility = View.VISIBLE
        }

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_image)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.ivSheetImage)

        // 2. Buy Now Logic: Navigate to Confirmation Page instead of WhatsApp
        binding.btnBuyNow.setOnClickListener {
            val intent = Intent(requireContext(), OrderConfirmationActivity::class.java)

            // Passing all necessary details to the Confirmation Activity
            intent.putExtra("product_title", product.title)
            intent.putExtra("product_price", product.price)
            intent.putExtra("product_image", product.imageUrl)
            intent.putExtra("seller_name", product.sellerName)
            intent.putExtra("seller_phone", product.sellerPhone)
            intent.putExtra("seller_id", product.sellerId)

            startActivity(intent)
            dismiss() // Close the sheet
        }
    }
}