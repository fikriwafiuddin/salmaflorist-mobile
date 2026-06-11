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
import com.example.salmaflorist.R
import com.example.salmaflorist.data.DBOpenHelper
import com.example.salmaflorist.databinding.FragmentAdminProductsBinding
import com.example.salmaflorist.databinding.ItemProductRowAdminBinding
import com.example.salmaflorist.model.Category
import com.example.salmaflorist.model.Product
import java.text.NumberFormat
import java.util.Locale

class AdminProductsFragment : Fragment() {
    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBOpenHelper
    private var categories: List<Category> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBOpenHelper(requireContext())

        setupFilter()
        setupSearch()
        
        binding.fabAddProduct.setOnClickListener {
            openProductForm()
        }

        loadProducts()
    }

    private fun setupFilter() {
        val rawCategories = dbHelper.getAllCategories()
        categories = listOf(Category(0, "Semua Kategori")) + rawCategories
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
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
                loadProducts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadProducts() {
        val keyword = binding.etSearch.text.toString()
        val selectedCat = categories[binding.spinnerCategoryFilter.selectedItemPosition]
        val catId = if (selectedCat.id == 0) "all" else selectedCat.id.toString()
        
        val products = dbHelper.getProducts(keyword, catId, "Default")
        val table = binding.tableProducts
        
        // Clear previous rows except header
        while (table.childCount > 2) {
            table.removeViewAt(2)
        }

        val inflater = LayoutInflater.from(requireContext())
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        products.forEach { product ->
            val rowBinding = ItemProductRowAdminBinding.inflate(inflater, table, false)
            with(rowBinding) {
                tvName.text = product.name
                tvCategory.text = product.category?.name
                tvPrice.text = formatter.format(product.price).replace("Rp", "Rp ")
                
                // Enhanced image loading logic
                val imageSource = product.image
                if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
                    try {
                        ivProduct.setImageURI(android.net.Uri.parse(imageSource))
                    } catch (e: SecurityException) {
                        ivProduct.setImageResource(R.drawable.placeholder_flower)
                    }
                } else {
                    // Resource-based images from seed
                    val resId = resources.getIdentifier(imageSource, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        ivProduct.setImageResource(resId)
                    } else {
                        ivProduct.setImageResource(R.drawable.placeholder_flower)
                    }
                }

                ivMenu.setOnClickListener { view ->
                    showPopupMenu(view, product)
                }
            }
            table.addView(rowBinding.root)
        }
    }

    private fun showPopupMenu(view: View, product: Product) {
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

    private fun showDeleteConfirmation(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                if (dbHelper.deleteProduct(product.id)) {
                    Toast.makeText(requireContext(), "Produk dihapus", Toast.LENGTH_SHORT).show()
                    loadProducts()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}