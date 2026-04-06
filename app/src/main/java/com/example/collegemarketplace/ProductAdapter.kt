package com.example.collegemarketplace

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.collegemarketplace.databinding.ItemProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(
    private var products: List<Product>,
    private val canDelete: Boolean = false,
    private val isHistory: Boolean = false
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        val uid = auth.uid ?: ""

        // 1. Title & Price
        holder.binding.tvTitle.text = if (isHistory && product.isDelivered) {
            "${product.title} (Delivered ✅)"
        } else {
            product.title
        }
        holder.binding.tvPrice.text = "₹${product.price}"

        // 2. Seller Info
        holder.binding.tvSellerInfo.text = if (!product.sellerDept.isNullOrEmpty()) {
            "${product.sellerDept} - ${product.sellerYear}"
        } else {
            "Seller: ${product.sellerName}"
        }

        // 3. Status Badges Logic
        updateBadgeUI(holder, product)

        // 4. Wishlist Icon Logic
        if (canDelete || isHistory) {
            holder.binding.btnWishlist.visibility = View.GONE
        } else {
            holder.binding.btnWishlist.visibility = View.VISIBLE
            checkWishlistStatus(product, holder.binding.btnWishlist, uid)
            holder.binding.btnWishlist.setOnClickListener {
                toggleWishlist(product, holder.binding.btnWishlist, uid)
            }
        }

        // 5. Image Loading
        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_image)
            .into(holder.binding.ivProduct)

        // 6. Seller Controls (Delete / Mark Sold)
        if (canDelete) {
            holder.binding.btnDelete.visibility = View.VISIBLE
            holder.binding.btnMarkAsSold.visibility = View.VISIBLE
            holder.binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Listing?")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete") { _, _ -> deleteProduct(product) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            holder.binding.btnMarkAsSold.setOnClickListener { toggleSoldStatus(product) }
        } else {
            holder.binding.btnDelete.visibility = View.GONE
            holder.binding.btnMarkAsSold.visibility = View.GONE
        }

        // 7. Item Click Handling
        holder.itemView.setOnClickListener {
            val activity = context as AppCompatActivity
            when {
                isHistory -> {
                    PurchasedDetailSheet(product).show(activity.supportFragmentManager, "PurchasedDetail")
                }
                product.isSold && !canDelete -> {
                    Toast.makeText(context, "Item no longer available.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    ProductDetailSheet(product).show(activity.supportFragmentManager, "ProductDetail")
                }
            }
        }
    }

    private fun updateBadgeUI(holder: ProductViewHolder, product: Product) {
        val currentUid = auth.uid ?: ""

        if (product.isSold) {
            holder.binding.cardSoldBadge.visibility = View.VISIBLE
            holder.binding.cardSoldBadge.setCardBackgroundColor(Color.RED)
            holder.binding.tvSoldStatus.text = "SOLD"
            holder.binding.ivProduct.alpha = 0.7f

            if (currentUid == product.sellerId || currentUid == product.buyerId) {
                holder.binding.cardDeliveryBadge.visibility = View.VISIBLE
                if (product.isDelivered) {
                    holder.binding.cardDeliveryBadge.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                    holder.binding.tvDeliveryStatus.text = "DELIVERED"
                    holder.binding.tvDeliveryStatus.setTextColor(Color.WHITE)
                } else {
                    holder.binding.cardDeliveryBadge.setCardBackgroundColor(Color.parseColor("#FFEB3B"))
                    holder.binding.tvDeliveryStatus.text = "IN PROGRESS"
                    holder.binding.tvDeliveryStatus.setTextColor(Color.BLACK)
                }
            } else {
                holder.binding.cardDeliveryBadge.visibility = View.GONE
            }
        } else {
            holder.binding.cardSoldBadge.visibility = View.GONE
            holder.binding.cardDeliveryBadge.visibility = View.GONE
            holder.binding.ivProduct.alpha = 1.0f
        }
    }

    private fun checkWishlistStatus(product: Product, button: ImageButton, uid: String) {
        if (uid.isEmpty()) return
        db.collection("wishlists").document(uid).collection("my_items")
            .whereEqualTo("imageUrl", product.imageUrl)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    button.setImageResource(android.R.drawable.btn_star_big_on)
                    button.setColorFilter(Color.parseColor("#FFD700")) // Yellow/Gold
                } else {
                    button.setImageResource(android.R.drawable.btn_star_big_off)
                    button.setColorFilter(Color.WHITE) // Back to White
                }
            }
    }

    private fun toggleWishlist(product: Product, button: ImageButton, uid: String) {
        if (uid.isEmpty()) {
            Toast.makeText(button.context, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val wishlistRef = db.collection("wishlists").document(uid).collection("my_items")
        wishlistRef.whereEqualTo("imageUrl", product.imageUrl).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // Item not in wishlist, adding it
                    wishlistRef.add(product).addOnSuccessListener {
                        button.setImageResource(android.R.drawable.btn_star_big_on)
                        button.setColorFilter(Color.parseColor("#FFD700")) // Yellow
                        Toast.makeText(button.context, "Added to Wishlist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Item in wishlist, removing it
                    for (doc in snapshot) doc.reference.delete()
                    button.setImageResource(android.R.drawable.btn_star_big_off)
                    button.setColorFilter(Color.WHITE) // White
                    Toast.makeText(button.context, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteProduct(product: Product) {
        db.collection("products").whereEqualTo("imageUrl", product.imageUrl).get()
            .addOnSuccessListener { for (doc in it) doc.reference.delete() }
    }

    private fun toggleSoldStatus(product: Product) {
        val newStatus = !product.isSold
        db.collection("products").whereEqualTo("imageUrl", product.imageUrl).get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots) {
                    doc.reference.update("isSold", newStatus)
                }
            }
    }

    override fun getItemCount() = products.size

    fun updateData(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }
}