package com.example.salmaflorist

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.salmaflorist.databinding.FragmentCatalogBinding
import com.example.salmaflorist.databinding.FragmentHomeBinding

class CatalogFragment : Fragment() {

    // View Binding setup yang aman untuk Fragment
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

        // Setup UI
        setupFilterUI()
        setupProductList()
    }

    private fun setupFilterUI() {
        // 1. Setup Kategori Dropdown
        val categories = db.getAllCategories()
        val categoryNames = mutableListOf("Semua") // Untuk pilihan 'all'
        categoryNames.addAll(categories.map { it.name })

        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.spinnerCategory.setAdapter(catAdapter)

        binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            currentCategoryId = if (position == 0) "all" else categories[position - 1].id.toString()
            setupProductList()
        }

        // 2. Setup Sort Dropdown
        val sortOptions = listOf("Default", "Harga Terendah", "Harga Tertinggi")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions)
        binding.spinnerPriceSort.setAdapter(sortAdapter)

        binding.spinnerPriceSort.setOnItemClickListener { _, _, _, id ->
            currentSort = sortOptions[id.toInt()]
            setupProductList()
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

        // Jika ingin manual via tombol 'Cari' sesuai desain Laravel:
        binding.btnSearch.setOnClickListener {
            currentKeyword = binding.etSearch.text.toString()
            setupProductList()
        }
    }

    private fun setupProductList() {
        // Mengambil data dari SQLite (Gunakan method JOIN yang kita bahas sebelumnya)
        val products = db.getProducts(currentKeyword, currentCategoryId, currentSort)

        if (products.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE

            // Inisialisasi Adapter & LayoutManager (2 kolom seperti web)
            val adapter = ProductAdapter(products, db)
            binding.rvProducts.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                this.adapter = adapter
                // Agar scrolling lebih halus di dalam NestedScrollView
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Penting: Set binding ke null untuk menghindari memory leak
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CatalogFragment()
    }
}