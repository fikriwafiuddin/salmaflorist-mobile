package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CategoryDto
import com.example.salmaflorist.data.api.dto.ProductDto
import com.example.salmaflorist.data.repository.ProductRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminProductsBinding
import com.example.salmaflorist.databinding.ItemProductRowAdminBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AdminProductsFragment : Fragment() {
    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var productRepository: com.example.salmaflorist.data.repository.ProductRepository
    private var categories: List<CategoryDto> = emptyList()
    private var productsList: List<ProductDto> = emptyList()
    // Store allCategories including "All" option for consistent access
    private var allCategories: List<CategoryDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        productRepository = ProductRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getCategories()

            when (result) {
                is ApiResult.Success -> {
                    categories = result.data
                    // Update allCategories with "All" option
                    allCategories = listOf(CategoryDto(0, "Semua Kategori")) + categories

                    setupFilter()
                    setupSearch()

                    binding.fabAddProduct.setOnClickListener {
                        openProductForm()
                    }

                    loadProducts()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    // Still setup with empty categories
                    categories = emptyList()
                    allCategories = listOf(CategoryDto(0, "Semua Kategori"))
                    setupFilter()
                    setupSearch()
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun setupFilter() {
        // Use allCategories which already includes "All" option
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            allCategories.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = adapter

        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Debounce could be added here for better performance
                loadProducts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadProducts() {
        val keyword = binding.etSearch.text.toString().trim().takeIf { it.isNotEmpty() }
        val selectedCatIndex = binding.spinnerCategoryFilter.selectedItemPosition

        // Use allCategories property instead of recreating the list
        val selectedCat = allCategories.getOrNull(selectedCatIndex)
        val categoryId = if (selectedCat?.id == 0 || selectedCat == null) null else selectedCat.id

        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.getProducts(
                categoryId = categoryId,
                search = keyword
            )

            when (result) {
                is ApiResult.Success -> {
                    productsList = result.data
                    populateProducts()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    productsList = emptyList()
                    populateProducts()
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun populateProducts() {
        if (_binding == null) return

        val table = binding.tableProducts

        // Clear previous rows except header
        while (table.childCount > 2) {
            table.removeViewAt(2)
        }

        val inflater = LayoutInflater.from(requireContext())
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        productsList.forEach { product ->
            val rowBinding = ItemProductRowAdminBinding.inflate(inflater, table, false)
            with(rowBinding) {
                tvName.text = product.name
                tvCategory.text = product.category?.name
                tvPrice.text = formatter.format(product.price).replace("Rp", "Rp ")

                // Load image
                val imageSource = product.image
                if (!imageSource.isNullOrEmpty()) {
                    if (imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
                        // Load from URL (Cloudinary or other web URL)
                        Glide.with(this@AdminProductsFragment)
                            .load(imageSource)
                            .placeholder(R.drawable.placeholder_flower)
                            .error(R.drawable.placeholder_flower)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ivProduct)
                    } else if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
                        try {
                            ivProduct.setImageURI(android.net.Uri.parse(imageSource))
                        } catch (e: SecurityException) {
                            ivProduct.setImageResource(R.drawable.placeholder_flower)
                        }
                    } else {
                        // Resource-based images
                        val resId = resources.getIdentifier(imageSource, "drawable", requireContext().packageName)
                        if (resId != 0) {
                            ivProduct.setImageResource(resId)
                        } else {
                            ivProduct.setImageResource(R.drawable.placeholder_flower)
                        }
                    }
                } else {
                    ivProduct.setImageResource(R.drawable.placeholder_flower)
                }

                ivMenu.setOnClickListener { view ->
                    showPopupMenu(view, product)
                }
            }
            table.addView(rowBinding.root)
        }

        if (productsList.isEmpty()) {
            val emptyText = com.google.android.material.textview.MaterialTextView(requireContext())
            emptyText.text = "Tidak ada produk"
            emptyText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyText.setPadding(32, 32, 32, 32)
            table.addView(emptyText)
        }
    }

    private fun showPopupMenu(view: View, product: ProductDto) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "Update")
        popup.menu.add(0, 2, 1, "Delete")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> openProductForm(product.id)
                2 -> showDeleteConfirmation(product)
            }
            true
        }
        popup.show()
    }

    private fun openProductForm(productId: Int = -1) {
        val fragment = AdminProductFormFragment().apply {
            arguments = Bundle().apply {
                putInt("PRODUCT_ID", productId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showDeleteConfirmation(product: ProductDto) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProduct(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = productRepository.deleteProduct(id)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadProducts()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun showError(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}