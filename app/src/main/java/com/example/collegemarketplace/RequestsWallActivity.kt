package com.example.collegemarketplace

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collegemarketplace.databinding.ActivityRequestsWallBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RequestsWallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsWallBinding
    private lateinit var requestAdapter: RequestAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsWallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbarWall)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "REC Request Wall"

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. FAB Listener to post new request
        binding.fabPostRequest.setOnClickListener {
            startActivity(Intent(this, PostRequestActivity::class.java))
        }

        // 4. Initial Data Fetch
        fetchRequests()
    }

    private fun setupRecyclerView() {
        val currentUid = auth.currentUser?.uid

        // Initialize adapter with empty lists for both Data and Document IDs
        requestAdapter = RequestAdapter(mutableListOf(), mutableListOf(), currentUid)

        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = requestAdapter
    }

    private fun fetchRequests() {
        db.collection("requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Log error if needed
                    return@addSnapshotListener
                }

                val requestList = mutableListOf<Request>()
                val requestIdList = mutableListOf<String>()

                // Manually loop to capture the Firestore Document ID for deletion
                snapshot?.documents?.forEach { doc ->
                    val request = doc.toObject(Request::class.java)
                    if (request != null) {
                        requestList.add(request)
                        requestIdList.add(doc.id) // This is the unique Firestore ID
                    }
                }

                if (requestList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvRequests.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvRequests.visibility = View.VISIBLE

                    // Update the adapter with both the data AND the IDs
                    requestAdapter.updateData(requestList, requestIdList)
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}