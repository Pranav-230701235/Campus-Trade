package com.example.collegemarketplace

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.collegemarketplace.databinding.ItemRequestBinding
import com.google.firebase.firestore.FirebaseFirestore

class RequestAdapter(
    private var requests: List<Request>,
    private var requestIds: List<String>, // Added to track document IDs for deletion
    private val currentUserId: String?
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        val docId = requestIds[position]
        val context = holder.itemView.context

        holder.binding.tvReqTitle.text = request.itemName
        holder.binding.tvReqDesc.text = request.description
        holder.binding.tvReqBy.text = "By ${request.requesterName}"
        holder.binding.tvReqDept.text = "${request.requesterYear} - ${request.requesterDept}"

        if (request.requesterId == currentUserId) {
            // User owns this request: Show Delete, Hide Help
            holder.binding.btnHelp.visibility = View.GONE
            holder.binding.btnDeleteRequest.visibility = View.VISIBLE

            holder.binding.btnDeleteRequest.setOnClickListener {
                showDeleteConfirmation(context, docId)
            }
        } else {
            // Others see this request: Hide Delete, Show Help
            holder.binding.btnHelp.visibility = View.VISIBLE
            holder.binding.btnDeleteRequest.visibility = View.GONE

            holder.binding.btnHelp.setOnClickListener {
                openWhatsApp(context, request)
            }
        }
    }

    private fun showDeleteConfirmation(context: Context, docId: String) {
        AlertDialog.Builder(context)
            .setTitle("Fulfillment")
            .setMessage("Has this request been fulfilled? Deleting it will remove it from the REC Wall.")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance().collection("requests").document(docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request removed!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = requests.size

    fun updateData(newList: List<Request>, newIds: List<String>) {
        requests = newList
        requestIds = newIds
        notifyDataSetChanged()
    }

    private fun openWhatsApp(context: Context, request: Request) {
        val message = "Hi ${request.requesterName}, I saw your request for '${request.itemName}' on CampusTrade REC. I have this item available!"
        val phoneNumber = request.requesterPhone
        val fullNumber = if (phoneNumber.startsWith("+")) phoneNumber else "91$phoneNumber"

        try {
            val url = "https://api.whatsapp.com/send?phone=$fullNumber&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}