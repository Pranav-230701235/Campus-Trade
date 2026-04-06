package com.example.collegemarketplace

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.collegemarketplace.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Master list to keep track of available products for searching and filtering
    private var marketProducts = mutableListOf<Product>()

    // Permission launcher for Android 13+ notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled. You might miss requests!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 1. Initialize UI components
        setupRecyclerView()
        setupBottomNavigation()
        setupCategoryFilters()
        loadUserGreeting()

        // 2. Setup Banner Listener for Request Wall
        binding.cardRequestWall.setOnClickListener {
            startActivity(Intent(this, RequestsWallActivity::class.java))
        }

        // 3. Setup Push Notifications
        askNotificationPermission()
        subscribeToNotifications()

        // 4. Initial fetch from Firestore
        fetchProductsFromFirestore()
    }

    private fun subscribeToNotifications() {
        // Subscribe all REC students to the "requests" topic
        FirebaseMessaging.getInstance().subscribeToTopic("requests")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Subscription failed
                }
            }
    }

    private fun askNotificationPermission() {
        // Only necessary for API level >= 33 (Android 13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProductsFromFirestore()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(mutableListOf(), canDelete = false, isHistory = false)
        binding.rvProducts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvProducts.adapter = adapter
    }

    private fun setupCategoryFilters() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty() || checkedIds.contains(binding.chipAll.id)) {
                adapter.updateData(marketProducts)
                if (checkedIds.contains(binding.chipAll.id) && checkedIds.size > 1) {
                    checkedIds.forEach { id ->
                        if (id != binding.chipAll.id) {
                            findViewById<Chip>(id).isChecked = false
                        }
                    }
                }
            } else {
                val selectedCategories = checkedIds.map { id ->
                    findViewById<Chip>(id).text.toString().lowercase()
                }
                val filteredList = marketProducts.filter { product ->
                    selectedCategories.contains(product.category.lowercase())
                }
                adapter.updateData(filteredList)
                if (binding.chipAll.isChecked) {
                    binding.chipAll.isChecked = false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_toolbar_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.queryHint = "Search books, kits, etc..."
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
        return true
    }

    private fun filterList(query: String?) {
        val filteredList = mutableListOf<Product>()
        val searchText = query?.lowercase() ?: ""

        if (searchText.isEmpty()) {
            filteredList.addAll(marketProducts)
        } else {
            for (product in marketProducts) {
                if (product.title.lowercase().contains(searchText) ||
                    product.sellerDept.lowercase().contains(searchText) ||
                    product.category.lowercase().contains(searchText)) {
                    filteredList.add(product)
                }
            }
        }
        adapter.updateData(filteredList)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.rvProducts.smoothScrollToPosition(0)
                    true
                }
                R.id.nav_sell -> {
                    startActivity(Intent(this, UploadActivity::class.java))
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadUserGreeting() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val name = doc.getString("name") ?: "RECian"
            binding.tvWelcomeUser.text = "Hello, $name!"
        }
    }

    private fun fetchProductsFromFirestore() {
        val currentUid = auth.currentUser?.uid
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null) return@addOnSuccessListener
                val allItems = snapshot.toObjects(Product::class.java)
                val filteredItems = allItems.filter {
                    it.sellerId != currentUid && !it.isSold
                }
                marketProducts.clear()
                marketProducts.addAll(filteredItems)
                binding.chipAll.isChecked = true
                adapter.updateData(filteredItems)
            }
    }
}