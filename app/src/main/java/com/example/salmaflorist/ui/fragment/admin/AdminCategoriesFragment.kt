package com.example.salmaflorist.ui.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salmaflorist.R
import com.example.salmaflorist.data.api.dto.ApiResult
import com.example.salmaflorist.data.api.dto.CategoryDto
import com.example.salmaflorist.data.repository.CategoryRepositoryProvider
import com.example.salmaflorist.databinding.FragmentAdminCategoriesBinding
import com.example.salmaflorist.databinding.ItemCategoryRowBinding
import com.example.salmaflorist.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminCategoriesFragment : Fragment() {
    private var _binding: FragmentAdminCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryRepository: com.example.salmaflorist.data.repository.CategoryRepository
    private var categoriesList = listOf<CategoryDto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Initialize repository with token provider
        categoryRepository = CategoryRepositoryProvider.getInstance {
            sessionManager.getToken() ?: ""
        }

        binding.fabAddCategory.setOnClickListener {
            showCategoryDialog()
        }

        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.getCategories()

            when (result) {
                is ApiResult.Success -> {
                    categoriesList = result.data
                    populateCategories()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    // Still populate with empty list
                    categoriesList = emptyList()
                    populateCategories()
                }
                is ApiResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun populateCategories() {
        val table = binding.tableCategories

        // Remove all views except header and divider
        while (table.childCount > 2) {
            table.removeViewAt(2)
        }

        val inflater = LayoutInflater.from(requireContext())

        categoriesList.forEach { category ->
            val rowBinding = ItemCategoryRowBinding.inflate(inflater, table, false)
            rowBinding.tvCategoryName.text = category.name

            rowBinding.ivMenu.setOnClickListener { view ->
                showPopupMenu(view, category)
            }

            table.addView(rowBinding.root)
        }

        if (categoriesList.isEmpty()) {
            val emptyText = com.google.android.material.textview.MaterialTextView(requireContext())
            emptyText.text = "Belum ada kategori"
            emptyText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyText.setPadding(32, 32, 32, 32)
            table.addView(emptyText)
        }
    }

    private fun showPopupMenu(view: View, category: CategoryDto) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_admin_options, popup.menu)

        // Clear and add specific actions for category
        popup.menu.clear()
        popup.menu.add(0, 1, 0, "Edit")
        popup.menu.add(0, 2, 1, "Hapus")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> showCategoryDialog(category)
                2 -> showDeleteConfirmation(category)
            }
            true
        }
        popup.show()
    }

    private fun showCategoryDialog(category: CategoryDto? = null) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (category == null) "Tambah Kategori" else "Edit Kategori")

        val input = EditText(requireContext())
        input.setPadding(64, 32, 64, 32)
        input.setText(category?.name)
        builder.setView(input)

        builder.setPositiveButton("Simpan") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                if (category == null) {
                    createCategory(name)
                } else {
                    updateCategory(category.id, name)
                }
            } else {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun createCategory(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.createCategory(name)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Kategori berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    loadCategories()
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

    private fun updateCategory(id: Int, name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.updateCategory(id, name)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Kategori berhasil diupdate!", Toast.LENGTH_SHORT).show()
                    loadCategories()
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

    private fun showDeleteConfirmation(category: CategoryDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori '${category.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteCategory(category.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCategory(id: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.deleteCategory(id)

            when (result) {
                is ApiResult.Success -> {
                    Toast.makeText(requireContext(), "Kategori berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    loadCategories()
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
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}