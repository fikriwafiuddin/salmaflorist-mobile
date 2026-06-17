package com.example.salmaflorist.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.salmaflorist.R
import com.example.salmaflorist.adapter.CatalogProductApiAdapter
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CategoryDto
import com.example.salmaflorist.data.api.dto.ProductDto
import com.example.salmaflorist.data.api.dto.toProductModel
import com.example.salmaflorist.data.repository.ProductRepositoryProvider
import com.example.salmaflorist.databinding.FragmentCatalogBinding
import com.example.salmaflorist.model.Product
import com.example.salmaflorist.ui.fragment.ProductDetailFragment
import com.example.salmaflorist.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Catalog Fragment - Menampilkan katalog produk dengan filter dan search
 */
class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private val productRepository by lazy {
        ProductRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }
    }

    private var currentKeyword = ""
    private var currentCategoryId: Int? = null
    private var currentSort = "Default"
    private var allProducts: List<ProductDto> = emptyList()
    private var allCategories: List<CategoryDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupFilterUI()
        loadCategories()
        loadProducts()
    }

    private fun setupFilterUI() {
        // Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s.toString()
                filterAndDisplayProducts()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSearch.setOnClickListener {
            currentKeyword = binding.etSearch.text.toString()
            filterAndDisplayProducts()
        }

        // Sort functionality
        val sortOptions = listOf("Default", "Harga Terendah", "Harga Tertinggi")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions)
        binding.spinnerPriceSort.adapter = sortAdapter

        binding.spinnerPriceSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = sortOptions[position]
                filterAndDisplayProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Load kategori dari API
     */
    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getCategories()

            when (result) {
                is ApiResult.Success -> {
                    allCategories = result.data
                    setupCategorySpinner()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("CatalogFragment", "Error loading categories: ${result.message}")
                }
                is ApiResult.Loading -> {
                    // Loading state
                }
            }
        }
    }

    /**
     * Setup spinner kategori setelah data dimuat
     */
    private fun setupCategorySpinner() {
        val categoryNames = mutableListOf("Semua")
        categoryNames.addAll(allCategories.map { it.name })

        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.spinnerCategory.adapter = catAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategoryId = if (position == 0) null else allCategories[position - 1].id
                filterAndDisplayProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Load produk dari API
     */
    private fun loadProducts() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getProducts(
                categoryId = currentCategoryId,
                search = if (currentKeyword.isBlank()) null else currentKeyword
            )

            showLoading(false)

            when (result) {
                is ApiResult.Success -> {
                    allProducts = result.data
                    android.util.Log.d("CatalogFragment", "Loaded ${allProducts.size} products")
                    filterAndDisplayProducts()
                }
                is ApiResult.Error -> {
                    android.util.Log.e("CatalogFragment", "Error loading products: ${result.message}")
                    showEmptyState()
                }
                is ApiResult.Loading -> {
                    // Loading handled in showLoading
                }
            }
        }
    }

    /**
     * Filter dan display produk berdasarkan kategori dan sort
     */
    private fun filterAndDisplayProducts() {
        // Filter by category
        var filtered = if (currentCategoryId != null) {
            allProducts.filter { it.categoryId == currentCategoryId }
        } else {
            allProducts
        }

        // Filter by keyword (jika belum difilter via API)
        if (currentKeyword.isNotBlank() && filtered.size == allProducts.size) {
            filtered = filtered.filter {
                it.name.contains(currentKeyword, ignoreCase = true) ||
                it.description?.contains(currentKeyword, ignoreCase = true) == true
            }
        }

        // Sort
        filtered = when (currentSort) {
            "Harga Terendah" -> filtered.sortedBy { it.price }
            "Harga Tertinggi" -> filtered.sortedByDescending { it.price }
            else -> filtered
        }

        displayProducts(filtered.map { it.toProductModel() })
    }

    /**
     * Display produk di RecyclerView
     */
    private fun displayProducts(products: List<Product>) {
        if (products.isEmpty()) {
            showEmptyState()
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            val adapter = CatalogProductApiAdapter(products) { product ->
                navigateToDetail(product.id)
            }
            binding.rvProducts.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                this.adapter = adapter
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        // For now, we don't have a loading indicator in the layout
        // Could add one or use the empty state as loading placeholder
        if (isLoading) {
            binding.tvEmptyState.text = "Memuat produk..."
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        }
    }

    private fun navigateToDetail(productId: Int) {
        val fragment = ProductDetailFragment.newInstance(productId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CatalogFragment()
    }
}
