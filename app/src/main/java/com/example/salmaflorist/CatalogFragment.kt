package com.example.salmaflorist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.salmaflorist.adapter.CatalogProductAdapter
import com.example.salmaflorist.databinding.FragmentCatalogBinding
import com.example.salmaflorist.model.Product

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DBOpenHelper

    private var currentKeyword = ""
    private var currentCategoryId = "all"
    private var currentSort = "Default"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = (requireActivity() as MainActivity).getObject()
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterUI()
        setupProductList()
    }

    private fun setupFilterUI() {
        val categories = db.getAllCategories()
        val categoryNames = mutableListOf("Semua")
        categoryNames.addAll(categories.map { it.name })

        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.spinnerCategory.adapter = catAdapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategoryId = if (position == 0) "all" else categories[position - 1].id.toString()
                setupProductList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 2. Setup Sort Dropdown
        val sortOptions = listOf("Default", "Harga Terendah", "Harga Tertinggi")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions)
        binding.spinnerPriceSort.adapter = sortAdapter

        binding.spinnerPriceSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = sortOptions[position]
                setupProductList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 3. Setup Search (Real-time atau via tombol)
        // Jika ingin real-time seperti search bar modern:
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s.toString()
                setupProductList()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSearch.setOnClickListener {
            currentKeyword = binding.etSearch.text.toString()
            setupProductList()
        }
    }

    private fun setupProductList() {
        val products = db.getProducts(currentKeyword, currentCategoryId, currentSort)

        if (products.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            val adapter = CatalogProductAdapter(products, db) { product ->
                navigateToDetail(product)
            }
            binding.rvProducts.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                this.adapter = adapter
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun navigateToDetail(product: Product) {
        val fragment = ProductDetailFragment.newInstance(product)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leak
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CatalogFragment()
    }
}